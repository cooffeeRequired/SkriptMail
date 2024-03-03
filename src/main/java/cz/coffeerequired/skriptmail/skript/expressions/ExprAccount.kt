package cz.coffeerequired.skriptmail.skript.expressions

import ch.njol.skript.Skript
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.DocumentationId
import ch.njol.skript.doc.Name
import ch.njol.skript.doc.Since
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.util.Kleenean
import cz.coffeerequired.skriptmail.api.ConfigFields.ACCOUNTS
import cz.coffeerequired.skriptmail.api.email.Account
import cz.coffeerequired.skriptmail.api.tryGetById
import org.bukkit.event.Event

@DocumentationId("account")
@Name("Configured email account/s")
@Description(
"""
    # get all of them
    set {_accounts::*} to configured emails accounts
    # get single of them
    set {_account} to configured email account "id of your account"
    
    **Your config.yml**
    # accounts:
    #  example: -> id of the account
    #     ... accounts details
    #   example1:
    #     ... accounts details
""")
@Since("1.0")
class ExprAccount : SimpleExpression<Account>() {
    companion object {
        init {
            Skript.registerExpression(
                ExprAccount::class.java, Account::class.java, ExpressionType.COMBINED,
                "[configured] emails accounts",
                "[configured] email account %string%"
            )
        }
    }

    private var line: Int = -1
    private lateinit var id: Expression<String>


    @Suppress("UNCHECKED_CAST")
    /** Suppressed [expressions] checking */
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        line = matchedPattern
        if (line == 1) this.id = expressions!![0] as Expression<String>
        return true
    }
    override fun isSingle(): Boolean = line == 1
    override fun getReturnType(): Class<out Account> = Account::class.java
    override fun get(event: Event?): Array<Account?> {
        if (ACCOUNTS.isEmpty()) return arrayOf()
        if (line == 0) {
            return ACCOUNTS.toTypedArray()
        } else {
            val identifier = this.id.getSingle(event)
            return arrayOf(identifier?.let { tryGetById(it) })
        }
    }

    override fun toString(event: Event?, debug: Boolean): String = if (line == 0) "configured emails accounts" else "configured email account ${id.getSingle(event)}"
}