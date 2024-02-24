package cz.coffeerequired.skriptmail.skript.expressions

import ch.njol.skript.Skript
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Examples
import ch.njol.skript.doc.Name
import ch.njol.skript.doc.Since
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.util.Kleenean
import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.email.EmailService
import jakarta.mail.Message
import org.bukkit.event.Event

class InboxEmails(val subject: String, val content: String) {
    override fun toString(): String {
        return "email with subject $subject & and body $content"
    }
}

@Name("Inbox Emails")
@Description("Get emails from the inbox")
@Since("1.0")
@Examples("""
     set {_mails::*} to last 2 emails of service "test" 
""")
class ExprInbox : SimpleExpression<InboxEmails>() {

    companion object{
        init {
            Skript.registerExpression(ExprInbox::class.java, InboxEmails::class.java, ExpressionType.SIMPLE,
                "(1:last|2:first) [%-number%] message[s] of service %string%"
            )
        }
    }

    private lateinit var exprNum: Expression<Long>
    private lateinit var exprServiceId: Expression<String>
    private var isLast: Boolean = false
    private var multiple: Boolean = false


    override fun get(event: Event?): Array<InboxEmails?> {
        var messages: List<Message>?
        return if (multiple) {
            val count = exprNum.getSingle(event)
            val id = exprServiceId.getSingle(event)
            messages = id?.let { EmailService.getInbox(it) }
            if (messages == null) {
                SkriptMail.gLogger().warn("&cThe are now opened inbox for service '&f$id&c'")
                return arrayOf()
            }
            val size = messages.size
            //println("1: ${size-count!!.toInt()}, 2: $size")

            messages = when (isLast) {
                true -> messages.slice(size-count!!.toInt()..<size)
                else -> messages.slice(0 ..count!!.toInt())
            }
            messages.map { InboxEmails(it.subject, it.content.toString()) }.toTypedArray()
        } else {
            val id = exprServiceId.getSingle(event)
            messages = id?.let { EmailService.getInbox(it) }
            if (messages == null) {
                SkriptMail.gLogger().warn("&cThe are now opened inbox for service '&f$id&c'")
                return arrayOf()
            }
            arrayOf(when(isLast) {
                true -> messages.last().let { InboxEmails(it.subject, it.content.toString()) }
                else -> messages.first().let { InboxEmails(it.subject, it.content.toString()) }
            })
        }
    }

    override fun isSingle(): Boolean {
        return !multiple
    }

    override fun getReturnType(): Class<out InboxEmails> {
        return InboxEmails::class.java
    }

    override fun toString(event: Event?, debug: Boolean): String {
        return "${if(isLast) "last" else "first" } ${if(exprNum != null) "${exprNum.toString(event, debug)} messages" else "message"} of service ${exprServiceId.toString(event, debug)}"
    }

    @Suppress("unchecked_cast")
    override fun init(
        expressions: Array<out Expression<*>>?,
        matchedPattern: Int,
        isDelayed: Kleenean?,
        parseResult: SkriptParser.ParseResult?
    ): Boolean {
        isLast = parseResult!!.mark == 1
        if (expressions!!.size > 1) {
            this.exprNum = expressions[0] as Expression<Long>
            this.exprServiceId = expressions[1] as Expression<String>
            this.multiple = true
        } else {
            this.exprServiceId = expressions[0] as Expression<String>
        }
        return true
    }
}