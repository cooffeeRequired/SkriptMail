package cz.coffeerequired.skriptmail.skript.expressions

import ch.njol.skript.classes.Changer.*
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
import cz.coffeerequired.skriptmail.api.ConfigFields.PROJECT_DEBUG
import cz.coffeerequired.skriptmail.api.email.Email
import org.bukkit.event.Event


@Name("Template/Body of Email")
@Since("1.0")
@Description("You can set/get/reset body/template to the email form")
@Examples("""
    set {_email} to new email using {_account}
    
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
class ExprProperties : PropertyExpression<Email, String>() {

    private var isTemplate: Boolean = false
    companion object { init {
        register(ExprProperties::class.java, String::class.java, "[email] (:template[s]|body)", "emails")
    }}
    override fun toString(event: Event?, debug: Boolean): String = "email template/body of ${expr.toString(event, debug)}"
    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?): Boolean {
        expr = expressions?.get(0) as Expression<Email>; isTemplate = parseResult!!.hasTag("template"); return true
    }
    override fun getReturnType(): Class<out String> = String::class.java
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
        if (o == null) {
            if(PROJECT_DEBUG) SkriptMail.logger().debug("The variable 'o' cannot be null $0 $event $expr")
            SkriptMail.logger().errorWithNode("The 'o' cannot be null", node = parser.node)
            return
        }
        when (mode) {
            ChangeMode.SET -> {
                for (d in delta!!.iterator()) {
                    o.content =  d as String
                    if (!isTemplate && o.hasTemplate) {
                        if (PROJECT_DEBUG) SkriptMail.logger().debug("Template is set! You can't change the body of email like that, use 'reset body of %email%' $0 $isTemplate $1 $o")
                        SkriptMail.logger().warn("Template is set! You can't change the body of email like that, use 'reset body of %email%'")
                        return
                    }
                    if (isTemplate) o.hasTemplate = true
                }
            }
            ChangeMode.RESET -> o.content = ""
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
        return source?.map { it.recipients!!.joinToString(",") }!!.toTypedArray()
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
            ChangeMode.ADD -> { for (d in delta!!.iterator()) { o?.recipients!!.add(d as String) } }
            ChangeMode.SET -> {
                val s = mutableListOf<String>()
                for (d in delta!!.iterator()) { s.add(d as String) }
                o?.recipients = s
            }
            ChangeMode.RESET -> { o?.recipients = mutableListOf() }
            else -> {}
        }
    }
}