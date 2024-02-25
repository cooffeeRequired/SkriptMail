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
                        .async()
                        .buildMailer()
                } else {
                    MailerBuilder
                        .withSMTPServer(host, port!!.toInt())
                        .async()
                        .withTransportStrategy(if (starttls == true) TransportStrategy.SMTP_TLS else TransportStrategy.SMTP)
                        .buildMailer()
                }
            }
            EmailFieldType.POP3 -> { SkriptMail.logger().warn("POP3 service aren't supported yet!") }
            EmailFieldType.IMAP -> { SkriptMail.logger().warn("IMAP service aren't supported yet!") }
            else -> { SkriptMail.logger().exception(IllegalStateException("The service %s aren't supported".format(type)), msg = "The service %s aren't supported".format(type) ) }
        }
    }

    fun setForm(form: Form) { this.form = form }
    fun send() {
        try {
            if (account.address.isNullOrEmpty()) throw Exception("Address is required!")
            if (form.content.isNullOrEmpty()) form.content = "No Content"
            if (form.recipients.isNullOrEmpty()) throw Exception("Recipients are required! Recipients of email are empty or null!")
            if (form.subject.isNullOrEmpty()) form.subject = "No Subject"

            Bukkit.getScheduler().runTaskAsynchronously(instance(), Runnable {
                    val email: Email?
                    val popBuilder = EmailBuilder.startingBlank()
                    popBuilder.withSubject(this.form.subject)

                    if (this.account.address.contains(";")) {
                        val parts = this.account.address.split(";")
                        popBuilder.from(parts[0], parts[1])
                    } else {
                        this.account.address.let { popBuilder.from(it) }
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

                if (ConfigFields.EMAIL_DEBUG == true) SkriptMail.logger().info("Email was present successfully")

                this.mailer!!.sendMail(email, true)
                    .whenComplete { _, throwable ->
                    if (throwable == null) {
                        if (ConfigFields.EMAIL_DEBUG == true) SkriptMail.logger().info("Email was sent successfully!")
                    } else {
                        if (ConfigFields.EMAIL_DEBUG == true) SkriptMail.logger().error("Sending mail failed! Caused by: %s", if (throwable.cause != null) throwable.cause!!.message else throwable.message)
                    }
                }
            })
        } catch (ex: Exception) {
            if (ConfigFields.PROJECT_DEBUG == true) SkriptMail.logger().error("Sending mail failed! Caused by: %s", if (ex.cause != null) ex.cause!!.message else ex.message)
        }
    }

    companion object {
        class Form(
            val recipients: MutableList<String>?,
            var subject: String?,
            var content: String?,
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