package cz.coffeerequired.skriptmail.api.email

import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.ConfigFields.EMAIL_DEBUG
import cz.coffeerequired.skriptmail.api.ConfigFields.PROJECT_DEBUG
import java.util.*

class EmailServiceProvider {
    companion object {

        val registeredServices = mutableMapOf<String, EmailService>()
        val pairedAccountServices = mutableMapOf<String, String>()

        @Throws(EmailException::class)
        fun newEmailService(id: Pair<String?, String?>?, address: String?, host: EmailHost, usernameOrEmail: String?, password: String = ""): EmailService {
            val properties = Properties().apply {
                setProperty("mail.smtp.host",host.smtpHost)
                setProperty("mail.smtp.port", (if(!host.tlsAllowed) 465 else host.smtpPort).toString())
                setProperty("mail.smtp.auth", "true")
                setProperty("mail.smtp.starttls.enable",host.tlsAllowed.toString())
                setProperty("mail.imap.host",host.imapHost)
                setProperty("mail.imap.port",host.imapPort.toString())
                setProperty("mail.imap.ssl.enable",host.sslAllowed.toString())
                setProperty("mail.imap.partialfetch", "true")
                setProperty("mail.imap.connectionpoolsize", "5")
            }
            if (address == null || usernameOrEmail == null) throw EmailException("address and usernameOrEmail cannot be null", EmailExceptionType.INITIALIZE)
            val email =  EmailService(id!!.first as String, properties, usernameOrEmail, password, address, host)
            registeredServices[id.first as String] = email
            pairedAccountServices[id.second!!] = id.first as String
            return email
        }
        fun newEmailCustomService(id: Pair<String?, String?>, address: String?, host: String?, port: Long?, auth: Boolean?, startTLS: Boolean?, username: String?, password: String): EmailService {
            val properties = Properties().apply {
                setProperty("mail.smtp.host",host)
                setProperty("mail.smtp.port", port.toString())
                setProperty("mail.smtp.auth", auth.toString())
                setProperty("mail.smtp.starttls.enable", startTLS.toString())

                val imapaddress = host?.replace("smtp", "imap")

                setProperty("mail.imap.host", imapaddress)
                setProperty("mail.imap.port", "993")
                setProperty("mail.imap.ssl.enable", "true")
                setProperty("mail.imap.partialfetch", "true")
                setProperty("mail.imap.connectionpoolsize", "5")
            }
            var h: EmailHost? = null
            try {
                h = host {
                    name("Custom")
                    smtp(host.toString(), port!!.toInt())
                    imap(host?.replace("smtp", "imap")!!, 993)
                    tlsAllowed = startTLS!!
                    sslAllowed = true
                }
            }  catch (ex: Exception) {
                SkriptMail.logger().exception(ex, "")
            }
            if (address == null || username == null) throw EmailException("address and usernameOrEmail cannot be null", EmailExceptionType.INITIALIZE)
            val email =  EmailService(id.first!!, properties, username, password, address, h)
            registeredServices[id.first!!] = email
            pairedAccountServices[id.second!!] = id.first as String
            return email
        }
        fun unregisterService(id: String) {
            val service = registeredServices[id]
            if (service == null) {
                if (PROJECT_DEBUG && EMAIL_DEBUG) SkriptMail.logger().debug("Service cannot be null! $0 $this")
                return SkriptMail.logger().error("Service cannot be null!")
            }
            service.unregisterMailbox()
            registeredServices.remove(id)
        }

    }
}