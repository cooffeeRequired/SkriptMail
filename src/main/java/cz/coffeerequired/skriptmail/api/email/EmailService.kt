package cz.coffeerequired.skriptmail.api.email

import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.ConfigFields
import cz.coffeerequired.skriptmail.api.WillUsed
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import jakarta.mail.Folder
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Store
import jakarta.mail.event.MessageCountAdapter
import jakarta.mail.event.MessageCountEvent
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import java.lang.Runnable
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.collections.set
import kotlin.time.measureTime

class EmailService(val id: String, @WillUsed val session: Session, private val store: Store) {
    companion object {
        private val registeredServices = mutableMapOf<String, EmailService>()
        private val registeredListeners = mutableMapOf<String, Pair<Folder, ScheduledTask>>()
        private val registeredMailbox = mutableMapOf<String, EmailInbox>()

        @WillUsed
        fun tryRegisterNewService(account: Account, id: String = UUID.randomUUID().toString()) {
            try {
                if (registeredServices[id] != null) return SkriptMail.logger().warn("&cService for id $id is already registered!")
                val session = createSession()
                val service = EmailService(id, session, connectStore(account, session))
                registeredServices[id] = service
                SkriptMail.logger().info("Registered new email service for id &f&n$id")
                tryRegisterAnListener(service, id)
            } catch (ex: Exception) {
                SkriptMail.logger().exception(ex, "Registration of new Service")
            }
        }

        private fun tryRegisterAnListener(service: EmailService, id: String) {
            val folder = service.getFolder("INBOX") ?: throw IllegalArgumentException("INBOX folder not found")
            folder.open(Folder.READ_ONLY)
            val event = BukkitEmailMessageEvent(id, true)
            val inbox = tryRegisterMailBox(folder, id)
            folder.addMessageCountListener(object : MessageCountAdapter() {
                override fun messagesAdded(e: MessageCountEvent?) {
                    runBlocking {
                        launch {
                            val mess = e!!.messages
                            for (m in mess) {
                                inbox.addMessage("Subject: ${m.subject} c:${if(m.contentType.contains("TEXT")) m.content else "Unsupported body of email!"}")
                                event.callEventWithData(
                                    m.subject,
                                    m.receivedDate,
                                    if(m.contentType.contains("TEXT")) m.content else "Unsupported body of email!",
                                    m.allRecipients,
                                    m.from
                                )
                            }
                        }
                    }
                }
            })
            val runner = Bukkit.getAsyncScheduler().runAtFixedRate(SkriptMail.instance(), { folder.messageCount }, 0, 1, TimeUnit.SECONDS)
            registeredListeners[id] = Pair(folder, runner)
        }

        private fun createSession(): Session {
            val props = Properties().apply {
                setProperty("mail.store.protocol", "imaps")
                setProperty("mail.imaps.ssl.trust", "*")
                setProperty("mail.imap.fetchsize", "3000000")
            }
            return Session.getInstance(props, null)
        }

        private fun connectStore(account: Account, session: Session): Store {
            val store = session.getStore("imaps")
            val (_, _, host) = account
            val sanitizedHost = host.let {
                if (host?.startsWith("imap") == true) {
                    val i = host.indexOf(".")
                    "imap.${host.substring(i + 1)}"
                } else {
                    host
                }
            }
            store.connect(sanitizedHost, account.authUsername, account.authPassword)
            return store
        }

        fun unregisterService(id: String) {
            registeredServices[id]?.let { service ->
                unregisterListener(id)
                unregisterMailbox(id)
                service.store.close()
                registeredServices.remove(id)
            }
        }

        private fun unregisterListener(id: String) {
            registeredListeners[id]?.let { (folder, runner) ->
                folder.close(true)
                if (!runner.isCancelled) {
                    runner.cancel()
                }
            }
            registeredListeners.remove(id)
        }

        private fun unregisterMailbox(id: String) {
            registeredMailbox[id]?.closeInbox()
            registeredMailbox.remove(id)
        }


        private fun fetchMessagesAndUpdateInbox(folder: Folder, inbox: EmailInbox) {
            val max = folder.messageCount
            val perRequest = ConfigFields.MAILBOX_PER_REQUEST.toInt()
            val left = if (max - perRequest < 0) 0 else max-perRequest

            runBlocking {
                val emailProcessingJobs = mutableListOf<Deferred<Boolean>>()
                val messages: Array<Message> = folder.messages.sliceArray(left..<max)
                val parsedMessages: MutableList<String> = mutableListOf()
                for (email in messages) {
                    val job = async {
                        val mimeMessage = email as MimeMessage
                        val contentType = mimeMessage.contentType
                        val subject = mimeMessage.subject
                        val content = when(contentType.contains("TEXT")) {
                            true -> "Subject: $subject c:${mimeMessage.content}"
                            else -> "Subject: $subject c:Unsupported body of email!"
                        }
                        parsedMessages.add(content)
                    }
                    emailProcessingJobs.add(job)
                }
                emailProcessingJobs.awaitAll()
                inbox.updateInbox(*parsedMessages.toTypedArray())
            }
        }


        private fun tryRegisterMailBox(folder: Folder, id: String): EmailInbox {
            val inbox = EmailInbox(id, null)
            inbox.openInbox()
            runBlocking {
                launch {
                    val runnable = Runnable {
                        if (folder.hasNewMessages()) {
                            val time = measureTime { fetchMessagesAndUpdateInbox(folder, inbox) }
                            if (ConfigFields.EMAIL_DEBUG == true) SkriptMail.logger().info("Emails was sync in &e$time")
                        }
                    }
                    inbox.applyTask(runnable)
                    registeredMailbox[id] = inbox
                    val time = measureTime { fetchMessagesAndUpdateInbox(folder, inbox) }
                    if (ConfigFields.EMAIL_DEBUG == true) SkriptMail.logger().info("Emails was sync in &e$time")
                }
            }
            return inbox
        }


        fun tryGetInbox(id: String): EmailInbox? {
            return this.registeredMailbox[id]
        }
    }

