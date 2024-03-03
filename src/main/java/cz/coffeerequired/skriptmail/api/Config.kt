package cz.coffeerequired.skriptmail.api

import ch.njol.skript.Skript
import ch.njol.skript.bstats.bukkit.Metrics
import ch.njol.skript.bstats.charts.SimplePie
import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.ConfigFields.ACCOUNTS
import cz.coffeerequired.skriptmail.api.ConfigFields.EMAIL_DEBUG
import cz.coffeerequired.skriptmail.api.ConfigFields.MAILBOX_BATCH_PER_REQUEST
import cz.coffeerequired.skriptmail.api.ConfigFields.MAILBOX_ENABLED
import cz.coffeerequired.skriptmail.api.ConfigFields.MAILBOX_FILTER
import cz.coffeerequired.skriptmail.api.ConfigFields.MAILBOX_FOLDERS
import cz.coffeerequired.skriptmail.api.ConfigFields.MAILBOX_RATE_UNIT
import cz.coffeerequired.skriptmail.api.ConfigFields.MAILBOX_REFRESH_RATE
import cz.coffeerequired.skriptmail.api.ConfigFields.PROJECT_DEBUG
import cz.coffeerequired.skriptmail.api.ConfigFields.TEMPLATES
import cz.coffeerequired.skriptmail.api.email.Account
import cz.coffeerequired.skriptmail.api.email.Email
import cz.coffeerequired.skriptmail.api.email.EmailHosts
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.util.*
import java.util.concurrent.TimeUnit

class Config(private val plugin: SkriptMail, private val server: Server, private val supportedVersions: List<Version>) {
    private lateinit var serverVersion: Version
    init { init() }
    private var revisionVersion: Any? = null
    private var kotlinVersion: Any? = null
    fun getRevisionVersion(): Any? = this.revisionVersion
    fun getKotlinVersion(): Any? = this.kotlinVersion


    /**
     * Returns the exact version of the server.
     *
     * @param str the version string
     * @return the version
     * @throws Exception if the version cannot be determined
     */
    private fun getExactVersion(str: String): Version {
        try {
            var literalVersion: String = str.split("MC:")[1]
            literalVersion = literalVersion.slice(0 until literalVersion.length - 1)
            return Version(literalVersion.trim())
        } catch (ex: Exception) {
            throw ex
        }
    }

    private fun init() {
        val exactBukkitVersion: Version = getExactVersion(Bukkit.getVersion())
        this.serverVersion = exactBukkitVersion
        if (!exactBukkitVersion.greaterOrEquals(this.supportedVersions[0])) {
            throw Exception("Version cannot be accepted cause it's not allowed... current version: $exactBukkitVersion, supported: ${this.supportedVersions[0]}")
        }
        try {
            val stream: InputStream? = this.plugin.getResource("plugin.yml")
            if (stream != null) {
                val `is` = InputStreamReader(stream)
                val yml = YamlConfiguration.loadConfiguration(`is`)
                this.revisionVersion = yml.get("revision-version")
                this.kotlinVersion = yml.get("kotlin-version")
            }
        } catch (ex: java.lang.Exception) {
           ex.printStackTrace()
        }
    }

    fun getServerVersion(): Version = this.serverVersion

    fun initializeSkript(dependency: String) {
        val pm: PluginManager = this.server.pluginManager
        val l: Logger =   SkriptMail.logger()
        val pl = pm.getPlugin(dependency)
        if (pl == null) {
            l.error("Dependency [%s] weren't found. Check your /plugins folder", dependency)
            return pm.disablePlugin(this.plugin)
        } else if (!pl.isEnabled) {
            l.error("Opps! Seems like SkMail was loaded before %s, something delayed the start. Try restart your server", dependency)
            return pm.disablePlugin(this.plugin)
        }
        val skriptPrefix = "&#e3e512S&#9ae150k&#55d57br&#00c59ci&#00b2aep&#329dadt&r"
        return l.info("%s was found and hooked.", skriptPrefix)
    }

    fun classRegistration(self: JavaPlugin, classesPaths: String) {
        val l: Logger =  SkriptMail.logger()
        val pm: PluginManager = this.server.pluginManager
        try {
            val addon = Skript.registerAddon(self)
            addon.setLanguageFileDirectory("lang")
            addon.loadClasses(classesPaths)
        } catch (ex: Exception) {
            l.exception(ex, "Skript cannot been registered %s classes!".format(this.plugin.name))
            return pm.disablePlugin(this.plugin)
        }
    }

    fun initializeResources() {
        loadConfigFile(false, null)
        matchConfiguration()
        loadFileHandler(null, "templates/main.html", replace = false, true)
        loadConfigs()
        loadMailboxSettings()
        loadTemplates()
    }
    private var configFile: File? = null
    private lateinit var config: FileConfiguration

    private fun loadFileHandler(file: File?, fileName: String, replace: Boolean, withoutYaml: Boolean = false, sender: CommandSender? = null): List<Any?> {
        var f: File? = file
        if (file == null) f = File(this.plugin.dataFolder, fileName)
        if (!f!!.exists()) this.plugin.saveResource(fileName, replace)
        if (!withoutYaml) {
            val config: FileConfiguration = f.let { YamlConfiguration.loadConfiguration(it) }
            SkriptMail.logger().info("&n$fileName&7 was loaded &asuccessfully.", sender = sender)
            return listOf(config, f)
        }
        SkriptMail.logger().info("&n$fileName&7 was loaded &asuccessfully.", sender = sender)
        return listOf()
    }

