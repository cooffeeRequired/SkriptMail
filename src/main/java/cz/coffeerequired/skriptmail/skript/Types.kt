package cz.coffeerequired.skriptmail.skript

import ch.njol.skript.classes.ClassInfo
import ch.njol.skript.classes.Parser
import ch.njol.skript.lang.ParseContext
import ch.njol.skript.registrations.Classes
import cz.coffeerequired.skriptmail.api.email.Account
import cz.coffeerequired.skriptmail.api.email.Email

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
                        ```
                    """.trimIndent()
                )
                .since("1.0")
                .parser(object : Parser<Account>() {
                    override fun toString(o: Account, flags: Int): String {
                        return "email account for id %s".format(o.component7())
                    }
                    override fun toVariableNameString(o: Account): String { return toString(o, 0) }
                    override fun canParse(context: ParseContext): Boolean { return false }
                })
        )

        Classes.registerClass(
            ClassInfo(Email::class.java, "email")
                .user("email")
                .name("email")
                .description("Representing new email form", """
                    ```applescript
                        set {_email} to new email with credentials "smtp:host:port@auth=true&starttls=true"
                        set {_email} to new email using account "example"
                    ````
                """.trimIndent())
                .parser(object  : Parser<Email>() {
                    override fun toString(o: Email, flags: Int): String {
                        return "email form of service: %s from %s and recipients %s with subject %s and content-length %s"
                            .format(
                                o.field.component2()!!.value,
                                o.field.component1(),
                                if (o.recipient?.isEmpty() == true) "none" else o.recipient!!.joinToString { it },
                                o.subject,
                                if (o.content != null) o.content!!.length else ""
                            )
                    }
                    override fun toVariableNameString(o: Email): String { return toString(o, 0) }
                    override fun canParse(context: ParseContext): Boolean { return false }
                }
        ))
    }
}
