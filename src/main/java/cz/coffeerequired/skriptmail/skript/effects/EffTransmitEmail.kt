package cz.coffeerequired.skriptmail.skript.effects

import ch.njol.skript.Skript
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Examples
import ch.njol.skript.doc.Name
import ch.njol.skript.doc.Since
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.Config
import cz.coffeerequired.skriptmail.api.email.Account
import cz.coffeerequired.skriptmail.api.email.Email
import cz.coffeerequired.skriptmail.api.email.EmailController
import org.bukkit.event.Event
import java.util.*
import kotlin.time.TimeSource
import kotlin.time.measureTime

@Name("Send Email")
@Description("That will send the email to the recipients in asynchronous mode..")
@Examples("""
    set {_email} to new email using account "example"
    set {_email}'s recipients to "test22@gmail.com"
    #set {_email}'s content to "This is test Email"
    set {_email}'s subject to "Another email"
    
    # send email with additional auth username, password, there's that possibility because if, for example, you don't set
    # ...auth-credentials:
    #   username: <username|email|userid> # depends on service provider
    #   password: <password|token|outh2> # depends on service provider
    # So you can use this method
    send email {_email} using auth username "...." and password "...."
    
    # send email without additional auth, there's that possibility because if you have set the '...auth-credentials' in your 'config.yml'
    send email {_email}
""")
@Since("1.0")
class EffTransmitEmail: Effect() {

    override fun execute(event: Event?) {
        val email = this.emailExpr.getSingle(event)
        if (email != null) {
            val (field, recipient, subject, content) = email
            val form: EmailController.Companion.Form = EmailController.Companion.Form.formBuilder()
                .content(content)
                .recipient(recipient)
                .subject(subject)
                .build()
            form.usingTemplate = email.hasTemplate
            val controller: EmailController

            var authUsername: String? = null
            var authPassword: String? = null

            if (line == 1 && field.auth == true) {
                authUsername = this.emailAuthUsername.getSingle(event)
                authPassword = this.emailAuthPassword.getSingle(event)
            }

            try {
                controller = EmailController(field, authUsername, authPassword)
                controller.setForm(form)
                controller.send()
                Config.executedEmails[Date()] = email
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
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
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

    companion object {
        init {
        Skript.registerEffect(
            EffTransmitEmail::class.java,
            "(send|transmit|post) email %email%",
            "(send|transmit|post) email %email% using auth username %string% and password %string%"
        )
    } }
}