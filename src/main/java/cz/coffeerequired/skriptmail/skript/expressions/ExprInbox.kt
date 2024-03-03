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
import cz.coffeerequired.skriptmail.api.email.EmailServiceProvider
import jakarta.mail.Message
import org.bukkit.event.Event

@Name("Inbox Emails")
@Description("Get emails from the inbox")
@Since("1.0")
@Examples("""
     set {_mails::*} to last 2 emails of service "test" 
     set {_mail} to first email of service "test"
""")

class ExprInbox : SimpleExpression<Message>() {
    companion object{
        init {
            Skript.registerExpression(ExprInbox::class.java, Message::class.java, ExpressionType.SIMPLE,
                "(1:last|2:first) message of service %string%",
                "(1:last|2:first) %number% messages of service %string%"
            )
        }
    }

    private lateinit var exprNum: Expression<Long>
    private lateinit var exprServiceId: Expression<String>
    private var isLast: Boolean = false
    private var multiple: Boolean = false

    override fun toString(event: Event?, debug: Boolean): String {
        return "${if(isLast) "last" else "first" } of service ${exprServiceId.toString(event, debug)}"
    }

    @Suppress("UNCHECKED_CAST")
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

    override fun isSingle(): Boolean = !multiple

    override fun getReturnType(): Class<out Message> = Message::class.java

    override fun get(event: Event?): Array<Message?> {
        val id = exprServiceId.getSingle(event)
        val provider = id?.let { EmailServiceProvider.registeredServices[id] }
        if (provider == null) { SkriptMail.logger().warn("&cThe service or mailbox of the given id $id weren't initialized yet."); return arrayOf() }
        val mailbox = provider.mailbox
        if (mailbox != null) {
            return if (multiple) {
                val count = exprNum.getSingle(event)
                mailbox.getEmails(0..(count?.toInt() ?: 10), if(isLast) 2 else 1)
            } else {
                val message = if(isLast) mailbox.getLastEmail() else mailbox.getFirstEmail()
                arrayOf(message)
            }
        }
        return arrayOf()
    }
}