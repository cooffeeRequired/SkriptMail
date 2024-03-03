package cz.coffeerequired.skriptmail.skript.expressions

import ch.njol.skript.Skript
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Examples
import ch.njol.skript.doc.Name
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.skript.util.LiteralUtils
import ch.njol.util.Kleenean
import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.email.*
import org.bukkit.event.Event
import cz.coffeerequired.skriptmail.api.tryGetById

@Name("Create/Make new email form")
@Description(
    "you can create an email form using multiple configurations",
    "1. using an account",
    "2. using an registered service",
    "3. using credential string",
    "4. using a predefined host"
)
@Examples("""
    set {_email} to new email using {_account}
    send {_email} to console

    set {_email} to new email using credential string "smtp:gmail.com:587@auth=true&starttls=true" with address "test@gmail.com"
    send {_email} to console

    set {_email} to new email using predefined service OUTLOOK with address "test2@gmail.com"
    send {_email} to console
    
    set {_account} to email account "using_predefined"
    register service with id "test" using {_account}
    
    set {_email} to new email using registered service "test"
    send {_email} to console
""")
class ExprEmail : SimpleExpression<Email>() {

    companion object { init {
        Skript.registerExpression(ExprEmail::class.java, Email::class.java, ExpressionType.COMBINED,
            "[new] email using [config[uration]|account] %string/emailaccount%",
            "[new] email using credential string %string% [(:with) [[custom] [email]] address %string%]",
            "[new] email using predefined service %emailservice% [(:with) [[custom] [email]] address %string%]",
            "[new] email using registered service %string%"
        )
    }}

    private var line: Int = -1
    private lateinit var exprInput: Expression<Any>
    private var exprAddress: Expression<String>? = null
    private var withCustomAddress: Boolean = false

    override fun toString(event: Event?, debug: Boolean): String {
        val str = when(line) {
            0 -> "configuration "
            1 -> "credential string "
            2 -> "predefined service "
            3 -> "registered service "
            else -> ""
        }

        return "email using $str ${exprInput.toString(event, debug)}"
    }

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>>?,
        matchedPattern: Int,
        isDelayed: Kleenean?,
        parseResult: SkriptParser.ParseResult?
    ): Boolean {
        line = matchedPattern
        if (line == 1 || line == 2) {exprAddress = expressions!![1] as Expression<String>; withCustomAddress = parseResult!!.hasTag("with")}
        exprInput = LiteralUtils.defendExpression(expressions!![0])
        return LiteralUtils.canInitSafely(exprInput)
    }

    override fun isSingle(): Boolean = true
    override fun getReturnType(): Class<out Email> = Email::class.java
    override fun get(event: Event?): Array<Email?> {
        val input = exprInput.getSingle(event)
        val output = mutableListOf<Email>()
        when (line) {
            0 -> {
                when (input) {
                    is Account -> output.add(Email(input, null, null, null))
                    is String -> output.add(tryGetById(input)?.let { Email(it, null, null, null) } as Email)
                }
            }
            1 -> {
                val tokens = Email.tokenize(input as String)
                val address = exprAddress?.getSingle(event)
                val account = Account.fromTokens(tokens, address = address ?: "skript-mail@non-reply.xyz")
                if (account != null) {
                    output.add(Email(account, null, null, null))
                } else {
                    SkriptMail.logger().error("Account cannot be null")
                }
            }
            2 -> {
                val address = exprAddress?.getSingle(event)
                val account = Account.fromService((input as EmailHosts).host, address = address ?: "skript-mail@non-reply.xyz", null)
                output.add(Email(account, null, null, null))
            }
            3 -> {
                val service = EmailServiceProvider.registeredServices[input]
                if (service?.host == null) {
                    SkriptMail.logger().error("Service for id $input doesn't exist'")
                    return arrayOf()
                }
                val account = Account.fromService(service.host, address = service.address, id = service.serviceId)
                output.add(Email(account, null, null, null, true))
            }
        }
        return output.toTypedArray()
    }
}