    @WillUsed
    fun getFolder(folder: String): Folder? = store.getFolder(folder)
}

class EmailInbox(private val id: String, private var task: Runnable?) {
    private val messages = mutableListOf<String?>()
    private var scheduler: ScheduledExecutorService? = Executors.newScheduledThreadPool(2)
    private var isClosed: Boolean = false
    private val initialDelay = 0L
    private val period = ConfigFields.MAILBOX_REFRESH_RATE

    fun openInbox(initial: List<String?>? = null) {
        try {
            task?.let { scheduler?.scheduleAtFixedRate(it, initialDelay, period, ConfigFields.MAILBOX_RATE) }
            initial?.let { messages.addAll(it) }
            isClosed = false
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun closeInbox() {
        scheduler?.shutdownNow(); scheduler = null; task = null; isClosed = true
    }

    @WillUsed fun updateInbox(vararg messages: String) {
        CompletableFuture.runAsync {
            val uniqueNewMessages = messages.filter { !this.messages.contains(it) }
            val overflow = (this.messages.size + uniqueNewMessages.size) - 2 * ConfigFields.MAILBOX_PER_REQUEST
            if (overflow > 0) {
                this.messages.addAll(uniqueNewMessages.takeLast((uniqueNewMessages.size - overflow).toInt()))
            } else {
                this.messages.addAll(uniqueNewMessages)
            }
        }
    }

    fun addMessage(message: String?) {
        this.messages.add(message)
    }

    override fun toString(): String {
        return "EmailInbox{messages=${messages.size}, id=$id, task=$task, scheduler: $scheduler, isClosed: $isClosed}"
    }

    fun applyTask(task: Runnable) {
        this.task = task
    }

    fun getEmails(range: IntRange, mode: Int): Array<String?> {
        if (this.messages.isEmpty()) return arrayOf()
        return when (mode) {
            2 -> messages.subList(range.first, range.last).toTypedArray()
            1 -> messages.subList((messages.size - range.last), (messages.size - range.first)).toTypedArray()
            else -> arrayOf()
        }
    }

    fun getFirstEmail(): String? {
        return messages.last()
    }
    fun getLastEmail(): String? {
        return this.messages.first()
    }
}
