@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package cz.coffeerequired.skriptmail.skript

import ch.njol.skript.Skript
import ch.njol.skript.classes.Changer.ChangeMode
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Examples
import ch.njol.skript.doc.Name
import ch.njol.skript.doc.Since
import ch.njol.skript.expressions.base.PropertyExpression
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import ch.njol.util.coll.CollectionUtils
import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.EmailFieldType
import cz.coffeerequired.skriptmail.api.email.Account
import cz.coffeerequired.skriptmail.api.email.BukkitEmailMessageEvent
import cz.coffeerequired.skriptmail.api.email.Email
import cz.coffeerequired.skriptmail.api.email.EmailAddress
import org.bukkit.event.Event
import java.util.*

@Name("Name or Email from EmailAddress email-(recipient/sender)")
@Since("1.0")
@Description("Get email or name from email-(recipient/sender) in the 'on email receive event'")
@Examples("""
    on email receive:
        broadcast event-recipients's name
        broadcast event-sender's name
        broadcast event-recipients's email
        broadcast event-sender's email
""")
class PropsEmailAddress : PropertyExpression<EmailAddress, String>() {
    companion object { init { register(PropsEmailAddress::class.java, String::class.java, "(1:email|2:name)", "emailaddresss") }}

    private var mark = 0

    override fun toString(event: Event?, debug: Boolean): String {
        return "${if(mark == 1) "email" else "name"} of ${expr.toString(event, debug)}"
    }
    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        if (!parser.isCurrentEvent(BukkitEmailMessageEvent::class.java)) {
            Skript.error("Those expression can be used only with 'on received email'")
            return false
        }
        expr = expressions?.get(0) as Expression<EmailAddress>; return true
    }
    override fun getReturnType(): Class<out String> { return String::class.java }
    override fun get(event: Event?, source: Array<out EmailAddress>?): Array<String?> {
        return when (mark) {
            1 -> source!!.map { it.email }.toTypedArray()
            2 -> source!!.map { it.name }.toTypedArray()
            else -> arrayOfNulls(0)
        }
    }

}

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
class PropsEmailAccountPort : PropertyExpression<Account, Integer>() {
    companion object { init { register(PropsEmailAccountPort::class.java, Integer::class.java, "[account] port", "emailaccounts") }}
    override fun toString(event: Event?, debug: Boolean): String { return "port of email ${expr.toString(event, debug)}" }
    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        expr = expressions?.get(0) as Expression<Account>; return true
    }
    override fun getReturnType(): Class<out Integer> { return Integer::class.java }
    override fun get(event: Event?, source: Array<out Account>?): Array<Integer?> { return source?.map { it.port!!.toInt() as Integer }?.toTypedArray() ?: arrayOfNulls(0) }
}

@Name("Authentication of Account")
@Since("1.0")
@Description("Get authentication of Account what is saved in your accounts section in 'config.yml'", "Allow to enable either disable authentication for service server")
@Examples("""
    set {_account} to configured email account "Test"
    send {_account}'s auth
    send auth of {_account}
""")
class PropsEmailAccountAuth : PropertyExpression<Account, java.lang.Boolean>() {
    companion object { init { register(PropsEmailAccountAuth::class.java, java.lang.Boolean::class.java, "[account] auth[entication]", "emailaccounts") }}
    override fun toString(event: Event?, debug: Boolean): String { return "authentication of email ${expr.toString(event, debug)}" }
    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        expr = expressions?.get(0) as Expression<Account>; return true
    }
    override fun getReturnType(): Class<out java.lang.Boolean> { return java.lang.Boolean::class.java }
    override fun get(event: Event?, source: Array<out Account>?): Array<java.lang.Boolean?> { return source?.map { it.auth as java.lang.Boolean }?.toTypedArray() ?: arrayOfNulls(0) }
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
class PropsEmailAccountStartTLS : PropertyExpression<Account, java.lang.Boolean>() {
    companion object { init { register(PropsEmailAccountStartTLS::class.java, java.lang.Boolean::class.java, "[account] starttls", "emailaccounts") }}
    override fun toString(event: Event?, debug: Boolean): String { return "starttls of email ${expr.toString(event, debug)}" }
    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        expr = expressions?.get(0) as Expression<Account>; return true
    }
    override fun getReturnType(): Class<out java.lang.Boolean?> {
        return java.lang.Boolean::class.java
    }
    override fun get(event: Event?, source: Array<out Account>?): Array<java.lang.Boolean?> { return source?.map { it.starttls as java.lang.Boolean }?.toTypedArray() ?: arrayOfNulls(0) }
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

