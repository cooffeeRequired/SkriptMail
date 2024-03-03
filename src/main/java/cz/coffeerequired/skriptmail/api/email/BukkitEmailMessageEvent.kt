package cz.coffeerequired.skriptmail.api.email

import jakarta.mail.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.*

class LastReceived(val msg: Message) {
    var subject: String? = ""
    var content: String? = "empty"
    var date: Date? = Date()
    var recipients: Array<String?> = arrayOfNulls(0)
    var from: String? = ""

    init {
        runBlocking {
            withContext(Dispatchers.IO) {
                async {
                    this@LastReceived.subject = msg.subject ?: "No Subject"
                }.await()
                async {
                    this@LastReceived.content = if (msg.contentType.contains("TEXT")) {
                        msg.content.toString().trimEnd()
                    } else {
                        "Non supported content type: ${msg.contentType}"
                    }
                }.await()
                async { this@LastReceived.date = msg.receivedDate }.await()
                async { this@LastReceived.recipients = msg.allRecipients.map { it.toString() }.toTypedArray() }.await()
                async { this@LastReceived.from = msg.from[0].toString() }.await()
            }
        }
    }

    override fun toString(): String {
        return "LR{content=$content, subject=$subject, date=$date}"
    }

}


class BukkitEmailMessageEvent(private val id: String, isAsync: Boolean) : Event(isAsync) {
    var lastReceived: LastReceived? = null
        private set
    fun id(): String = id
    fun callEventWithMessage(msg: Message) {
        this.lastReceived = LastReceived(msg)
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