    fun loadConfigFile(replace: Boolean, sender: CommandSender?) {
        val (config, file) = loadFileHandler(this.configFile, "config.yml", replace = replace, sender = sender)
        if (config != null && config is FileConfiguration) this.config = config
        if (file != null && file is File) this.configFile = file
    }

    private fun getExactRecord(config: ConfigurationSection?, path: String): Account? {
        val section = config!!.getConfigurationSection(path)
        if (section != null) {
            val service = section.getEnum("service", EmailHosts::class)
            val address = section.getString("address")
            var type: EmailFieldType? = null
            var host: String? = null
            var port: Long? = null
            var auth: Boolean? = null
            var startTLS: Boolean? = null
            if (service == null) {
                type = section.getEnum("type", EmailFieldType::class)
                host = section.getString("host")
                port = section.getLong("port")
                auth = section.getBoolean("auth")
                startTLS = section.getBoolean("starttls")
            }
            val authSection = section.getConfigurationSection("auth-credentials")
            val authSUsername = authSection!!.getString("username")
            val authSPassword = authSection.getString("password")
            return Account(address, type, host, port, auth, startTLS, path, authSUsername, authSPassword, service?.host)
        }
        return null
    }

    fun loadTemplates() {
        try {
            val file = File(this.plugin.dataFolder, "templates")
            val fileStream = Files.walk(file.toPath())
            fileStream.forEach {
                val f = it.toFile()
                if (f.isFile) { TEMPLATES[f.name] = Files.readString(it) }
            }
        } catch (ex: Exception) {
            SkriptMail.logger().exception(ex, null)
        }
    }

    fun loadConfigs() {
        try {
            PROJECT_DEBUG = this.config.getBoolean("project-debug")
            EMAIL_DEBUG = this.config.getBoolean("email-debug")
            val list: MutableList<Account> = mutableListOf()
            val section = this.config.getConfigurationSection("accounts")
            val keys = section!!.getKeys(false)
            if (keys.isNotEmpty()) { for (key in keys) { getExactRecord(section, key)?.let { list.add(it) } } }
            ACCOUNTS = list
        } catch (ex: Exception) {
            SkriptMail.logger().exception(ex, msg = null)
        }
    }

    fun loadMailboxSettings() {
        try {
            val mailSection = this.config.getConfigurationSection("mailbox")
            if (mailSection != null) {
                MAILBOX_ENABLED = mailSection.getBoolean("enabled")
                MAILBOX_FILTER = mailSection.getString("filter")?.let { Regex(it) }
                MAILBOX_REFRESH_RATE = mailSection.getLong("refresh-rate")
                MAILBOX_BATCH_PER_REQUEST = mailSection.getLong("max-fetch-per-request")
                MAILBOX_RATE_UNIT = mailSection.getString("rate-unit")?.let { TimeUnit.valueOf(it.uppercase())}!!
                MAILBOX_FOLDERS = mailSection.getStringList("folders")

                if (MAILBOX_BATCH_PER_REQUEST > 100) {
                    SkriptMail.logger().warn("&eYou have set $MAILBOX_BATCH_PER_REQUEST a value greater than 100. items for a single query to the email server! This can cause performance issues")
                }
            }
        } catch (ex: Exception) {
            SkriptMail.logger().exception(ex, msg = null)
        }
    }

    fun initializeBStats(id: Long) {
        val metricsPrefix = "&#e3e512M&#a6e247e&#6cda6et&#2ece8dr&#00bfa4i&#00afafc&#329dads&r"
        val metrics = Metrics(plugin, id.toInt())
        metrics.addCustomChart(SimplePie(
            "skript_version"
        ) { Skript.getVersion().toString() })
        metrics.addCustomChart(SimplePie(
            "skriptmail_version"
        ) { plugin.pluginMeta.version })
        SkriptMail.logger().info("%s was hooked successfully", metricsPrefix)
    }

    private fun matchConfiguration() {
        try {
            var hasUpdated = false
            val stream = this.plugin.getResource(configFile!!.name)
            if (stream != null) {
                val isStream = InputStreamReader(stream)
                val defConfig = YamlConfiguration.loadConfiguration(isStream)
                for (key in defConfig.getConfigurationSection("")!!.getKeys(false)) {
                    if (!this.config.contains(key)) {
                        config.set(key, defConfig.get(key))
                        hasUpdated = !hasUpdated
                    }
                }
                for (key in config.getConfigurationSection("")!!.getKeys(false)) {
                    if (!defConfig.contains(key)) {
                        config[key] = null
                        hasUpdated = true
                    }
                }
                if (hasUpdated) config.save(configFile!!)
            }
        } catch (ex: Exception) {
            SkriptMail.logger().error(ex)
        }
    }

    fun registerCommand(self: JavaPlugin, command: String): Any? {
        try {
            return self.getCommand(command)?.setExecutor(Commands(SkriptMail.logger(), this))
        } catch (err: Exception) {
            SkriptMail.logger().error(err)
        }
        return null
    }

    companion object {
        var executedEmails: MutableMap<Date, Email> = mutableMapOf()
    }
}
