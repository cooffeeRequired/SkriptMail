package cz.coffeerequired.skriptmail.skript.expressions

import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Examples
import ch.njol.skript.doc.Name
import ch.njol.skript.doc.Since
import ch.njol.skript.expressions.base.PropertyExpression
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import cz.coffeerequired.skriptmail.api.EmailFieldType
import cz.coffeerequired.skriptmail.api.email.Account
import org.bukkit.event.Event

@Name("Id of Account")
@Since("1.0")
@Description("Get id of Account what is saved in your accounts section in 'config.yml'")
@Examples("""
    set {_account} to configured email account "Test"
    send {_account}'s id
    send id of {_account}
""")
class PropsEmailAccountId : PropertyExpression<Account, String>() {
    companion object { init { register(PropsEmailAccountId::class.java, String::class.java, "[account] id", "emailaccounts") }}
    override fun toString(event: Event?, debug: Boolean): String { return "id of email ${expr.toString(event, debug)}" }
    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        expr = expressions?.get(0) as Expression<Account>; return true
    }
    override fun getReturnType(): Class<out String> { return String::class.java }
    override fun get(event: Event?, source: Array<out Account>?): Array<String?> { return source?.map { it.id }?.toTypedArray() ?: arrayOfNulls(0) }
}

@Name("Address of Account")
@Since("1.0")
@Description("Get address of Account what is saved in your accounts section in 'config.yml'", "the address under which the email will be sent")
@Examples("""
    set {_account} to configured email account "Test"
    send {_account}'s address
    send address of {_account}
""")
class PropsEmailAccountAddress : PropertyExpression<Account, String>() {
    companion object { init { register(PropsEmailAccountAddress::class.java, String::class.java, "[account] address", "emailaccounts") }}
    override fun toString(event: Event?, debug: Boolean): String { return "address of email ${expr.toString(event, debug)}" }
    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        expr = expressions?.get(0) as Expression<Account>; return true
    }
    override fun getReturnType(): Class<out String> { return String::class.java }
    override fun get(event: Event?, source: Array<out Account>?): Array<String?> { return source?.map { it.address }?.toTypedArray() ?: arrayOfNulls(0) }
}

@Name("Host of Account")
@Since("1.0")
@Description("Get host of Account what is saved in your accounts section in 'config.yml'", "host service through which it will be sent for example 'smtp.gmail.com'")
@Examples("""
    set {_account} to configured email account "Test"
    send {_account}'s host
    send host of {_account}
""")
class PropsEmailAccountHost : PropertyExpression<Account, String>() {
    companion object { init { register(PropsEmailAccountHost::class.java, String::class.java, "[account] host", "emailaccounts") }}
    override fun toString(event: Event?, debug: Boolean): String { return "address of email ${expr.toString(event, debug)}" }
    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        expr = expressions?.get(0) as Expression<Account>; return true
    }
    override fun getReturnType(): Class<out String> { return String::class.java }
    override fun get(event: Event?, source: Array<out Account>?): Array<String?> { return source?.map { it.host }?.toTypedArray() ?: arrayOfNulls(0) }
}

@Name("Port of Account")
@Since("1.0")
@Description("Get port of Account what is saved in your accounts section in 'config.yml'")
@Examples("""
    set {_account} to configured email account "Test"
    send {_account}'s port
    send port of {_account}
""")
class PropsEmailAccountPort : PropertyExpression<Account, Any>() {
    companion object { init { register(PropsEmailAccountPort::class.java, Any::class.java, "[account] port", "emailaccounts") }}
    override fun toString(event: Event?, debug: Boolean): String { return "port of email ${expr.toString(event, debug)}" }
    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        expr = expressions?.get(0) as Expression<Account>; return true
    }
    override fun getReturnType(): Class<out Any> { return Integer::class.java }
    override fun get(event: Event?, source: Array<out Account>?): Array<Any?> { return source?.map { it.port!!.toInt() }?.toTypedArray() ?: arrayOfNulls(0) }
}

@Name("Authentication of Account")
@Since("1.0")
@Description("Get authentication of Account what is saved in your accounts section in 'config.yml'", "Allow to enable either disable authentication for service server")
@Examples("""
    set {_account} to configured email account "Test"
    send {_account}'s auth
    send auth of {_account}
""")
class PropsEmailAccountAuth : PropertyExpression<Account, Any>() {
    companion object { init { register(PropsEmailAccountAuth::class.java, Any::class.java, "[account] auth[entication]", "emailaccounts") }}
    override fun toString(event: Event?, debug: Boolean): String { return "authentication of email ${expr.toString(event, debug)}" }
    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        expr = expressions?.get(0) as Expression<Account>; return true
    }
    override fun getReturnType(): Class<out Any> { return java.lang.Boolean::class.java }
    override fun get(event: Event?, source: Array<out Account>?): Array<Any?> { return source?.map { it.auth as Any }?.toTypedArray() ?: arrayOfNulls(0) }
}

@Name("Starttls fields of Account")
@Since("1.0")
@Description(
    "Get Starttls fields of Account of Account what is saved in your accounts section in 'config.yml'",
    "Its allowed you enable or either disable TLS/SSL security flag"
)
@Examples("""
    set {_account} to configured email account "Test"
    send {_account}'s starttls
    send starttls of {_account}
""")
class PropsEmailAccountStartTLS : PropertyExpression<Account, Any>() {
    companion object { init { register(PropsEmailAccountStartTLS::class.java, Any::class.java, "[account] starttls", "emailaccounts") }}
    override fun toString(event: Event?, debug: Boolean): String { return "starttls of email ${expr.toString(event, debug)}" }
    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        expr = expressions?.get(0) as Expression<Account>; return true
    }
    override fun getReturnType(): Class<out Any?> {
        return java.lang.Boolean::class.java
    }
    override fun get(event: Event?, source: Array<out Account>?): Array<Any?> { return source?.map { it.starttls as Any }?.toTypedArray() ?: arrayOfNulls(0) }
}

@Name("type of Account service")
@Since("1.0")
@Description(
    "Get type field of Account what is saved in your accounts section in 'config.yml'",
    "SMTP / POP3(Not implemented yet) / IMAP(Not implemented yet)"
)
@Examples("""
    set {_account} to configured email account "Test"
    send {_account}'s service type
    send service type of {_account}
""")
class PropsEmailAccountType : PropertyExpression<Account, EmailFieldType>() {
    companion object { init { register(PropsEmailAccountType::class.java, EmailFieldType::class.java, "[account] service type", "emailaccounts") }}
    override fun toString(event: Event?, debug: Boolean): String { return "type of email service ${expr.toString(event, debug)}" }
    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        expr = expressions?.get(0) as Expression<Account>; return true
    }
    override fun getReturnType(): Class<out EmailFieldType> { return EmailFieldType::class.java }
    override fun get(event: Event?, source: Array<out Account>?): Array<EmailFieldType?> { return source?.map { it.type }?.toTypedArray() ?: arrayOfNulls(0) }
}