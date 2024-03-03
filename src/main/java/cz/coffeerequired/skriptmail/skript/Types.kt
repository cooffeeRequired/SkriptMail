package cz.coffeerequired.skriptmail.skript

import ch.njol.skript.classes.ClassInfo
import ch.njol.skript.classes.EnumClassInfo
import ch.njol.skript.classes.Parser
import ch.njol.skript.lang.ParseContext
import ch.njol.skript.registrations.Classes
import cz.coffeerequired.skriptmail.api.email.Account
import cz.coffeerequired.skriptmail.api.email.Email
import cz.coffeerequired.skriptmail.api.email.EmailHosts
import jakarta.mail.Message


@Suppress("UNUSED")
object Types {
    init {
        Classes.registerClass(
            ClassInfo(Account::class.java, "emailaccount")
                .user("emailaccount")
                .name("email account")
                .description(
                    "Representing of Email configuration/account",
                    """
                        ```applescript
                            set {_email} to configured email account "example"
                            send address of {_email}
                            send type of {_email}
                            send host of {_email}
                            send port of {_email}
                            send auth of {_email}
                            send starttls of {_email}
                            send service of {_email}
                        ```
                    """.trimIndent()
                )
                .since("1.0")
                .parser(object : Parser<Account>() {
                    override fun toString(o: Account, flags: Int): String {
                        return "email account for id '${o.id}'"
                    }
                    override fun toVariableNameString(o: Account): String { return toString(o, 0) }
                    override fun canParse(context: ParseContext): Boolean { return false }
                })
        )

        Classes.registerClass(
            EnumClassInfo(EmailHosts::class.java, "emailservice", "email service")
                .user("email ?service?")
                .name("Email Services")
                .description("represent all predefined email services as like *GMAIL*, *YAHOO* etc.")
                .examples("set {_email} to new email using predefined service GMAIL")
                .since("1.1")
        )

        Classes.registerClass(
            ClassInfo(Email::class.java, "email")
                .user("email")
                .name("email")
                .description("Representing new email form", """
                    ```applescript
                        set {_email} to new email using {_account}
                        send {_email} to console
                    
                        set {_email} to new email using credential string "smtp:gmail.com:587@auth=true&starttls=true" with address "test@gmail.com"
                        send {_email} to console
                    
                        set {_email} to new email using predefined service OUTLOOK with address "test2@gmail.com"
                        send {_email} to console
                    ````
                """.trimIndent())
                .parser(object  : Parser<Email>() {
                    override fun toString(o: Email, flags: Int): String {
                        return "email form of service: %s from %s and recipients %s with subject %s and content-length %s"
                            .format(
                                o.account.id,
                                o.account.component1(),
                                if (o.component2()?.isEmpty() == true) "none" else o.component2()?.joinToString { it },
                                o.component3(),
                                o.component4()?.length
                            )
                    }
                    override fun toVariableNameString(o: Email): String { return toString(o, 0) }
                    override fun canParse(context: ParseContext): Boolean { return false }
                }
        ))

        Classes.registerClass(
            ClassInfo(Message::class.java, "emailmessage")
                .user("email message")
                .name("Email Message")
                .description("The message (email) object from IMAP")
                .parser(object  : Parser<Message>() {
                    override fun toString(o: Message?, flags: Int): String {
                        if (o != null) {
                            return "Email message ${o.from[0]}"
                        }
                        return "Email message"
                    }
                    override fun toVariableNameString(o: Message?): String {
                        return toString(o, 0)
                    }
                    override fun canParse(context: ParseContext): Boolean {
                        return false
                    }
                })
        )
    }
}
