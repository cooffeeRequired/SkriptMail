package cz.coffeerequired.skriptmail.api.email

import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.ConfigFields
import cz.coffeerequired.skriptmail.api.MailboxRateUnit
import cz.coffeerequired.skriptmail.api.WillUsed
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import jakarta.mail.Folder
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Store
import jakarta.mail.event.MessageCountAdapter
import jakarta.mail.event.MessageCountEvent
import org.bukkit.Bukkit
import java.util.Properties
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.jvm.Throws

class EmailService(val id: String, @WillUsed val session: Session, private val store: Store) {
    companion object {
        private val registeredServices: MutableMap<String, EmailService> = mutableMapOf()
        private val registeredListeners: MutableMap<String, Pair<Folder, ScheduledTask>> = mutableMapOf()
        private val registeredMailbox: MutableMap<String, Pair<List<Message>?, ScheduledTask?>> = mutableMapOf()
        @WillUsed
        fun tryRegisterNewService(id: String?, account: Account) {
            try {
                tryRegisterNewService(id, account.host, account.authUsername, account.authPassword)
            } catch (ex: Exception) {
                SkriptMail.gLogger().exception(ex, "Registration of new Service")
                return
            }
        }

        @WillUsed
        fun getInbox(id: String): List<Message>? {
            val pairs = this.registeredMailbox[id]
            if (pairs != null) {
                return pairs.first
            }
            return null
        }

        fun tryUnregisterService(id: String) {
            val service = registeredServices.remove(id)
            if (service != null) {
                try {
                    service.store.close()
                    val listenerPair = registeredListeners.remove(id)
                    if (listenerPair == null) {
                        SkriptMail.gLogger().error("The listener for id $id doesn't found!")
                    } else {
                        val folder = listenerPair.first;val listener = listenerPair.second
                        if (folder.isOpen) { folder.close(true) } else { SkriptMail.gLogger().error("The folder for listener of id $id is already closed!") }
                        if (!listener.isCancelled) { listener.cancel() } else { SkriptMail.gLogger().error("The listener of id $id is already canceled!") }
                    }
                    val inboxPair = registeredMailbox.remove(id)
                    if (inboxPair == null) {
                        SkriptMail.gLogger().error("The listener for id $id doesn't found!")
                    } else {
                        val listener = inboxPair.second
                        if (listener != null) {
                            if (!listener.isCancelled) { listener.cancel() } else { SkriptMail.gLogger().error("The listener of id $id is already canceled!") }
                        } else {
                            SkriptMail.gLogger().error("The listener for id $id doesn't found!")
                        }
                    }
                } catch (ex: Exception) {
                    SkriptMail.gLogger().exception(ex, "Something goes wrong in tryUnregisterService")
                }
            }
        }

        private fun tryRegisterAnListener(service: EmailService, id: String) {
            val folder = service.getFolder("INBOX")
            folder!!.open(Folder.READ_WRITE)
            val event = BukkitEmailMessageEvent(id, true)
            folder.addMessageCountListener(object : MessageCountAdapter() {
                override fun messagesAdded(e: MessageCountEvent?) {
                    Bukkit.getAsyncScheduler().runNow(SkriptMail.instance()) {
                        val mess = e!!.messages
                        for (m in mess) {
                            event.callEventWithData(
                                m.subject,
                                m.receivedDate,
                                m.content,
                                m.allRecipients,
                                m.from
                            )
                        }
                    }
                }
            })
            val runner = Bukkit.getAsyncScheduler().runAtFixedRate(SkriptMail.instance(), { folder.messageCount }, 0, 700, TimeUnit.MILLISECONDS)
            var messages: List<Message>? = null
            val inboxListener: ScheduledTask?

            if (ConfigFields.MAILBOX_ENABLED == true) {
                inboxListener = Bukkit.getAsyncScheduler().runAtFixedRate(SkriptMail.instance(), {
                    messages = folder.messages.slice(0 ..ConfigFields.MAILBOX_PER_REQUEST.toInt())
                    if (ConfigFields.MAILBOX_FILTER != null) {
                        messages = messages!!.filter { it.content.toString().matches(ConfigFields.MAILBOX_FILTER!!) }
                    }
                    synchronized(this) {
                        val t = registeredMailbox[id]
                       registeredMailbox[id] = Pair(messages, t?.second)
                    }
                },
                    0,
                    ConfigFields.MAILBOX_REFRESH_RATE,
                    when (ConfigFields.MAILBOX_RATE) {
                        MailboxRateUnit.MILIS -> TimeUnit.MILLISECONDS
                        MailboxRateUnit.SECONDS -> TimeUnit.SECONDS
                        MailboxRateUnit.MINUTES -> TimeUnit.MINUTES
                        MailboxRateUnit.HOURS -> TimeUnit.HOURS
                })
                registeredMailbox[id] = Pair(messages, inboxListener)
            }
            registeredListeners[id] = Pair(folder, runner)
        }

        @Throws(Exception::class)
        fun tryRegisterNewService(id: String? = UUID.randomUUID().toString(), potentialHost: String?, name: String?, password: String?) {
               Bukkit.getAsyncScheduler().runNow(SkriptMail.instance()) {
                   try {
                       val props = Properties().apply {
                           setProperty("mail.store.protocol", "imaps")
                           setProperty("mail.imaps.ssl.trust", "*")
                       }
                       val session = Session.getInstance(props, null)
                       val store = session.getStore("imaps")
                       val host: String
                       if (!potentialHost!!.startsWith("imap")) {
                           val i = potentialHost.indexOfFirst { it == '.' }
                           val k = potentialHost.substring(i+1)
                           host = "imap.${k}"
                       } else {
                           host = potentialHost
                       }
                       store.connect(host, name, password)
                       id?.let { key ->
                           val service = EmailService(key, session, store)
                           when (registeredServices.containsKey(key)) {
                               true -> SkriptMail.gLogger().warn("&cEmail service for id &f'$id'&c was already registered.")
                               false -> {
                                   registeredServices[key] = service
                                   SkriptMail.gLogger().info("Registered new email service for id &f&n$id")
                                   tryRegisterAnListener(service, id)
                               }
                           }
                       }
                   } catch (ex: Exception) { throw ex }
               }
        }
    }

    @WillUsed
    fun getFolder(folder: String): Folder? { return this.store.getFolder(folder) }
}