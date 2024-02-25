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
import cz.coffeerequired.skriptmail.api.email.EmailInbox
import cz.coffeerequired.skriptmail.api.email.EmailService
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
                "(1:last|2:first) message of service %string%",
                "(1:last|2:first) %number% messages of service %string%"
            )
        }
    }

    private lateinit var exprNum: Expression<Long>
    private lateinit var exprServiceId: Expression<String>
    private var isLast: Boolean = false
    private var multiple: Boolean = false


    override fun get(event: Event?): Array<InboxEmails?> {
        val id = exprServiceId.getSingle(event)
        val inbox = id?.let { EmailService.tryGetInbox(it) }
        println(inbox)
        if (inbox == null) { SkriptMail.gLogger().warn("&cThe service or mailbox of the given id $id weren't initialized yet."); return arrayOf() }
        return if (multiple) {
            val count = exprNum.getSingle(event)
            run {
                val messages = inbox.getEmails(0..(count?.toInt() ?: 10), if(isLast) 2 else 1).map { str ->
                    val parts = str?.split("c:")
                    if (parts?.size!! > 1) {
                        val subject = parts[0].slice(8..<parts[0].length)
                        val content = parts[1]
                        InboxEmails(subject, content)
                    } else { null }
                }
                return messages.toTypedArray()
            }
        } else {
            val messages = inbox.getEmails(0..1, if(isLast) 2 else 1).map { str ->
                val parts = str?.split("c:")
                if (parts?.size!! > 1) {
                    val subject = parts[0].slice(8..<parts[0].length)
                    val content = parts[1]
                    InboxEmails(subject, content)
                } else { null}
            }
            messages.toTypedArray()
        }
    }

    override fun isSingle(): Boolean {
        return !multiple
    }

    override fun getReturnType(): Class<out InboxEmails> {
        return InboxEmails::class.java
    }

    override fun toString(event: Event?, debug: Boolean): String {
        return "${if(isLast) "last" else "first" } ${"${exprNum.toString(event, debug)} messages"} of service ${exprServiceId.toString(event, debug)}"
    }

    @Suppress("unchecked_cast")
    override fun init(
        expressions: Array<out Expression<*>>?,
        matchedPattern: Int,
        isDelayed: Kleenean?,
        parseResult: SkriptParser.ParseResult?
    ): Boolean {
        isLast = parseResult!!.mark == 1
        multiple = matchedPattern == 1

        if (multiple) {
            this.exprNum = expressions?.get(0) as Expression<Long>
            this.exprServiceId = expressions[1] as Expression<String>
        } else {
            this.exprServiceId = expressions?.get(0) as Expression<String>
        }
        return true
    }
}