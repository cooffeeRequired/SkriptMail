@file:Suppress("DEPRECATION")
package cz.coffeerequired.skriptmail.api

import ch.njol.skript.config.Node
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender

class Logger(private val version: Version) {
    private var prefix: String = ""
    fun setPrefix(prefix: String) { this.prefix = prefix }

    private fun messageHandler(isSupported: Boolean, message: String, vararg arguments: Any?, sender: CommandSender, level: Int? = 0, withoutPrefix: Boolean? = false, clickable: Any? = false) {
        var formattedString: String = if (withoutPrefix != true) String.format("&7[ ${this.prefix} &7] $message", *arguments) else message.format(*arguments)
        if (withoutPrefix == false) {
            when(level) {
                1 -> formattedString = String.format("&7[ ${this.prefix} ${"info".gradient("#38F6F1", "#38B5F6")} &7] $message", *arguments)
                2 -> formattedString = String.format("&7[ ${this.prefix} ${"warn".gradient("#F6D838", "#B29107")} &7] $message", *arguments)
                3 -> formattedString = String.format("&7[ ${this.prefix} ${"error".gradient("#FA3B3B", "#8C0404")} &7] $message", *arguments)
            }
        }

        if (isSupported) {
            val converter = LegacyComponentSerializer
                .builder()
                .useUnusualXRepeatedCharacterHexFormat()
                .character('&')
                .hexColors()
                .build()
            if (clickable != false) {
                val mess = converter.deserialize(formattedString)
                    .append(converter.deserialize("&e&lGitHub").hoverEvent(HoverEvent.showText(converter.deserialize("Open the URL"))))
                    .clickEvent(ClickEvent.openUrl(clickable.toString()))
                sender.sendMessage(mess)
            } else {
                sender.sendMessage(converter.deserialize(formattedString))
            }
        } else {
            sender.sendMessage(ChatColor.stripColor(formattedString))
        }
    }

    private var sender: ConsoleCommandSender = Bukkit.getConsoleSender()

    fun emptyLine(sender: CommandSender?) {
        this.messageHandler(
            true,
            message = "",
            sender = sender ?: this.sender,
            withoutPrefix = true
        )
    }

    fun log(msg: Any, vararg arguments: Any?, sender: CommandSender? = null) {
        val (major, minor, _) = this.version
        this.messageHandler(
            major + minor > 1L + 16L,
            message = msg.toString(),
            arguments = arguments,
            sender = sender ?: this.sender
        )
    }

    fun info(msg: Any, vararg arguments: Any?, sender: CommandSender? = null, withoutPrefix: Boolean? = false) {
        val (major, minor, _) = this.version
        this.messageHandler(
            major + minor > 1L + 16L,
            message = msg.toString(),
            arguments = arguments,
            sender = sender ?: this.sender,
            level = 1,
            withoutPrefix
        )
    }

    fun infoClickable(msg: Any, vararg arguments: Any?, sender: CommandSender? = null, withoutPrefix: Boolean? = false, clickable: Any?) {
        val (major, minor, _) = this.version
        this.messageHandler(
            major + minor > 1L + 16L,
            message = msg.toString(),
            arguments = arguments,
            sender = sender ?: this.sender,
            level = 1,
            withoutPrefix,
            clickable = clickable
        )
    }

    fun warn(msg: Any, vararg arguments: Any?, sender: CommandSender? = null) {
        val (major, minor, _) = this.version
        this.messageHandler(
            major + minor > 1L + 16L,
            message = msg.toString(),
            arguments = arguments,
            sender = sender ?: this.sender,
            level = 2
        )
    }

    fun error(msg: Any, vararg arguments: Any?, sender: CommandSender? = null) {
        val (major, minor, _) = this.version
        this.messageHandler(
            major + minor > 1L + 16L,
            message = msg.toString(),
            arguments = arguments,
            sender = sender ?: this.sender,
            level = 3
        )
    }

    fun errorWithNode(msg: Any, vararg arguments: Any?, node: Node?, sender: CommandSender? = null) {
        val (major, minor, _) = this.version
        this.messageHandler(
            major + minor > 1L + 16L,
            message = "Node: $node \n $msg",
            arguments = arguments,
            sender = sender ?: this.sender,
            level = 3
        )
    }

    private fun exceptionParsing(ex: Exception, title: String?): String {
        val stacktrace = ex.stackTrace
        val (major, minor, _) = this.version
        this.messageHandler(
            major + minor > 1L + 16L,
            message = "API message: ${ex.localizedMessage}",
            sender = this.sender,
            level = 3
        )
        val b = StringBuilder()
            .append(title)
            .append("\n")
            .append("&c*".times(100)).append("\n")
        for (stack in stacktrace) {
           val (lineNumber, fileName, className, methodName) = stack
            b
                .append("\t&c* ")
                .append("&7(&8$lineNumber&7) ")
                .append("&#f73423$fileName &c$className.&#630d0b&n&l$methodName&r&c")
                .append("\n")
        }
        b.append("&c*".times(100))
        return b.toString()
    }

    fun exception(ex: Exception, msg: Any?, vararg arguments: Any?, sender: CommandSender? = null) {
        val (major, minor, _) = this.version
        this.messageHandler(
            major + minor > 1L + 16L,
            message = exceptionParsing(ex, msg?.toString()),
            arguments = arguments,
            sender = sender ?: this.sender,
            level = 3
        )
    }
}


operator fun StackTraceElement.component1(): Int {
    return this.lineNumber
}
operator fun StackTraceElement.component2(): String? {
    return this.fileName
}
operator fun StackTraceElement.component3(): String? {
    return this.className
}
operator fun StackTraceElement.component4(): String? {
    return this.methodName
}
