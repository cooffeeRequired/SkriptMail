@file:Suppress("unused")

package cz.coffeerequired.skriptmail.api

import cz.coffeerequired.skriptmail.api.email.Account
import jakarta.mail.Message
import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMultipart
import org.bukkit.configuration.ConfigurationSection
import java.awt.Color
import java.io.IOException
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.reflect.KClass


fun String.times(times: Int): String {
    val b = StringBuilder()
    for (i in 0 until times) {
        b.append(this)
    }
    return b.toString()
}

fun String.isHTML(): Boolean {
    val pattern= "(?i)<([A-Z][A-Z0-9]*)\\b[^>]*>(.*?)</\\1>"
    val p: Pattern = Pattern.compile(pattern)
    val m: Matcher = p.matcher(this)
    return m.find()
}

fun String.gradient(startColor: String, endColor: String, step: Int? = null): String {
    val steps = step ?: this.length
    val gradientBuilder = StringBuilder()
    val gradientColors = mutableListOf<String>()
    val startRGB = Color.decode(startColor)
    val endRGB = Color.decode(endColor)
    val stepSizeRed = (endRGB.red - startRGB.red) / steps.toDouble()
    val stepSizeGreen = (endRGB.green - startRGB.green) / steps.toDouble()
    val stepSizeBlue = (endRGB.blue - startRGB.blue) / steps.toDouble()

    for (i in 0 until steps) {
        val newRed = (startRGB.red + stepSizeRed * i).toInt()
        val newGreen = (startRGB.green + stepSizeGreen * i).toInt()
        val newBlue = (startRGB.blue + stepSizeBlue * i).toInt()

        val interpolatedColor = String.format("#%02X%02X%02X", newRed, newGreen, newBlue)
        gradientColors.add(interpolatedColor)
    }

    gradientColors.add(endColor)

    for (charIndex in this.indices) {
        val gradientIndex = (charIndex * steps) / this.length
        val color = gradientColors[gradientIndex]
        gradientBuilder.append("&$color").append(this[charIndex])
    }

    return "$gradientBuilder&r"
}

@Suppress("UNCHECKED_CAST")
fun <T : Enum<T>> ConfigurationSection.getEnum(path: String, enum: KClass<T>): T? {
    val record = this.getString(path)
    if (record != null) {
        val t = enum.java.getMethod("valueOf", String::class.java).invoke(null, record)
        if (enum.isInstance(t)) {
            return t as T
        }
    }
    return null
}

fun tryGetById(value: String): Account? {
    return ConfigFields.ACCOUNTS.find { it.id.equals(value, false) }
}
fun tryGetContent(value: String): String {
    return ConfigFields.TEMPLATES.getOrDefault("$value.html", "")
}

fun Message.getTextFromMessage(): String? {
    var result: String? = ""
    if (this.isMimeType("text/plain")) {
        result = this.content.toString()
    } else if (this.isMimeType("text/hml")) {
        result = this.content.toString()
    } else if (this.isMimeType("multipart/*")) {
        result = (this.content as MimeMultipart).getTextFromMimeMultipart()
    }
    return result
}

@Throws(MessagingException::class, IOException::class)
fun MimeMultipart.getTextFromMimeMultipart(): String {
    val result = java.lang.StringBuilder()
    val count = this.count
    for (i in 0 until count) {
        val bodyPart = this.getBodyPart(i)
        if (bodyPart.isMimeType("text/plain")) {
            result.append("\n").append(bodyPart.content)
            break // without break same text appears twice in my tests
        } else if (bodyPart.isMimeType("text/html")) {
            val html = bodyPart.content as String
            result.append("\n").append(html)
        } else if (bodyPart.content is MimeMultipart) {
            result.append((bodyPart.content as MimeMultipart).getTextFromMimeMultipart())
        }
    }
    return result.toString()
}