@Name("Recipient/s of Email")
@Since("1.0")
@Description("You can set/get/reset/add the recipient to the email form")
@Examples("""
    set {_email} to new email using account "example"
    set {_email}'s recipients to "test2@seznam.cz"
    add "test@gmail.com" to recipients of {_email}
    set {_recipients::*} to recipients of {_email}
    reset recipients of {_email}
""")
class PropsEmailRecipient : PropertyExpression<Email, String>() {
    companion object { init { register(PropsEmailRecipient::class.java, String::class.java, "[email] recipient[s]", "emails") }}
    override fun toString(event: Event?, debug: Boolean): String { return "email recipients of ${expr.toString(event, debug)}"}
    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        expr = expressions?.get(0) as Expression<Email>; return true
    }
    override fun getReturnType(): Class<out String> { return String::class.java }
    override fun get(event: Event?, source: Array<out Email>?): Array<String> {
        return source?.map { it.recipient!!.joinToString(",") }!!.toTypedArray()
    }
    override fun acceptChange(mode: ChangeMode): Array<Class<*>> {
        return when (mode) {
            ChangeMode.SET -> CollectionUtils.array(Array<String>::class.java)
            ChangeMode.ADD -> CollectionUtils.array(Array<String>::class.java, String::class.java)
            ChangeMode.RESET -> CollectionUtils.array(Any::class.java)
            else -> CollectionUtils.array()
        }
    }
    override fun change(event: Event?, delta: Array<out Any?>?, mode: ChangeMode?) {
        val o = expr.getSingle(event)
        assert(o != null)
        when (mode) {
            ChangeMode.ADD -> { for (d in delta!!.iterator()) { o?.recipient!!.add(d as String) } }
            ChangeMode.SET -> {
                val s = mutableListOf<String>()
                for (d in delta!!.iterator()) { s.add(d as String) }
                o?.recipient = s
            }
            ChangeMode.RESET -> { o?.recipient = mutableListOf() }
            else -> {}
        }
    }
}

@Name("Subject of Email")
@Since("1.0")
@Description("You can set/get/reset subject to the email form")
@Examples("""
    set {_email} to new email using account "example"
    set {_email}'s subject to "Another mail"
    set {_subject} to subject of {_email}
    reset subject of {_email}
""")
class PropsEmailSubject : PropertyExpression<Email, String>() {
    companion object { init { register(PropsEmailSubject::class.java, String::class.java, "[email] subject", "emails") }}
    override fun toString(event: Event?, debug: Boolean): String { return "email subject of ${expr.toString(event, debug)}"}
    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        expr = expressions?.get(0) as Expression<Email>; return true
    }
    override fun getReturnType(): Class<out String> { return String::class.java }
    override fun get(event: Event?, source: Array<out Email>?): Array<String?> { return source?.map { it.subject }!!.toTypedArray() }
    override fun acceptChange(mode: ChangeMode): Array<Class<*>> {
        return when (mode) {
            ChangeMode.SET -> CollectionUtils.array(String::class.java)
            ChangeMode.RESET -> CollectionUtils.array(Any::class.java)
            else -> CollectionUtils.array()
        }
    }
    override fun change(event: Event?, delta: Array<out Any?>?, mode: ChangeMode?) {
        val o = expr.getSingle(event)
        assert(o != null)
        when (mode) {
            ChangeMode.SET -> { for (d in delta!!.iterator()) { o?.subject = d as String } }
            ChangeMode.RESET -> { o?.subject = "" }
            else -> {}
        }
    }
}


@Name("Template/Body of Email")
@Since("1.0")
@Description("You can set/get/reset body/template to the email form")
@Examples("""
    set {_email} to new email using account "example"
    
    # using a content without rendering the data
    set {_email}'s body to "22"
    
    # using template without rendering data
    set {_email}'s template to (email template "main")
    
    # using template with rendering data, for e.g. if you have template and then you have `{it::title}, {it::name}`
    # that will be replaced to corresponded variable values e.g. "title = Welcome on Something", "name = Jorge"
    set {_data::title} to "Welcome on Something"
    set {_data::name} to "Jorge"
    
    set {_email}'s template to (email template "main" with {_data::*})
    
    set {_subject} to body of {_email}
    reset body of {_email}
""")
class PropsEmailTemplate : PropertyExpression<Email, String>() {

    private var isTemplate: Boolean = false
    companion object { init { register(PropsEmailTemplate::class.java, String::class.java, "[email] (:template[s]|body)", "emails") }}
    override fun toString(event: Event?, debug: Boolean): String { return "email template/body of ${expr.toString(event, debug)}"}
    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        expr = expressions?.get(0) as Expression<Email>; isTemplate = parseResult!!.hasTag("template"); return true
    }
    override fun getReturnType(): Class<out String> { return String::class.java }
    override fun get(event: Event?, source: Array<out Email>?): Array<String?> { return source?.map { it.content }!!.toTypedArray() }
    override fun acceptChange(mode: ChangeMode): Array<Class<*>> {
        return when (mode) {
            ChangeMode.SET -> CollectionUtils.array(String::class.java, Any::class.java)
            ChangeMode.RESET -> CollectionUtils.array(Any::class.java)
            else -> CollectionUtils.array()
        }
    }
    override fun change(event: Event?, delta: Array<out Any?>?, mode: ChangeMode?) {
        val o = expr.getSingle(event)
        assert(o != null)
        when (mode) {
            ChangeMode.SET -> {
                for (d in delta!!.iterator()) {
                    o?.content = d as String
                    if (!isTemplate && o!!.hasTemplate) {
                        SkriptMail.logger().warn("Template is set! You can't change the body of email like that, use 'reset body of %email%'")
                        return
                    }
                    if (isTemplate) o!!.hasTemplate = true
                }
            }
            ChangeMode.RESET -> { o?.content = "" }
            ChangeMode.DELETE -> {}
            else -> {}
        }
    }
}