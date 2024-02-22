package cz.coffeerequired.skriptmail.api.email

import cz.coffeerequired.skriptmail.api.EmailFieldType
import java.util.*

class Account(
    val address: String?,
    private val type: EmailFieldType?,
    private val host: String?,
    private val port: Long? = 0,
    var auth: Boolean?,
    var starttls: Boolean?,
    private var id: String?,
    var authUsername: String?,
    var authPassword: String?
) {
    operator fun component1() = address
    operator fun component2() = type
    operator fun component3() = host
    operator fun component4() = port
    operator fun component5() = auth
    operator fun component6() = starttls
    operator fun component7() = authUsername
    operator fun component8() = authPassword
    operator fun component9() = id

    init {
        if (this.id == null) { this.id = UUID.randomUUID().toString() }
        if (this.auth == null) this.auth = false
        if (this.starttls == null) this.starttls = false
    }

    override fun toString(): String {
        return "ConfigEmailField{address: $address, type: $type, host: $host, port: $port, auth: $auth, starttls: $starttls}"
    }
}