package cz.coffeerequired.skriptmail.api.email

import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.ConfigFields.PROJECT_DEBUG

class Email(
    var account: Account,
    var recipients: MutableList<String>? = mutableListOf(),
    var subject: String? = "Mail from SkriptMail service",
    var content: String? = "",
    var isRegistered: Boolean? = false,
) {
    var hasTemplate: Boolean = false

    /** @return [Account] [account] content*/
    operator fun component1(): Account = this.account
    /** @return [MutableList<String>] [recipients] recipients*/
    operator fun component2(): MutableList<String>? = this.recipients
    /** @return [String] subject*/
    operator fun component3(): String? = this.subject
    /** @return [String] content*/
    operator fun component4(): String? = this.content
    /** @return [Boolean] isRegistered*/
    operator fun component5(): Boolean = this.isRegistered ?: false

    companion object {
        enum class Tokens(var value: String) {
            SERVICE("service"),
            HOST("host"),
            PORT("port"),
            OPTAUTH("optauth"),
            OPTSTARTTLS("starttls"),
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
                    '=' -> buffer.clear()
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
            if (PROJECT_DEBUG) SkriptMail.logger().debug("Tokenized input: $input, tokens: $tokens")
            return tokens
        }
    }

    override fun toString(): String {
        return "Email{account=${account}, recipients=${recipients?.toTypedArray().contentToString()}, subject='$subject', content='$content', registered=$isRegistered}"
    }
}
