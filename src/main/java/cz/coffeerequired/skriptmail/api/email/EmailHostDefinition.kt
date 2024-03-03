package cz.coffeerequired.skriptmail.api.email

class EmailHostDefinition {
    private var name: String? = null
    private var smtp: Pair<String, Int>? = null
    private var imap: Pair<String, Int>? = null
    var sslAllowed: Boolean = false
    var tlsAllowed: Boolean = false

    fun name(name: String) { this.name = name }
    fun smtp(host: String, port: Int) { smtp = Pair(host, port) }
    fun imap(host: String, port: Int) { imap = Pair(host, port) }

    fun build(): EmailHost {
        requireNotNull(name) { "Name is not defined for the email host" }
        requireNotNull(smtp) { "SMTP server is not defined for $name" }
        requireNotNull(imap) { "IMAP server is not defined for $name" }

        return EmailHost(
            name!!,
            smtp!!.first,
            smtp!!.second,
            imap!!.first,
            imap!!.second,
            sslAllowed,
            tlsAllowed
        )
    }
}

fun host(block: EmailHostDefinition.() -> Unit): EmailHost {
    val definition = EmailHostDefinition()
    definition.block()
    return definition.build()
}

data class EmailHost(
    val name: String,
    val smtpHost: String,
    val smtpPort: Int,
    val imapHost: String,
    val imapPort: Int,
    val sslAllowed: Boolean,
    val tlsAllowed: Boolean
)