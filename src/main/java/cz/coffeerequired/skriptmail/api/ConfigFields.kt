package cz.coffeerequired.skriptmail.api

import cz.coffeerequired.skriptmail.api.email.Account

@Suppress("UNUSED")
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
    }
}
