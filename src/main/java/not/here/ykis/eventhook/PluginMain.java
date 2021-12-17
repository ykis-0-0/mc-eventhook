/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package not.here.ykis.eventhook;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// For plugin.yml generation
import org.bukkit.plugin.java.annotation.plugin.*;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.bukkit.plugin.java.annotation.command.*;
import org.bukkit.plugin.PluginLoadOrder;

// For MockBukkit
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.PluginDescriptionFile;

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
// Commands
@Commands(
  @Command(name = Constants.COMMAND_NAME, desc = "Control the activation state of hooked scripts", usage = "Usage: /<command> <load | unload | reload | help>")
)
public class PluginMain extends JavaPlugin {

  private Registry registry = null;

  private boolean checkConfSchema() {
    YamlConfiguration configTemplate = YamlConfiguration.loadConfiguration(this.getTextResource("config.yml"));
    boolean permissible = true;
    boolean needSupplyDrop = false;

    int epochUsed = configTemplate.getInt("schema.epoch");
    if(epochUsed != this.getConfig().getInt("schema.epoch")){
      this.getLogger().warning("The existing config.yml is from another epoch, copying the new template.");
      permissible = false;
      needSupplyDrop = true;
    }

    int revisionUsed = configTemplate.getInt("schema.revision");
    if(revisionUsed != this.getConfig().getInt("schema.revision")){
      this.getLogger().warning("A newer revision of config.yml exists, copy a new template.");
      needSupplyDrop = true; // revision change isn't blocking
    }

    if(needSupplyDrop) {
      try {
        configTemplate.save(new File(this.getDataFolder(), "config.yml.newtemplate"));
        this.getLogger().info("New template saved as config.yml.newtemplate");
      } catch(IOException e) {
        e.printStackTrace();
        this.getLogger().severe("Could not save new configuration template");
      }
    }

    return permissible;
  }

  //#region Internal Lifecycle management
  private boolean announceCommencement() {
    if(this.registry != null) {
      this.getLogger.warning("There are runners standing by, please dismiss them before reload")
      return false;
    }
    if(!this.getConfig().isConfigurationSection("events")) {
      this.getLogger().severe("In config.yml[events]: Type mismatch, Map expected");
      return false;
    }

    Registry registry = new Registry(this);

    ConfigurationSection applicationForms = this.getConfig().getConfigurationSection("events");

    int countAthletes = registry.processApplications(applicationForms);

    this.getLogger().info(String.format(
      "%d applications received, of which %d are approved",
      applicationForms.getKeys(false).size(), countAthletes
    ));

    this.registry = registry;

    this.registry.makeReady();

    this.getLogger().info("All runners on their position.");
    return true;
  }

  private boolean endOfEvent() {
    if(this.registry == null) {
      this.getLogger().warning("There is no waiting runners, no need to demobilize");
      return false;
    }

    this.registry.dismissParticipants();
    this.registry = null;
    return true;
  }
  //#endregion

  @Override
  public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
    if(sender instanceof Player) {
      sender.sendMessage("This command is intended for use in server console only");
      return false;
    }

    if(args.length != 1) {
      sender.sendMessage(String.format(
        "Too %s arguments!",
        args.length < 1 ? "few" : "many"
      ));
      return false;
    }

    String action = args[0];
    if(!java.util.Arrays.asList(new String[] {"load", "unload", "reload"}).contains(action)) {
      if(!action.equals("help")) sender.sendMessage(String.format(
        "%s is not a valid action", action
      ));
      return false;
    }

    if(action.equals("unload") || action.equals("reload")) {
      boolean result = this.endOfEvent();
      sender.sendMessage(result
        ? "Configuration unloaded"
        : "There is nothing left to unload"
      );
    }
    if(action.equals("load") || action.equals("reload")) {
      this.reloadConfig();
      boolean result = this.announceCommencement();
      sender.sendMessage(result
        ? "Configuration loaded"
        : "A loaded configuration exists"
      );
    }

    return true;
  }
  //#endregion

  //#region External Lifecycle compliance
  @Override
  public void onEnable() {
    this.getLogger().info("Enabled!");

    // In case for first launch
    this.getDataFolder().mkdir();
    this.saveDefaultConfig();
    this.getConfig().options().copyDefaults(false);

    // If the config.yml schema isn't compatible
    if(!this.checkConfSchema()) {
      this.getLogger().warning("Disabling myself, see you next time~");
      this.setEnabled(false);
      return;
    }

    this.getCommand(Constants.COMMAND_NAME).setExecutor(this);
    boolean loaded = this.announceCommencement();
  }

  @Override
  public void onDisable() {
    this.endOfEvent();

    this.getLogger().info("Disabled!");
  }
  //#endregion

  //#region Constructors for MockBukkit
  public PluginMain() {
    super();
  }

  public PluginMain(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
    super(loader, description, dataFolder, file);
  }
  //#endregion
}
