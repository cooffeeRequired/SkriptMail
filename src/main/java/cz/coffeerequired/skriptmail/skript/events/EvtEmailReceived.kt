package cz.coffeerequired.skriptmail.skript.events

import ch.njol.skript.Skript
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Examples
import ch.njol.skript.doc.Name
import ch.njol.skript.doc.Since
import ch.njol.skript.expressions.base.EventValueExpression
import ch.njol.skript.lang.*
import ch.njol.skript.registrations.EventValues
import ch.njol.skript.util.Getter
import ch.njol.util.Kleenean
import cz.coffeerequired.skriptmail.api.email.BukkitEmailMessageEvent
import cz.coffeerequired.skriptmail.api.email.EmailAddress
import jakarta.mail.Address
import org.bukkit.event.Event
import java.util.*


@Name("On Email Received")
@Description("The event will be triggered every time you get an email.")
@Since("1.0")
@Examples("on email receive")
class EvtEmailReceived : SkriptEvent() {
    override fun check(event: Event?): Boolean { return event is BukkitEmailMessageEvent }
    override fun toString(event: Event?, debug: Boolean): String { return "on email receive" }
    override fun init(args: Array<out Literal<*>>?, matchedPattern: Int, parseResult: SkriptParser.ParseResult?): Boolean { return true }
    companion object {
        init {
            Skript.registerEvent(
              "Email Receive",
              EvtEmailReceived::class.java,
              BukkitEmailMessageEvent::class.java,
              "email receive"
            )

            EventValues.registerEventValue(
                BukkitEmailMessageEvent::class.java, String::class.java,
                object : Getter<String, BukkitEmailMessageEvent>() {
                    override fun get(event: BukkitEmailMessageEvent): String { return event.id() }
                }, 0
            )
            EventValues.registerEventValue(
                BukkitEmailMessageEvent::class.java, Date::class.java,
                object : Getter<Date, BukkitEmailMessageEvent>() {
                    override fun get(event: BukkitEmailMessageEvent): Date { return event.lastReceived.rec }
                }, 0
            )
            EventValues.registerEventValue(
                BukkitEmailMessageEvent::class.java, EmailAddress::class.java,
                object : Getter<EmailAddress, BukkitEmailMessageEvent>() {
                    override fun get(event: BukkitEmailMessageEvent): EmailAddress {
                        return EmailAddress("", "")
                    }
                }, 0
            )
    } }
}

@Name("Event-Id (On Email Received)")
@Description("Gets id of service from that event")
@Since("1.0")
@Examples("""
    on email received:
        send event-id to console
""")
class EvtExprEmailReceivedID : EventValueExpression<String>(String::class.java) {
    companion object {
        init { Skript.registerExpression(EvtExprEmailReceivedID::class.java, String::class.java, ExpressionType.EVENT, "[the] [event-]id") }
    }
}

@Name("Event-Message (On Email Received)")
@Description("Gets message from that event")
@Since("1.0")
@Examples("""
    on email received:
        send event-message to console
""")
class EvtExprEmailReceivedMessage : EventValueExpression<String>(String::class.java) {
    companion object {
        init {
            Skript.registerExpression(EvtExprEmailReceivedMessage::class.java, String::class.java, ExpressionType.EVENT,
                "[the] [event-]message"
            )
        }
    }

    override fun get(event: Event?): Array<String?> {
        if (event is BukkitEmailMessageEvent) { return arrayOf(event.lastReceived.content.toString()) }
        return super.get(event)
    }

}

@Name("Event-Subject (On Email Received)")
@Description("Gets subject from that event")
@Since("1.0")
@Examples("""
    on email received:
        send event-subject to console
""")
class EvtExprEmailReceivedSubject : EventValueExpression<String>(String::class.java) {
    companion object {
        init {
            Skript.registerExpression(EvtExprEmailReceivedSubject::class.java, String::class.java, ExpressionType.EVENT,
                "[the] [event-]subject"
            )
        }
    }

    override fun get(event: Event?): Array<String?> {
        if (event is BukkitEmailMessageEvent) { return arrayOf(event.lastReceived.subject) }
        return super.get(event)
    }
}

@Name("Event-Recipient (On Email Received)")
@Description("Gets recipient from that event")
@Since("1.0")
@Examples("""
    on email received:
        send event-recipient to console
""")
class EvtExprEmailReceivedRecipient : EventValueExpression<EmailAddress>(EmailAddress::class.java) {
    companion object {
        init {
            Skript.registerExpression(EvtExprEmailReceivedRecipient::class.java, EmailAddress::class.java, ExpressionType.EVENT,
                "[the] [event-]recipient(1:s|)"
            )
        }
    }

    private var isPlural: Boolean = false

    override fun get(event: Event?): Array<EmailAddress> {
        if (event is BukkitEmailMessageEvent) {
            val recipients: Array<EmailAddress> = event.lastReceived.recipients.convertAddress()
            return if(!isPlural) arrayOf(recipients.first()) else recipients
        }
        return arrayOf()
    }
    override fun isSingle(): Boolean { return !isPlural }

    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parser: SkriptParser.ParseResult?): Boolean {
        isPlural = parser!!.mark == 1
        return super.init(expressions, matchedPattern, isDelayed, parser)
    }
}

@Name("Event-Received Date (On Email Received)")
@Description("Gets the received date of email from that event")
@Since("1.0")
@Examples("""
    on email received:
         send event-received date to console
""")
class EvtExprEmailReceivedDate : EventValueExpression<Date>(Date::class.java) {
    companion object {
        init {
            Skript.registerExpression(EvtExprEmailReceivedDate::class.java, Date::class.java, ExpressionType.EVENT,
                "[the] [event-]received date"
            )
        }
    }


    override fun get(event: Event?): Array<Date?> {
        if (event is BukkitEmailMessageEvent) { return arrayOf(event.lastReceived.rec) }
        return super.get(event)
    }
}

fun Array<Address>.convertAddress(): Array<EmailAddress> {
    return this.map {
        val iAddress = it.toString()
        val iStart = iAddress.indexOfFirst { it == '<' }+1
        val iEnd = iAddress.indexOfLast { it == '>' }
        if (iStart > 0 || iEnd > 0 || iStart-2 > 0) {
            val email = iAddress.substring(iStart, iEnd)
            val name = iAddress.substring(0, iStart-2)
            EmailAddress(name, email)
        } else {
            EmailAddress("<none>", iAddress)
        }
    }.toTypedArray()
}

@Name("Event-Sender(On Email Received)")
@Description("Gets the sender of that email from event")
@Since("1.0")
@Examples("""
    on email received:
         send event-sender to console
""")
class EvtExprEmailSender: EventValueExpression<EmailAddress>(EmailAddress::class.java) {
    companion object {
        init {
            Skript.registerExpression(EvtExprEmailSender::class.java, EmailAddress::class.java, ExpressionType.EVENT,
                "[the] [event-]sender"
            )
        }
    }

    override fun get(event: Event?): Array<EmailAddress> {
        if (event is BukkitEmailMessageEvent) { return event.lastReceived.from.convertAddress() }
        return arrayOf()
    }
}