package cz.coffeerequired.skriptmail.skript.expressions

import ch.njol.skript.Skript
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Examples
import ch.njol.skript.doc.Name
import ch.njol.skript.doc.Since
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.Variable
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.skript.variables.Variables
import ch.njol.util.Kleenean
import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.ConfigFields
import cz.coffeerequired.skriptmail.api.tryGetContent
import org.bukkit.event.Event
import java.util.TreeMap
import java.util.regex.Pattern

@Name("Configured Email Templates")
@Description("You can get all of your pre-configured email templates or one of them")
@Examples("""
    set {_templates::*} to all email templates
    
    # without re-rendered variables in the template,
    set {_template} to email template "main"
    
    set {_d::hello} to "hello, world."
    set {_template} to email template "main" with objects {_d::*}
    # the variable in the template need to starts 'it::' e.g. like this `{it::*}` the star can be replaced by anything
    # for e.g. you define 'set {_d::hello} to "hello, world."' and then you have in the template '{it::hello}'
    # it's will be replaced by the reload value so "hello, world' during the parsing the html template as content of email.
""")
@Since("1.0")
class ExprTemplates : SimpleExpression<String>() {
    private fun getVariables(html: String): List<String> {
        val pattern = Pattern.compile("\\{(.*?)}")
        val matcher = pattern.matcher(html)
        val variables = mutableListOf<String>()
        while (matcher.find()) {
            variables.add("{${matcher.group(1)}}")
        }
        return variables
    }

    private fun parseData(html: String, isLocal: Boolean, varName: String, event: Event): String {
        var content = html
        val foundVars = getVariables(html)
        val vvName = varName.slice(0 until varName.length -3)
        if (foundVars.isNotEmpty()) {
            foundVars.forEach {
                val sanitized = it.slice(1 until it.length -1)
                val e = sanitized.slice(sanitized.indexOf("::") + 2 until sanitized.length)
                val finalized = Variables.getVariable("$vvName::$e", event, isLocal)
                if (finalized != null) {
                    content = if (finalized is TreeMap<*,*>) {
                        content.replace(it, "[Object]")
                    } else {
                        content.replace(it, finalized.toString())
                    }
                }
            }
        }
        return content
    }

    @Suppress("unchecked_cast")
    override fun get(event: Event): Array<String?> {
        return when (line) {
            0 -> ConfigFields.TEMPLATES.map { it.key.replace(".html", "") }.toTypedArray()
            1 -> {
                val id = this.templateID.getSingle(event)
                val template = id?.let { tryGetContent(it) }
                if (template != null) {
                    when (withData) {
                        true -> {
                            val variableName = this.templateData.name.getSingle(event)
                            val isLocal = this.templateData.isLocal
                            val map: TreeMap<String, Any>?
                            try {
                                map = Variables.getVariable(variableName, event, isLocal) as TreeMap<String, Any>?
                                if (map!!.isNotEmpty() && variableName != null) {
                                    val content = parseData(template, isLocal, variableName, event)
                                    return arrayOf(content)
                                }
                            } catch (ex: Exception) {
                                SkriptMail.logger().exception(ex, "Variable &n&e'{%s}'&r doesn't returns any usable value", variableName)
                            }
                        }
                        else -> {
                            return arrayOf(template)
                        }
                    }
                }
                arrayOfNulls(0)
            }
            else -> arrayOfNulls(0)
        }
    }

    override fun isSingle(): Boolean {
        return line == 1
    }

    override fun getReturnType(): Class<String> {
        return String::class.java
    }

    override fun toString(event: Event?, debug: Boolean): String {
        return when (line) {
            0 -> "all email templates"
            1 -> "email template %s %s".format(templateID.toString(event, debug), if (withData) "with objects %s".format(templateData.toString(event, debug)) else "")
            else -> "email template"
        }
    }

    private lateinit var templateID: Expression<String>
    private lateinit var templateData: Variable<*>
    private var line = -1
    private var withData = false

    @Suppress("unchecked_cast")
    override fun init(
        expressions: Array<Expression<*>?>?,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        line = matchedPattern
        if (line == 1) {
            withData = parseResult.hasTag("with")
            val expr1 = expressions!![1]
            templateID = expressions[0] as Expression<String>
            if (withData) {
                if (expr1 is Variable<*>) {
                    if (expr1.isSingle) {
                        SkriptMail.logger().errorWithNode("Variable need to be a list!", node = parser.node)
                        return false
                    }
                    templateData = expr1
                    return true
                }
            } else {
                return true
            }
        } else { return true }
        return false
    }

    companion object {init {
        Skript.registerExpression(ExprTemplates::class.java, String::class.java, ExpressionType.COMBINED,
            "all email templates",
            "email template %string% [(:with) [objects|data] %-objects%]"
        )
    }}
}
