package cz.coffeerequired.skriptmail.skript.effects

import ch.njol.skript.Skript
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.util.AsyncEffect
import ch.njol.util.Kleenean
import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.Config
import cz.coffeerequired.skriptmail.api.email.Email
import cz.coffeerequired.skriptmail.api.email.EmailController
import org.bukkit.event.Event
import java.util.*

@Suppress("UNUSED")
class EffTransmitEmail: AsyncEffect() {
    override fun execute(event: Event?) {
        val email = this.emailExpr.getSingle(event)
        if (email != null) {
            val (field, recipient, subject, content) = email
            try {
                val controller: EmailController
                val form : EmailController.Companion.Form = EmailController.Companion.Form.formBuilder()
                    .content(content)
                    .recipient(recipient)
                    .subject(subject)
                    .build()
                form.usingTemplate = email.hasTemplate
                if (line == 1) {
                    if (email.field.auth == true) {
                        val sU = this.emailAuthUsername.getSingle(event)
                        val sP = this.emailAuthPassword.getSingle(event)
                        if (sP != null && sU != null) {
                            controller = EmailController(field, sU, sP)
                            controller.setForm(form)
                            controller.send()
                            Config.executedEmails[Date()] = email
                        } else {
                            SkriptMail.gLogger().warn("You are using a secured connection, do you forgot on credentials, expected &nusername&7 and &npassword&7 but got only %s %s",
                                if(sU != null) "username" else "",
                                if(sP != null) ",password" else ""
                            )
                        }
                    } else {
                        controller = EmailController(email.field, null, null)
                        controller.setForm(form)
                        controller.send()
                        Config.executedEmails[Date()] = email
                    }
                } else {
                    controller = EmailController(email.field, email.field.authUsername, email.field.authPassword)
                    controller.setForm(form)
                    controller.send()
                    Config.executedEmails[Date()] = email
                }
            } catch (ex: Exception) {
                SkriptMail.gLogger().exception(ex, ex.cause)
            }
        }
    }

    override fun toString(event: Event?, debug: Boolean): String {
        return if (line == 0) {
            "post email ${this.emailExpr.toString(event, debug)}"
        } else {
            "post email ${this.emailExpr.toString(event, debug)} using auth username ${this.emailAuthUsername.toString(event, debug)} and password ${this.emailAuthPassword.toString(event, debug)}"
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>>?,
        matchedPattern: Int,
        isDelayed: Kleenean?,
        parseResult: SkriptParser.ParseResult?
    ): Boolean {
        parser.hasDelayBefore = Kleenean.FALSE
        this.line = matchedPattern
        this.emailExpr = expressions!![0] as Expression<Email>
        if (line == 1) {
            this.emailAuthUsername = expressions[1] as Expression<String>
            this.emailAuthPassword = expressions[2] as Expression<String>
        }
        return true
    }

    private lateinit var emailExpr: Expression<Email>
    private lateinit var emailAuthUsername: Expression<String>
    private lateinit var emailAuthPassword: Expression<String>
    private var line = -1

    companion object { init {
        Skript.registerEffect(
            EffTransmitEmail::class.java,
            "(send|transmit|post) email %email%",
            "(send|transmit|post) email %email% using auth username %string% and password %string%"
        )
    } }

}