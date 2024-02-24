package cz.coffeerequired.skriptmail.api.email

import jakarta.mail.Address
import java.util.*

class ReceivedEmail(
    val subject: String, val rec: Date, val content: Any, val recipients: Array<Address>, val from: Array<Address>
)