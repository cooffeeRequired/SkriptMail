@file:Suppress("unused")

package cz.coffeerequired.skriptmail.api

import cz.coffeerequired.skriptmail.api.email.Account
import java.util.concurrent.TimeUnit


enum class EmailFieldType(val value: String) {
    SMTP("smtp"),
    IMAP("imap"),
    POP3("pop3");
}

object ConfigFields {
    var ACCOUNTS: MutableList<Account> = mutableListOf()
    var TEMPLATES: MutableMap<String, String> = mutableMapOf()
    var PROJECT_DEBUG: Boolean = false
    var EMAIL_DEBUG: Boolean = false
    var MAILBOX_ENABLED: Boolean = false
    var MAILBOX_FOLDERS: List<String> = listOf()
    var MAILBOX_FILTER: Regex? = null
    var MAILBOX_REFRESH_RATE: Long = 1000
    var MAILBOX_RATE_UNIT: TimeUnit = TimeUnit.MINUTES
    var MAILBOX_BATCH_PER_REQUEST: Long = 100
}