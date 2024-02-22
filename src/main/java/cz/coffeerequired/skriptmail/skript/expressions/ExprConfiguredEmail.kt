package cz.coffeerequired.skriptmail.skript.expressions

import ch.njol.skript.Skript
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.util.Kleenean
import cz.coffeerequired.skriptmail.api.ConfigFields
import cz.coffeerequired.skriptmail.api.email.Account
import org.bukkit.event.Event

@Suppress("UNUSED")
class ExprConfiguredEmail : SimpleExpression<Account>() {
    private lateinit var id: Expression<String>
    private var line: Int = -1

    override fun get(event: Event): Array<Account?> {
        if (line == 0) {
            if (ConfigFields.ACCOUNTS.isEmpty()) return arrayOfNulls(0)
            return ConfigFields.ACCOUNTS.toTypedArray()
        } else {

        }
        return arrayOfNulls(0)
    }

    override fun isSingle(): Boolean {
        return (line == 0)
    }

    override fun getReturnType(): Class<out Account> {
        return Account::class.java
    }

    override fun toString(event: Event?, debug: Boolean): String {
        return if (line == 0) "configured emails accounts" else "configured email %s account".format(id.toString(event, debug))
    }

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<Expression<*>?>?,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        this.line = matchedPattern
        if (line == 1) {
            this.id = expressions?.get(0) as Expression<String>
        }
        return true
    }

    companion object {
        init {
            Skript.registerExpression(ExprConfiguredEmail::class.java, Account::class.java, ExpressionType.COMBINED,
                "configured emails accounts",
                "configured email [account] %string%"
            )
        }
    }
}
