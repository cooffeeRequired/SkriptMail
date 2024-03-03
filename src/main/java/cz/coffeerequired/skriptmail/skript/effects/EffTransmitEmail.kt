package cz.coffeerequired.skriptmail.skript.effects

import ch.njol.skript.Skript
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Examples
import ch.njol.skript.doc.Name
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.ConfigFields.PROJECT_DEBUG
import cz.coffeerequired.skriptmail.api.email.Email
import cz.coffeerequired.skriptmail.api.email.EmailService
import cz.coffeerequired.skriptmail.api.email.EmailServiceProvider.Companion.pairedAccountServices
import cz.coffeerequired.skriptmail.api.email.EmailServiceProvider.Companion.registeredServices
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.event.Event


@Name("Send email")
@Description("That will send the email to the recipients in asynchronous mode.")
@Examples(
"""
    
""")
class EffTransmitEmail : Effect() {

    companion object { init {
        Skript.registerEffect(
            EffTransmitEmail::class.java,
            "(transmit|post) email %email%",
            "(transmit|post) email %email% using auth[entication] cred(s|entials) %string% and pass[word|code] %string%"
        )
    }}

    private lateinit var emailExpr: Expression<Email>
    private var authUsername: Expression<String> ? = null
    private var authPassword: Expression<String> ? = null
    private var line = -1

    override fun toString(event: Event?, debug: Boolean): String {
        return "transmit email ${emailExpr.toString(event, debug)}"
    }

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>>?,
        matchedPattern: Int,
        isDelayed: Kleenean?,
        parseResult: SkriptParser.ParseResult?
    ): Boolean {
        emailExpr = expressions!![0] as Expression<Email>
        line = matchedPattern
        if (line == 1) {
            authUsername = expressions[1] as Expression<String>
            authPassword = expressions[2] as Expression<String>
        }
        return true
    }

    override fun execute(event: Event?) {
        val email = emailExpr.getSingle(event)
        try {
            runBlocking {
                if (email?.isRegistered == true) {
                    val service = registeredServices[email.account.id]
                    if (PROJECT_DEBUG) {
                        SkriptMail.logger().debug("Service id was fetch $0 $service")
                        SkriptMail.logger().debug("Email id $email")
                    }
                    if (service != null) {
                        launch { service.sendEmail(*email.recipients!!.toTypedArray(), content = email.content ?: "", subject = email.subject ?: "") }.join()
                    } else {
                        SkriptMail.logger().error("Service cannot be null!")
                        if (PROJECT_DEBUG) {
                            SkriptMail.logger().debug("Service cannot be null $email")
                            return@runBlocking
                        }
                    }
                } else {
                    var service: EmailService? = null
                    val id = pairedAccountServices[email?.account?.id]
                    if (id != null) { service = registeredServices[id] }

                    var pass: String? = null
                    var username: String? = null
                    if (email!!.account.authUsername == null && email.account.authPassword == null) {
                        pass = authPassword?.getSingle(event)
                        username = authUsername?.getSingle(event)
                    }
                    if (service != null) {
                        launch { service.sendEmail(*email.recipients!!.toTypedArray(), content = email.content ?: "", subject = email.subject ?: "") }.join()
                    } else {
                        EmailService.sendAnonymous(email, email.account.authUsername ?: username, email.account.authPassword ?: pass)
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}