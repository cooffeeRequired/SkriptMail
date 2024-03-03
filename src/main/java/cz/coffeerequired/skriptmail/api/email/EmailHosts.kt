package cz.coffeerequired.skriptmail.api.email

@Suppress("unused")
enum class EmailHosts(val host: EmailHost) {
    GMAIL(host {
        name("GMAIL")
        smtp("smtp.gmail.com", 587)
        imap("imap.gmail.com", 993)
        sslAllowed = true
        tlsAllowed = true
    }),
    YAHOO(host {
        name("YAHOO")
        smtp("smtp.mail.yahoo.com", 587)
        imap("imap.mail.yahoo.com", 993)
        sslAllowed = true
        tlsAllowed = false
    }),
    OUTLOOK(host {
        name("OUTLOOK")
        smtp("smtp-mail.outlook.com", 587)
        imap("outlook.office365.com", 993)
        sslAllowed = true
        tlsAllowed = false
    }),
    AOL(host {
        name("AOL")
        smtp("smtp.aol.com", 587)
        imap("imap.aol.com", 993)
        sslAllowed = true
        tlsAllowed = false
    }),
    ICLOUD(host {
        name("ICLOUD")
        smtp("smtp.mail.me.com", 587)
        imap("imap.mail.me.com", 993)
        sslAllowed = true
        tlsAllowed = false
    }),
    ZOHO(host {
        name("ZOHO")
        smtp("smtp.zoho.com", 587)
        imap("imap.zoho.com", 993)
        sslAllowed = true
        tlsAllowed = false
    }),
    GMX(host {
        name("GMX")
        smtp("smtp.gmx.com", 587)
        imap("imap.gmx.com", 993)
        sslAllowed = true
        tlsAllowed = false
    });
}