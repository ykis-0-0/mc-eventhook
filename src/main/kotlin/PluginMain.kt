/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package not.here.ykis.eventhook

import org.bukkit.plugin.java.JavaPlugin

import java.io.File
import java.io.IOException

import org.bukkit.configuration.file.YamlConfiguration

import org.bukkit.command.CommandSender

// For plugin.yml generation
import org.bukkit.plugin.java.annotation.plugin.*
import org.bukkit.plugin.java.annotation.plugin.author.Author
import org.bukkit.plugin.PluginLoadOrder

// For MockBukkit
import org.bukkit.plugin.java.JavaPluginLoader
import org.bukkit.plugin.PluginDescriptionFile

/**
 * The entry point to the whole plugin,
 * the manager of the whole plugin lifecycle,
 * the interface to commands from the outside,
 * and the first-stage guard of the configuration schema.
 */
// Base Information for plugin.yml
@Plugin(name = "EventHook", version = "0.0-alpha")
@ApiVersion(ApiVersion.Target.v1_15)
@Author(value = "ykis-0-0")
@LogPrefix(value = "EventHook(Test)")
@LoadOrder(PluginLoadOrder.STARTUP)
class PluginMain : JavaPlugin {

  private var registry: Registry? = null
  private val commandManager: CommandManager = CommandManager()

  private fun checkConfSchema(): Boolean {
    val configTemplate = YamlConfiguration.loadConfiguration(getTextResource("config.yml")!!)
    var permissible = true
    var needSupplyDrop = false

    val epochUsed = configTemplate.getInt("schema.epoch")
    if (epochUsed != this.config.getInt("schema.epoch")) {
      this.logger.warning("The existing config.yml is from another epoch, copying the new template.")
      permissible = false
      needSupplyDrop = true
    }

    val revisionUsed = configTemplate.getInt("schema.revision")
    if (revisionUsed != this.config.getInt("schema.revision")) {
      this.logger.warning("A newer revision of config.yml exists, copy a new template.")
      needSupplyDrop = true // revision change isn't blocking
    }

    if (needSupplyDrop) {
      try {
        configTemplate.save(File(this.dataFolder, "config.yml.newtemplate"))
        this.logger.info("New template saved as config.yml.newtemplate")
      } catch (e: IOException) {
        e.printStackTrace()
        this.logger.severe("Could not save new configuration template")
      }
    }

    return permissible
  }

  //#region Internal Lifecycle management
  private fun announceCommencement(): Boolean {
    if (this.registry != null) {
      this.logger.warning("There are runners standing by, please dismiss them before reload")
      return false
    }

    if (!this.config.isConfigurationSection(Constants.RUNNERS_CONTAINER)) {
      this.logger.severe(String.format(
        "In config.yml[%s]: Type mismatch, Map expected",
        Constants.RUNNERS_CONTAINER
      ))
    }

    val registry = Registry(this)
    val applicationForms = this.config.getConfigurationSection(Constants.RUNNERS_CONTAINER)!! // Checked in l#88
    val countAthletes = registry.processApplications(applicationForms)

    this.logger.info("%d applications received, of which %d are approved".format(
      applicationForms.getKeys(false).size, countAthletes
    ))

    this.registry = registry
    this.registry!!.makeReady()
    this.logger.info("All runners on their position.")

    return true
  }

  private fun endOfEvent(): Boolean {
    if (this.registry == null) {
      this.logger.warning("There is no waiting runners, no need to demobilize")
      return false
    }

    this.registry!!.dismissParticipants()
    this.registry = null

    return true
  }

  //#endregion

  private fun prepCommand() {
    val wrapper: ((CommandSender) -> Boolean).() -> (String) -> (CommandSender, Array<String>) -> Boolean
        = wrapped@ { { callSite@ { sender, args -> // Using _ since we don't need to use the label for now
          if (args.isNotEmpty()) {
            sender.sendMessage("Too many arguments")
            return@callSite false
          }

          this@wrapped(sender)
        } } }

    val loadAction: (CommandSender) -> Boolean = { sender ->
      this.reloadConfig()
      val result = this.announceCommencement()

      sender.sendMessage(
        if(result) "Configuration loaded"
        else "A loaded configuration already exists"
      )
      true
    }

    val unloadAction: (CommandSender) -> Boolean = { sender ->
      val result = this.endOfEvent()
      sender.sendMessage(
        if(result) "Configuration unloaded"
        else "There is nothing left to unload"
      )

      true
    }

    val reloadAction: (CommandSender) -> Boolean = { sender ->
      unloadAction(sender)
      loadAction(sender)
      true
    }

    this.commandManager.register("help", { _: CommandSender -> false }.wrapper())
    this.commandManager.register("load", loadAction.wrapper())
    this.commandManager.register("unload", unloadAction.wrapper())
    this.commandManager.register("reload", reloadAction.wrapper())

    this.getCommand(Constants.COMMAND_NAME)!!.setExecutor(this.commandManager)
  }

  //#region External Lifecycle compliance
  override fun onEnable() {
    this.logger.info("Enabled!")
    this.prepCommand()

    // In case for first launch
    this.dataFolder.mkdir()
    this.saveDefaultConfig()
    // It's default is already false
    // this.getConfig().options().copyDefaults(false);

    // If the config.yml schema isn't compatible
    if (!this.checkConfSchema()) {
      this.logger.warning("Disabling myself, see you next time~")
      this.isEnabled = false
      return
    }
    if (!this.config.isSet(Constants.RUNNERS_CONTAINER)) {
      this.logger.warning(String.format(
        "Section '%s' not found in config.yml, fill in configurations then do /%s load",
        Constants.RUNNERS_CONTAINER, Constants.COMMAND_NAME
      ))
      this.logger.warning("Skipping remaining initializations")
      return
    }
    val loaded = this.announceCommencement()
    if (!loaded) {
      this.logger.severe("Unable to recruit runners, staying idle")
      return
    }
  }

  override fun onDisable() {
    this.endOfEvent()
    this.logger.info("Disabled!")
  }
  //#endregion

  //#region Constructors for MockBukkit
  @Suppress("Unused")
  constructor() : super()
  @Suppress("Unused")
  constructor(loader: JavaPluginLoader, description: PluginDescriptionFile, dataFolder: File, file: File) : super(loader, description, dataFolder, file)
  //#endregion
}