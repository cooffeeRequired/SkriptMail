package cz.coffeerequired.skriptmail.skript.effects

import ch.njol.skript.Skript
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Examples
import ch.njol.skript.doc.Name
import ch.njol.skript.doc.Since
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.util.AsyncEffect
import ch.njol.util.Kleenean
import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.email.Account
import cz.coffeerequired.skriptmail.api.email.EmailService
import cz.coffeerequired.skriptmail.api.email.EmailServiceProvider
import cz.coffeerequired.skriptmail.api.tryGetById
import org.bukkit.event.Event

@Name("Register new email service")
@Description("Try register new email service for given Account or either Account id string from your configuration 'config.yml'")
@Since("1.0")
@Examples("""
    on script load:
         register service with id "test" and using email account "example"
         register service with id "test" and using "example"
""")
class EffRegisterService : AsyncEffect(){

    companion object {
        init {
            Skript.registerEffect(EffRegisterService::class.java,
                "register service with id %string% using %emailaccount/string%"
            )
        }
    }

    private var isInstanceOfAccount: Boolean = false
    private lateinit var accountLike: Expression<Any>
    private lateinit var serviceId: Expression<String>

    override fun toString(event: Event?, debug: Boolean): String {
        return "register service with id %string% and using ${this.accountLike.toString(event, debug)}"
    }

    @Suppress("UNCHECKED_CAST")
    /** Suppressed [expressions] checking */
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        this.serviceId = expressions!![0] as Expression<String>
        this.accountLike = expressions[1] as Expression<Any>
        isInstanceOfAccount = this.accountLike.returnType != String::class.java
        return true
    }

    override fun execute(event: Event?) {
        var service: EmailService? = null
        val serviceID = this.serviceId.getSingle(event)
        when (val idOrAccount = this.accountLike.getSingle(event)) {
            is Account -> {
                if (idOrAccount.isCustom()) {
                    val (address, _, host, port, auth, startTLS, username, password, accountID) = idOrAccount
                    if (username != null && password != null) {
                        service = EmailServiceProvider.newEmailCustomService(serviceID to accountID, address, host, port, auth, startTLS, username, password)
                    }
                } else {
                    val (address, _, _, _, _, _, username, password, id, serv) = idOrAccount
                    if (serv != null && password != null) {
                        service = EmailServiceProvider.newEmailService(serviceID to id, address, serv, username, password)
                    }
                }
            }
            else -> {
                val account = tryGetById(idOrAccount.toString())
                if (account != null) {
                    val (address, _, host, port, auth, startTLS, username, password, id) = account
                    if (username != null && password != null) {
                        service = EmailServiceProvider.newEmailCustomService(serviceID to id, address, host, port, auth, startTLS, username, password)
                    }
                }
            }
        }
        service?.tryConnect ({
            val mailbox = it.registerMailbox()
            mailbox?.open()
        })
    }
}


@Name("Unregister email service")
@Description("Try unregister email service for given Id")
@Since("1.0")
@Examples("""
    on script unload:
         unregister email service for id "test"
""")

class EffUnregisterService : Effect() {
    companion object {
        init {
            Skript.registerEffect(EffUnregisterService::class.java, "unregister email service for id %string%")
        }
    }

    private lateinit var exprID: Expression<String>

    override fun toString(event: Event?, debug: Boolean): String {
        return "unregister email service for id ${this.exprID.toString(event, debug)}"
    }

    @Suppress("UNCHECKED_CAST")
    /** Suppressed [expressions] checking */
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        this.exprID = expressions!![0] as Expression<String>
        return true
    }

    override fun execute(event: Event?) {
        val identifier = this.exprID.getSingle(event)
        if (identifier != null) {
            EmailServiceProvider.unregisterService(identifier)
        } else {
            SkriptMail.logger().error("Identifier cannot be null!")
        }
    }
}