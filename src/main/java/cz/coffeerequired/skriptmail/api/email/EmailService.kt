package cz.coffeerequired.skriptmail.api.email

import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.ConfigFields.EMAIL_DEBUG
import cz.coffeerequired.skriptmail.api.ConfigFields.PROJECT_DEBUG
import cz.coffeerequired.skriptmail.api.isHTML
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import jakarta.mail.*
import jakarta.mail.event.MessageCountAdapter
import jakarta.mail.event.MessageCountEvent
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

class EmailService(
    val serviceId: String = UUID.randomUUID().toString(),
    private val properties: Properties = Properties(),
    private val usernameOrEmail: String = "",
    private val password: String = "",
    val address: String,
    val host: EmailHost?
) {

    private var session: Session? = null
    private var store: Store? = null
    private var transport: Transport? = null

    companion object {
        /**
         * Performs an asynchronous operation.
         *
         * @param block the block of code to execute as an asynchronous operation
         */
        @OptIn(DelicateCoroutinesApi::class)
        suspend fun performAsyncOperation(block: suspend () -> Unit) {
            val task = GlobalScope.async { block() }
            runBlocking { task.await() }
        }

        private val runners = mutableMapOf<String, ScheduledTask>()

        fun sendAnonymous(email: Email?, username: String?, password: String?) {
            if (email == null || username == null || password == null) return

            val host: EmailHost = email.account.component10() ?: host {
                val (_, _, host, port, _, starttls, _) = email.account
                smtp("smtp.${host}", port!!.toInt())
                imap("imap.${host}", 993)
                tlsAllowed = starttls == true
                sslAllowed = true
                name("Custom")
            }

            val service = EmailServiceProvider.newEmailService(email.account.id to UUID.randomUUID().toString(), email.account.address, host, username, password)
            service.tryConnect({
                runBlocking {
                    launch {
                        it.sendEmail(*email.recipients!!.toTypedArray(), content = email.content ?: "",subject =  email.subject ?: "")
                    }.join()
                }
                it.selfDestroy()
            }, true)

        }
    }

    private fun createSMTPConnection() {
        val transport = session?.getTransport("smtp")
        transport?.connect()
        if (PROJECT_DEBUG && EMAIL_DEBUG) { SkriptMail.logger().debug("[SMTP] connection status $0 ${transport?.isConnected} $1 $this") }
        this.transport = transport
    }

    fun selfDestroy() {
        this.transport?.close()
        this.store?.close()
        EmailServiceProvider.registeredServices.remove(this.serviceId)
    }


    private fun createIMAPConnection() {
        val store = session?.getStore("imap")
        store?.connect()
        if (PROJECT_DEBUG && EMAIL_DEBUG) { SkriptMail.logger().debug("[IMAP] connection status $0 ${store?.isConnected} $1 $this") }
        this.store = store
    }

    fun tryConnect(callback: (it: EmailService) -> Unit, noImap: Boolean? = false) {
        val startTime = Instant.now()
        if (PROJECT_DEBUG && EMAIL_DEBUG) { SkriptMail.logger().debug("Trying established connection $0 $this") }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val session = if(this@EmailService.session == null) {
                    withContext(Dispatchers.IO) {
                        Session.getInstance(properties, object : Authenticator() {
                            override fun getPasswordAuthentication(): PasswordAuthentication = PasswordAuthentication(usernameOrEmail, password)
                        })
                    }
                } else { this@EmailService.session }
                this@EmailService.session = session

                val job = async {
                    createSMTPConnection()
                    if (noImap != true) createIMAPConnection()
                }
                job.await()
                callback(this@EmailService)
                val endTime = Instant.now()
                val duration = Duration.between(startTime, endTime)
                if (PROJECT_DEBUG && EMAIL_DEBUG) { SkriptMail.logger().debug("Total connection time for [SMTP/IMAP] takes $0 ${duration.toMillis()/1000.0} seconds") }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
    suspend fun sendEmail(vararg receivers: String, content: String, subject: String) {
        val startTime = Instant.now()
        withContext(Dispatchers.IO) {
            try {
                val mimeMessage = MimeMessage(this@EmailService.session)
                if (this@EmailService.address.contains(";")) {
                    val parts = this@EmailService.address.split(";")
                    mimeMessage.setFrom(InternetAddress(parts[1], parts[0]))
                } else {
                    mimeMessage.setFrom(InternetAddress(this@EmailService.address.replace(";", ""), "Skript-Mailer"))
                }
                mimeMessage.subject = subject
                if (content.isHTML()) mimeMessage.setContent(content, "text/html; charset=utf-8") else mimeMessage.setText(content)
                this@EmailService.transport?.sendMessage(mimeMessage, receivers.map { InternetAddress(it) }.toTypedArray())
            } catch (ex: Exception) {
                SkriptMail.logger().exception(ex, "Failed to send email")
            } finally {
                val endTime = Instant.now()
                val duration = Duration.between(startTime, endTime)
                if (PROJECT_DEBUG && EMAIL_DEBUG) SkriptMail.logger().debug("Job: Sent email, time taken: ${duration.toMillis()/1000.0}s")
            }
        }
    }

    internal fun registerMailbox(): EmailMailbox? {
        if (store == null) {
            if (PROJECT_DEBUG && EMAIL_DEBUG) {
                SkriptMail.logger().debug("Store cannot be null !$ '$serviceId'")
                SkriptMail.logger().debug("Store: $store, null")
            }
            throw EmailException("Store cannot be null!", EmailExceptionType.INITIALIZE)
        }
        val result = runCatching {
            if (PROJECT_DEBUG && EMAIL_DEBUG) { SkriptMail.logger().debug("Trying register mailbox !$ '$serviceId'") }
            val mailbox = store?.let { EmailMailbox.register(serviceId, it, listOf()) }
            if (PROJECT_DEBUG && EMAIL_DEBUG) { SkriptMail.logger().debug("Mailbox registered successfully. $0 $mailbox $1 '$serviceId'") }
            if (mailbox != null) {
                mailbox.bind(BukkitEmailMessageEvent(serviceId, true))
                if (PROJECT_DEBUG && EMAIL_DEBUG) { SkriptMail.logger().debug("Mailbox bind event. $0 $mailbox $1 '$serviceId' $2 ${mailbox.event}") }
                mailbox.folder.open(2)

                if (PROJECT_DEBUG && EMAIL_DEBUG) { SkriptMail.logger().debug("Mailbox opened and folder in use $0 $mailbox, $1 ${mailbox.folder}") }
                mailbox.folder.addMessageCountListener(object : MessageCountAdapter() {
                    override fun messagesAdded(e: MessageCountEvent?) {
                        runBlocking {
                            performAsyncOperation {
                                for (message in e!!.messages) {
                                    mailbox.addMessage(message)
                                    mailbox.emit { it.callEventWithMessage(message) }
                                }
                            }
                        }
                    }
                })

                val runner = Bukkit.getAsyncScheduler().runAtFixedRate(SkriptMail.instance(), { mailbox.folder.messageCount }, 0, 1L, TimeUnit.SECONDS)
                runners += serviceId to runner
                if (PROJECT_DEBUG && EMAIL_DEBUG) { SkriptMail.logger().debug("Runner for repeating task registered succesfully $0 $runner") }
                if (PROJECT_DEBUG && EMAIL_DEBUG) { SkriptMail.logger().debug("Service for id $serviceId was registered succesfully $0 ${this@EmailService}") }
                return mailbox
            } else {
                SkriptMail.logger().error("Mailbox cannot be null!")
            }
            return null
        }
        result.onFailure {
            if (it.message!!.contains("Store cannot be null!")) {
                SkriptMail.logger().exception(it as Exception, "Store cannot be null!")
                if (PROJECT_DEBUG && EMAIL_DEBUG) { SkriptMail.logger().debug("Exception threw $it") }
            }
        }
        return result.getOrDefault(null)
    }

    internal fun unregisterMailbox() {
        store?.let { EmailMailbox.unregister(serviceId) }
        val runner = runners.remove(serviceId)
        if (PROJECT_DEBUG && EMAIL_DEBUG) SkriptMail.logger().debug("Removed runner $0 $runner")
    }
}
