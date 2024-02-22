package cz.coffeerequired.skriptmail.api.email

import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.SkriptMail.Companion.instance
import cz.coffeerequired.skriptmail.api.ConfigFields
import cz.coffeerequired.skriptmail.api.EmailFieldType
import org.bukkit.Bukkit
import org.simplejavamail.api.email.Email
import org.simplejavamail.api.mailer.Mailer
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder

class EmailController(
    private val account: Account,
    authUsername: String?,
    authPassword: String?,
) {
    private var mailer: Mailer? = null
    private lateinit var form: Form

    init {
        val (_, type, host, port, auth, starttls, authU, authP) = account

        when (type) {
            EmailFieldType.SMTP -> {
                mailer = if (auth == true) {
                    MailerBuilder
                        .withSMTPServer(host, port!!.toInt(), authU ?: authUsername, authP ?: authPassword)
                        .withTransportStrategy(if (starttls == true) TransportStrategy.SMTP_TLS else TransportStrategy.SMTP)
                        .buildMailer()
                } else {
                    MailerBuilder
                        .withSMTPServer(host, port!!.toInt())
                        .withTransportStrategy(if (starttls == true) TransportStrategy.SMTP_TLS else TransportStrategy.SMTP)
                        .buildMailer()
                }
            }
            EmailFieldType.POP3 -> { SkriptMail.gLogger().warn("POP3 service aren't supported yet!") }
            EmailFieldType.IMAP -> { SkriptMail.gLogger().warn("IMAP service aren't supported yet!") }
            else -> { SkriptMail.gLogger().exception(IllegalStateException("The service %s aren't supported".format(type)), msg = "The service %s aren't supported".format(type) ) }
        }
    }

    fun setForm(form: Form) { this.form = form }
    fun send() {
        Bukkit.getScheduler().runTaskAsynchronously(instance(), Runnable {
            val email: Email?
            val popBuilder = EmailBuilder.startingBlank()
            popBuilder.withSubject(this.form.subject)
            if (this.account.address?.contains(";") == true) {
                val parts = this.account.address.split(";")
                popBuilder.from(parts[0], parts[1])
            } else {
                this.account.address?.let { popBuilder.from(it) }
            }
            if (this.form.recipients!!.size < 1) {
                popBuilder.to(this.form.recipients!![0])
            } else {
                popBuilder.toMultiple(*this.form.recipients!!.toTypedArray())
            }
            if (this.form.usingTemplate) {
                popBuilder.withHTMLText(this.form.content)
            } else {
                popBuilder.withPlainText(this.form.content)
            }
            email = popBuilder.buildEmail()
            try {
                this.mailer!!.sendMail(email)
                if (ConfigFields.PROJECT_DEBUG == true) SkriptMail.gLogger().info("Email was sent successfully!")
            } catch (ex: Exception) {
                if (ConfigFields.PROJECT_DEBUG == true) SkriptMail.gLogger().warn("Sending mail failed! Caused by: %s", ex.cause!!.message)
            }
        })
    }

    companion object {
        class Form(
            val recipients: MutableList<String>?,
            val subject: String?,
            val content: String?,
            var usingTemplate: Boolean = false
        ) { companion object { fun formBuilder(): FormBuilder { return FormBuilder() } } }

        class FormBuilder {
            private var content: String? = null
            private var recipients: MutableList<String>? = mutableListOf()
            private var subject: String? = null
            fun content(str: String?): FormBuilder { this.content = str; return this }
            fun recipient(recipients: MutableList<String>?): FormBuilder { this.recipients = recipients;return this }
            fun subject(subject: String?): FormBuilder { this.subject = subject;return this }
            fun build(): Form { return Form(recipients, subject, content) }
        }
    }
}