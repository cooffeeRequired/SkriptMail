package cz.coffeerequired.skriptmail.skript.expressions

import ch.njol.skript.Skript
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.util.Kleenean
import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.EmailFieldType
import cz.coffeerequired.skriptmail.api.email.Account
import cz.coffeerequired.skriptmail.api.email.Email
import cz.coffeerequired.skriptmail.api.tryGetById
import org.bukkit.event.Event

@Suppress("UNUSED")
class ExprNewEmail : SimpleExpression<Email>() {

    private var line: Int = -1
    private lateinit var configurationString: Expression<String>
    private lateinit var configurationEmail: Expression<String>

    override fun get(event: Event?): Array<Email?> {
        val userInput: String? = configurationString.getSingle(event)
        return if (userInput != null) {
            when (line) {
                0 -> {
                    val result: MutableMap<Email.Companion.Tokens, String> = Email.tokenize(userInput)
                    try {
                        if (result.size >= 3) {
                            val address = this.configurationEmail.getSingle(event)
                            val service = EmailFieldType.valueOf((result[Email.Companion.Tokens.SERVICE] as String).uppercase())
                            val host = result[Email.Companion.Tokens.HOST]
                            val port = (result[Email.Companion.Tokens.PORT] as String).toLong()
                            val auth = result[Email.Companion.Tokens.OPTAUTH].toBoolean()
                            val starttls = result[Email.Companion.Tokens.OPTSTARTTLS].toBoolean()
                            val account = Account(address, service, host, port, auth = auth, starttls = starttls, null, null, null )
                            val email = Email(account, null, null, null)
                            return arrayOf(email)
                        } else {
                            SkriptMail.gLogger().warn("Expecting length is 3 but got %s, please check format of credentials string", result.size)
                            SkriptMail.gLogger().warn("Correct format is %s", "<service>:<host>:<port>@<auth=boolean optional>&<starttls=boolean optional>")
                        }
                    } catch (ex: Exception) {
                        if (ex.message!!.contains("No enum constant")) {
                            SkriptMail.gLogger().error("Cannot get service %s, allowed services are %s", result[Email.Companion.Tokens.SERVICE], EmailFieldType.entries.toTypedArray()
                                .contentToString())
                        } else if (ex.message!!.contains("null cannot be cast to non-null")) {
                            SkriptMail.gLogger().error("Creation of new Email failed!")
                            val filtered = result.filter { !it.key.toString().contains("opt", true) }
                            SkriptMail.gLogger().warn("Expecting length is 3 but got %s, please check format of credentials string", (filtered.size))
                            SkriptMail.gLogger().warn("Correct format is %s", "<service>:<host>:<port>@<auth=boolean optional>&<starttls=boolean optional>")
                        } else {
                            SkriptMail.gLogger().exception(ex, "Something goes wrong")
                        }
                    }
                    arrayOfNulls(0)
                }
                1 -> {
                    val account : Account? = tryGetById(userInput)
                    if (account != null) {
                        val email =  Email(account, null, null, null)
                        return arrayOf(email)
                    }
                    arrayOfNulls(0)
                }
                else -> arrayOfNulls(0)
            }
        } else {
            arrayOfNulls(0)
        }
    }

    override fun isSingle(): Boolean { return true }
    override fun getReturnType(): Class<out Email> { return Email::class.java }

    override fun toString(event: Event?, debug: Boolean): String {
        return if (line == 0) {
            "new email with credentials %s using %s".format(this.configurationString.toString(event, debug), this.configurationEmail.toString(event, debug))
        } else {
            "new email using account %s".format(this.configurationString.toString(event, debug))
        }
    }
    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>>?,
        matchedPattern: Int,
        isDelayed: Kleenean?,
        parseResult: SkriptParser.ParseResult?
    ): Boolean {
        this.line = matchedPattern
        this.configurationString = expressions!![0] as Expression<String>
        if (line == 0){
            this.configurationEmail = expressions[1] as Expression<String>
        }
        return true
    }

    companion object { init {
        Skript.registerExpression(ExprNewEmail::class.java, Email::class.java, ExpressionType.COMBINED,
            "new email with credentials %string% using %string%",
            "new email using [configuration|account] %string%"
        )
    }}
}
