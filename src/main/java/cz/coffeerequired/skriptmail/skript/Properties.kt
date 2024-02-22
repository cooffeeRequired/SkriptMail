package cz.coffeerequired.skriptmail.skript

import ch.njol.skript.classes.Changer.ChangeMode
import ch.njol.skript.doc.Name
import ch.njol.skript.expressions.base.PropertyExpression
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import ch.njol.util.coll.CollectionUtils
import cz.coffeerequired.skriptmail.api.email.Account
import cz.coffeerequired.skriptmail.api.email.Email
import org.bukkit.event.Event
import java.util.*

@Name("Property of Config accounts")
class PropsConfiguredEmail : PropertyExpression<Account, Any>() {
    companion object { init { register(PropsConfiguredEmail::class.java, Any::class.java, "[email] (:id|:address|:host|:port|:auth|:starttls|:type)", "emailaccounts") }}
    override fun get(event: Event, source: Array<Account?>): Array<Any?> {
        return when (tag) {
            "id" -> source.map { it -> it!!.component7() }.toTypedArray()
            "address" -> source.map { it -> it!!.component1() }.toTypedArray()
            "type" -> source.map { it -> it!!.component2()!!.value }.toTypedArray()
            "host" -> source.map { it -> it!!.component3() }.toTypedArray()
            "port" -> source.map { it -> it!!.component4() }.toTypedArray()
            "auth" -> source.map { it -> it!!.component5() }.toTypedArray()
            "starttls" -> source.map { it -> it!!.component6() }.toTypedArray()
            else -> arrayOfNulls(0)
        }
    }

    private var tag: String = ""
    override fun isSingle(): Boolean { return true }
    override fun getReturnType(): Class<out Any> { return Any::class.java }

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>>?,
        matchedPattern: Int,
        isDelayed: Kleenean?,
        parseResult: SkriptParser.ParseResult?
    ): Boolean {
        expr = expressions?.get(0) as Expression<Account>
        tag = parseResult!!.tags[0]
        return true
    }

    override fun toString(event: Event?, debug: Boolean): String {
        val email = expr.toString(event, debug)
        return when (tag) {
            "id" -> "id of %s".format(email)
            "address" -> "address of %s".format(email)
            "type" -> "type of %s".format(email)
            "host" -> "host of %s".format(email)
            "port" -> "port of %s".format(email)
            "auth" -> "auth of %s".format(email)
            "starttls" -> "starttls of %s".format(email)
            else -> email
        }
    }
}

@Name("Property of Email")
class PropsEmail : PropertyExpression<Email, Any>() {
    private var tag: String = ""
    override fun getReturnType(): Class<out Any> { return Any::class.java }
    override fun get(event: Event?, source: Array<out Email>?): Array<Any?> {
        return when(tag) {
            "recipient" -> source?.map { it.recipient }!!.toTypedArray()
            "subject" -> source?.map { it.subject }!!.toTypedArray()
            "content", "template" -> source?.map { it.content }!!.toTypedArray()
            else -> arrayOfNulls(0)
        }
    }

    override fun toString(event: Event?, debug: Boolean): String {
        val o = expr.toString(event, debug)
        return "new email form %s".format(o)
    }

    @Suppress("UNCHECKED_CAST")

    override fun init(
        expressions: Array<out Expression<*>>?,
        matchedPattern: Int,
        isDelayed: Kleenean?,
        parseResult: SkriptParser.ParseResult?
    ): Boolean {
        expr = expressions?.get(0) as Expression<Email>
        tag = parseResult!!.tags[0]
        return true
    }

    override fun isSingle(): Boolean {
        return false
    }

    override fun acceptChange(mode: ChangeMode): Array<Class<*>> {
        return when (tag) {
            "recipient" -> {
                when (mode) {
                    ChangeMode.SET -> CollectionUtils.array(Array<String>::class.java)
                    ChangeMode.ADD -> CollectionUtils.array(Array<String>::class.java, String::class.java)
                    ChangeMode.RESET -> CollectionUtils.array(Any::class.java)
                    else -> CollectionUtils.array()
                }
            }
            "content" -> {
                when (mode) {
                    ChangeMode.SET -> CollectionUtils.array(String::class.java)
                    ChangeMode.RESET -> CollectionUtils.array(Any::class.java)
                    else -> CollectionUtils.array()
                }
            }
            "subject" -> {
                when (mode) {
                    ChangeMode.SET -> CollectionUtils.array(String::class.java)
                    ChangeMode.RESET -> CollectionUtils.array(Any::class.java)
                    else -> CollectionUtils.array()
                }
            }
            "template" -> {
                when (mode) {
                    ChangeMode.SET -> CollectionUtils.array(String::class.java, Any::class::class.java)
                    ChangeMode.RESET -> CollectionUtils.array(Any::class.java)
                    else -> CollectionUtils.array()
                }
            }
            else -> CollectionUtils.array()
        }
    }

    override fun change(event: Event?, delta: Array<out Any?>?, mode: ChangeMode?) {
        val o = expr.getSingle(event)
        assert(o != null)
        return when (tag) {
            "recipient" -> {
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
            "subject" -> {
                when (mode) {
                    ChangeMode.SET -> { for (d in delta!!.iterator()) { o?.subject = d as String } }
                    ChangeMode.RESET -> { o?.subject = "" }
                    else -> {}
                }
            }
            "content" -> {
                when (mode) {
                    ChangeMode.SET -> { for (d in delta!!.iterator()) { o?.content = d as String } }
                    ChangeMode.RESET -> { o?.content = "" }
                    else -> {}
                }
            }
            "template" -> {
                when (mode) {
                    ChangeMode.SET -> { for (d in delta!!.iterator()) { o!!.sContent(d as String) } }
                    ChangeMode.RESET -> { o?.content = "" }
                    else -> {}
                }
            }
            else -> {}
        }
    }

    companion object { init {
        register(PropsEmail::class.java, Any::class.java, "[email] (:recipient[s]|:subject|:content|:template)", "emails")
    }}

}