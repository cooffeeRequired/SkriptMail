@file:Suppress("UNUSED_VARIABLE")

package cz.coffeerequired.skriptmail.api

import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.ConfigFields.ACCOUNTS
import cz.coffeerequired.skriptmail.api.ConfigFields.EMAIL_DEBUG
import cz.coffeerequired.skriptmail.api.ConfigFields.PROJECT_DEBUG
import cz.coffeerequired.skriptmail.api.ConfigFields.TEMPLATES
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import java.lang.reflect.Field

@Suppress("UNUSED_PARAMETER")
class Commands(private val logger: Logger, private val config: Config) : TabExecutor {
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        val completions: MutableList<String> = ArrayList()

        if (command.name.equals("skmail", ignoreCase = true) || command.name.equals("skriptmail", ignoreCase = true)) {
            when (args.size) {
                1 -> {
                    completions.add("reload")
                    completions.add("about")
                    completions.add("history")
                    completions.add("?")
                }
                2 -> {
                    when(args[0]) {
                        "reload" -> {
                            completions.add("config")
                            completions.add("all")
                            completions.add("templates")
                            completions.add("mailbox")
                        }
                    }
                }
            }
        }

        return completions
    }

    @Suppress("unchecked_cast")
    private fun <T>Class<T>.getValueByKey(key: String): T? {
        try {
            val declaredField: Field = this.getDeclaredField(key)
            declaredField.isAccessible = true
            return declaredField.get(null) as T
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return null
    }


    private fun reloadConfig(sender: CommandSender) {
        val before: MutableMap<String, *> = mutableMapOf(
            "ACCOUNTS" to ACCOUNTS,
            "TEMPLATES" to TEMPLATES,
            "PROJECT_DEBUG" to PROJECT_DEBUG,
            "EMAIL_DEBUG" to EMAIL_DEBUG
        )
        this.config.loadConfigFile(false, sender = sender)
        this.config.loadConfigs()
        var changed = 0
        before.forEach { (key, value) ->
            val field = ConfigFields::class.java.getValueByKey(key)
            if (value!!.toString() != field.toString()) { changed++; logger.info("key $key was changed!", sender = sender) }
        }
        if (changed == 0) { logger.info("Nothing was changed..", sender = sender) }
        else {
            logger.info("changed ${if (changed == 1) "$changed. field" else "$changed. fields"}", sender = sender)
        }
    }

    private fun printableDescription(desc: String?): String {
        if (desc.isNullOrEmpty()) return ""
        val chunks = desc.chunked(150)
        return chunks.joinToString("\n") { "\n    -$it" }
    }

    private fun printAbout(sender: CommandSender?) {
        val plm = SkriptMail.instance().pluginMeta
        logger.info("kotlin version: ${config.getKotlinVersion()}", sender = sender)
        logger.info("revision version: ${config.getRevisionVersion()}", sender = sender)
        logger.info("Description: &f${printableDescription(plm.description)}", sender = sender)
        logger.info("version: ${plm.version}",  sender = sender)
        logger.info("API-version: ${plm.apiVersion}", sender = sender)
        logger.info("Website: ${plm.website}", sender = sender)
        if (sender is Player) {
            logger.infoClickable("", sender = sender, clickable = "https://www.github.com/coffeeRequired/SkripMail")
        } else {
            logger.info("Github: &f&nhttps://www.github.com/coffeeRequired/SkripMail")
        }
        logger.emptyLine(sender)
    }

    private fun printHistory(sender: CommandSender) {
        val builder: StringBuilder = StringBuilder()
        var i = 1
//        Config.executedEmails.forEach { (date, value) ->
//            val ( account, recipients ) = value
//            val (address, type, host, port) = account
//            if (sender is Player) {
//                builder.append("- $i. $date $address -> %s [$type $host:$port".format(*recipients!!.toTypedArray()))
//            } else {
//                builder.append("$i. [$date] $type $address -> %s using $host:$port".format(*recipients!!.toTypedArray()))
//            }
//            i++
//            this.logger.info(builder.toString(), sender = sender)
//            builder.clear()
//        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("skmail.admin")) {
            if (command.name.equals("skmail", true) || command.name.equals("skriptmail", true)) {
                if (args.isEmpty()) {
                    logger.info("&7Usage: &e/skmail reload <config|all|templates|mailbox>", sender = sender)
                    logger.info("&7Usage: &e/skmail about", sender = sender)
                    logger.info("&7Usage: &e/skmail history", sender = sender)
                    return true
                }
                if (args[0].equals("reload", true)) {
                    if (args.size < 2) {
                        logger.info("", sender = sender)
                        logger.info(
                            "&7Usage: &e/skmail reload all &f&o- will reload all possible configuration and templates",
                            withoutPrefix = true,
                            sender = sender
                        )
                        logger.info(
                            "&7Usage: &e/skmail reload config &f&o- will reload configuration",
                            withoutPrefix = true,
                            sender = sender
                        )
                        logger.info(
                            "&7Usage: &e/skmail reload templates &f&o- will reload templates",
                            withoutPrefix = true,
                            sender = sender
                        )
                        logger.emptyLine(sender)
                    } else if (args[1].contains("template") && args[1].length <= 9) {
                        logger.info("Reloading templates...", sender = sender)
                        this.config.loadTemplates()
                        logger.info(
                            "Reload finished... was loaded &a%s&7 templates",
                            TEMPLATES.size,
                            sender = sender
                        )
                        logger.emptyLine(sender)
                    } else if (args[1] == "config") {
                        logger.info("Reloading config...", sender = sender)
                        reloadConfig(sender)
                        logger.info("Reload finished...", sender = sender)
                        logger.emptyLine(sender)
                    } else if (args[1] == "mailbox") {
                        logger.info("Reloading mailbox setting...", sender = sender)
                        config.loadMailboxSettings()
                        logger.info("Reloading mailbox finished...", sender = sender)
                    } else if (args[1] == "all") {
                        logger.info("Reloading all (templates/config/mailbox)...", sender = sender)
                        reloadConfig(sender)
                        this.config.loadTemplates()
                        this.config.loadMailboxSettings()
                        logger.info(
                            "Reload finished... was loaded &a%s&7 templates",
                            TEMPLATES.size,
                            sender = sender
                        )
                        logger.emptyLine(sender)
                    }
                } else if (args[0].equals("about", true) || args[0].equals("?", true)) {
                    printAbout(sender)
                } else if (args[0].equals("history", true)) {
                    printHistory(sender)
                }
            }
            return true
        }
        this.logger.info("You don't have permission &c'skmail.admin' &7for using that command", sender = sender)
        return false
    }
}
