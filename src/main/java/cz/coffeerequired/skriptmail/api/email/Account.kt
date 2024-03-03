package cz.coffeerequired.skriptmail.api.email

import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.EmailFieldType
import java.util.*

class Account(
    val address: String?,
    val type: EmailFieldType?,
    val host: String?,
    val port: Long? = 0,
    var auth: Boolean?,
    var starttls: Boolean?,
    var id: String?,
    var authUsername: String?,
    var authPassword: String?,
    private var service: EmailHost? = null
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
    operator fun component10() = service

    init {
        if (this.id == null) { this.id = UUID.randomUUID().toString() }
        if (this.auth == null) this.auth = false
        if (this.starttls == null) this.starttls = false
    }

    override fun toString(): String {
        return "ConfigEmailField{address: $address, type: $type, host: $host, port: $port, auth: $auth, starttls: $starttls, preferred_host: $service}"
    }

    companion object {
        fun fromTokens(tokens: MutableMap<Email.Companion.Tokens, String>, address: String): Account? {
            try {
                if (tokens.size >= 3) {
                    val service = EmailFieldType.valueOf((tokens[Email.Companion.Tokens.SERVICE] as String).uppercase())
                    val host = tokens[Email.Companion.Tokens.HOST]
                    val port = (tokens[Email.Companion.Tokens.PORT] as String).toLong()
                    val auth = tokens[Email.Companion.Tokens.OPTAUTH].toBoolean()
                    val starttls = tokens[Email.Companion.Tokens.OPTSTARTTLS].toBoolean()
                    return Account(address, service, host, port, auth = auth, starttls = starttls, null, null, null)
                } else {
                    SkriptMail.logger().warn("Expecting length is 3 but got ${tokens.size}, please check format of credentials string")
                    SkriptMail.logger().warn("Correct format is <service>:<host>:<port>@<auth=boolean optional>&<starttls=boolean optional>")
                }
            } catch (ex: Exception) {
                if (ex.message!!.contains("No enum constant")) {
                    SkriptMail.logger().error("Cannot get service ${tokens[Email.Companion.Tokens.SERVICE]}, allowed services are ${EmailFieldType.entries.toTypedArray().contentToString()}")
                } else if (ex.message!!.contains("null cannot be cast to non-null")) {
                    SkriptMail.logger().error("Creation of new Email failed!")
                    val filtered = tokens.filter { !it.key.toString().contains("opt", true) }
                    SkriptMail.logger().warn("Expecting length is 3 but got ${filtered.size}, please check format of credentials string")
                    SkriptMail.logger().warn("Correct format is <service>:<host>:<port>@<auth=boolean optional>&<starttls=boolean optional>")
                } else {
                    SkriptMail.logger().exception(ex, "Something goes wrong")
                }
            }
            return null
        }
        fun fromService(input: EmailHost, address: String, id: String?): Account {
            return Account(address, null, null, null, null, null, id, null, null, input)
        }
    }

    fun isCustom(): Boolean {
        return this.service == null
    }
}