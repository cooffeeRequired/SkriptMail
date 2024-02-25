package cz.coffeerequired.skriptmail.skript.effects

import ch.njol.skript.Skript
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Examples
import ch.njol.skript.doc.Name
import ch.njol.skript.doc.Since
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.util.AsyncEffect
import ch.njol.util.Kleenean
import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.ConfigFields
import cz.coffeerequired.skriptmail.api.email.Account
import cz.coffeerequired.skriptmail.api.email.EmailService
import cz.coffeerequired.skriptmail.api.tryGetById
import org.bukkit.event.Event

@Name("Register new email service")
@Description("Try register new email service for given Account or either Account id string from your configuration 'config.yml'")
@Since("1.0")
@Examples("""
    on script load:
         register new service with id "test" and using account configured email account "example"
         register new service with id "test" and using account "example"
""")
class EffRegisterService : AsyncEffect() {

    private lateinit var idExpr: Expression<String>
    private lateinit var accExpr: Expression<Any>

    override fun execute(event: Event?) {

        val id = idExpr.getSingle(event)
        val acc: Any? = accExpr.getSingle(event)

        if (acc != null) {
            when (acc) {
                is String -> {
                    if (ConfigFields.ACCOUNTS.isEmpty()) { SkriptMail.logger().warn("&cYou did not have set any account at all.");return }
                    when (val foundAcc = tryGetById(acc)) {
                        null -> SkriptMail.logger().warn("&cAccount for id '$acc' will not found!")
                        else -> id?.let { EmailService.tryRegisterNewService(foundAcc, it) }
                    }
                }
                is Account -> {
                    if (id != null) { EmailService.tryRegisterNewService(acc, id) }
                }
            }
        }
    }

    override fun toString(event: Event?, debug: Boolean): String {
        return "register new service with id ${idExpr.toString(event, debug)} and using account ${accExpr.toString(event, debug)}"
    }

    @Suppress("unchecked_cast")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        idExpr = expressions!![0] as Expression<String>
        accExpr = expressions[1] as Expression<Any>
        return true
    }

    companion object {
        init {
        Skript.registerEffect(EffRegisterService::class.java, "register [new] service with id %string% and using account %emailaccount/string%")
    }}
}

@Name("Unregister email service")
@Description("Try unregister email service for given Id")
@Since("1.0")
@Examples("""
    on script unload:
         unregister email service for id "test"
""")
class EffUnregisterService : AsyncEffect() {

    private lateinit var exprID: Expression<String>

    override fun execute(event: Event?) {
        val id = exprID.getSingle(event)
        if (id == null) {SkriptMail.logger().warn("&cId Cannot be null"); return}
        EmailService.unregisterService(id)
    }

    override fun toString(event: Event?, debug: Boolean): String {
        return "unregister email service for id ${this.exprID.toString(event, debug)}"
    }

    @Suppress("unchecked_cast")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        this.exprID = expressions!![0] as Expression<String>
        return true
    }

    companion object {init {
        Skript.registerEffect(EffUnregisterService::class.java, "unregister email service for id %string%")
    }}

}