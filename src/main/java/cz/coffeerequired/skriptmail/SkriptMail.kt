package cz.coffeerequired.skriptmail

import cz.coffeerequired.skriptmail.api.Config
import cz.coffeerequired.skriptmail.api.Logger
import cz.coffeerequired.skriptmail.api.Version
import cz.coffeerequired.skriptmail.api.gradient
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class SkriptMail : JavaPlugin() {
    private lateinit var config: Config
    private lateinit var logger: Logger

    fun logger(): Logger {
        return this.logger
    }

    override fun onLoad() {
        this.config = Config(this, Bukkit.getServer(), listOf(
            Version("1.8"),
            Version(1, 16, 2),
            Version(1, 20, 4)
        ))
        this.logger = Logger(this.config.getServerVersion())
        Log = this.logger()
        this.logger.setPrefix("${"skript-mail".gradient("#80F638", "#38F6B8")}&7")
        super.onLoad()
    }

    override fun onEnable() {
        instance =  this
        this.config.initializeSkript("Skript")
        this.config.initializeBStats(0)
        this.config.registerCommand(self = this, "skmail")
        this.config.classRegistration(self = this, "cz.coffeerequired.skriptmail.skript")
        this.config.initializeResources()
        this.config.loadTemplates()
        this.logger.log("Plugin was enabled %s", "&#52F638successfully.", sender = null)
    }

    override fun onDisable() {
    }

    companion object {
        lateinit var Log: Logger
        private lateinit var instance: SkriptMail
        fun gLogger(): Logger { return Log }
        fun instance(): SkriptMail { return this.instance }
    }
}
