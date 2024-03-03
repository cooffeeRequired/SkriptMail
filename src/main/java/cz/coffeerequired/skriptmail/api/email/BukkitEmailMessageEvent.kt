package cz.coffeerequired.skriptmail.api.email

import jakarta.mail.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.*

class ConvertedMessage(val msg: Message?) {
    var subject: String? = ""
    var content: String? = "empty"
    var date: Date? = Date()
    var recipients: Array<String?> = arrayOfNulls(0)
    var from: String? = ""

    init {
        if (msg != null) {
            runBlocking {
                withContext(Dispatchers.IO) {
                    async {
                        this@ConvertedMessage.subject = msg.subject ?: "No Subject"
                    }.await()
                    async {
                        this@ConvertedMessage.content = if (msg.contentType.contains("TEXT")) {
                            msg.content.toString().trimEnd()
                        } else {
                            "Non supported content type: ${msg.contentType}"
                        }
                    }.await()
                    async { this@ConvertedMessage.date = msg.receivedDate }.await()
                    async { this@ConvertedMessage.recipients = msg.allRecipients.map { it.toString() }.toTypedArray() }.await()
                    async { this@ConvertedMessage.from = msg.from[0].toString() }.await()
                }
            }
        }
    }

    override fun toString(): String {
        return "Message ($subject) of $from to ${recipients.contentToString()}"
    }

}


class BukkitEmailMessageEvent(private val id: String, isAsync: Boolean) : Event(isAsync) {
    var lastReceived: ConvertedMessage? = null
        private set
    fun id(): String = id
    fun callEventWithMessage(msg: Message) {
        this.lastReceived = ConvertedMessage(msg)
        this.callEvent()
    }

    override fun getHandlers(): HandlerList = Companion.handlers
    companion object {
        private val handlers = HandlerList()

        /** Suppressed for reflection of Skript Events */
        @Suppress("unused")
        @JvmStatic fun getHandlerList(): HandlerList = handlers
    }

}

