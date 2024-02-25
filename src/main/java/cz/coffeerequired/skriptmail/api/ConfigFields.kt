package cz.coffeerequired.skriptmail.api

import cz.coffeerequired.skriptmail.api.email.Account
import java.util.concurrent.TimeUnit


enum class EmailFieldType(val value: String) {
    SMTP("smtp"),
    IMAP("imap"),
    POP3("pop3");
}

class ConfigFields {
    companion object {
        var ACCOUNTS: MutableList<Account> = mutableListOf()
        var TEMPLATES: MutableMap<String, String> = mutableMapOf()
        var PROJECT_DEBUG: Boolean? = null
        var EMAIL_DEBUG: Boolean? = null
        var MAILBOX_ENABLED: Boolean? = null
        var MAILBOX_FOLDERS: List<String> = listOf()
        var MAILBOX_FILTER: Regex? = null
        var MAILBOX_REFRESH_RATE: Long = 1000
        var MAILBOX_RATE: TimeUnit = TimeUnit.MINUTES
        var MAILBOX_PER_REQUEST: Long = 100
    }
}
