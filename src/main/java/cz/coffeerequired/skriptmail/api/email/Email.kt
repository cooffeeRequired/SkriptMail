package cz.coffeerequired.skriptmail.api.email

import cz.coffeerequired.skriptmail.SkriptMail

class Email(
    var field: Account,
    var recipient: MutableList<String>?,
    var subject: String?,
    var content: String?,
) {
    init {
        if (recipient == null) recipient = mutableListOf()
    }

    var hasTemplate: Boolean = false

    fun sContent(template: String) {
        if (this.content == null && !this.hasTemplate) {
            this.content = template
            this.hasTemplate = !this.hasTemplate
        } else {
            SkriptMail.gLogger().warn("Email have already sett an template")
        }
    }

    operator fun component1(): Account { return this.field }
    operator fun component2(): MutableList<String>? { return this.recipient }
    operator fun component3(): String? { return this.subject }
    operator fun component4(): String? { return this.content }

    companion object {
        enum class Tokens(var value: String) {
            SERVICE("service"),
            HOST("host"),
            PORT("port"),
            OPTAUTH("optAuth"),
            OPTSTARTTLS("startTls"),
        }

        fun tokenize(input: String): MutableMap<Tokens, String>{
            val tokens = mutableMapOf<Tokens, String>()
            val buffer = StringBuilder()
            var cCounter = 0
            var aCounter = 0
            var isOptional = false
            var i = 0
            for (ch in input) {
                when (ch) {
                    ':' -> {
                        if (cCounter == 0) { tokens[Tokens.SERVICE] = buffer.toString(); buffer.clear() }
                        else if (cCounter == 1) { tokens[Tokens.HOST] = buffer.toString(); buffer.clear() }
                        cCounter++
                    }
                    '@' -> {
                        isOptional = true
                        if (cCounter == 2) { tokens[Tokens.PORT] = buffer.toString(); buffer.clear() }
                    }
                    '&' -> {
                        if (aCounter == 0) { tokens[Tokens.OPTAUTH] = buffer.toString(); buffer.clear() }
                        aCounter++
                    }
                    else -> {
                        buffer.append(ch)
                        if (isOptional) {
                            if (i+1 == input.length && aCounter > 0) { tokens[Tokens.OPTSTARTTLS] = buffer.toString(); buffer.clear() }
                        } else {
                            if (i+1 == input.length && cCounter >= 2) { tokens[Tokens.PORT] = buffer.toString(); buffer.clear() }
                        }
                    }
                }
                i++
            }
            return tokens
        }
    }
}