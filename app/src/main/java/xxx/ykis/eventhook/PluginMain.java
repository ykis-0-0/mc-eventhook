/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package xxx.ykis.eventhook;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import org.bukkit.plugin.java.annotation.plugin.*;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.bukkit.plugin.PluginLoadOrder;

// For MockBukkit
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.PluginDescriptionFile;


@Plugin(name = "EventHook", version = "0.0-alpha")
@ApiVersion(ApiVersion.Target.v1_16)
@Author(value = "ykis-0-0")
@LogPrefix(value = "EventHook(Test)")
@LoadOrder(PluginLoadOrder.STARTUP)
public class PluginMain extends JavaPlugin {

  private boolean checkConfSchema() {
    YamlConfiguration configTemplate = YamlConfiguration.loadConfiguration(this.getTextResource("defaults.yml"));
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

  private Athlete[] gatherAthletes() {
    if(!this.getConfig().isConfigurationSection("events")) {
      this.getLogger().severe("In config.yml[events]: Type mismatch, Map expected");
    }

    ConfigurationSection eventSection = this.getConfig().getConfigurationSection("events");
    ArrayList<Athlete> runners = new ArrayList<>();

    for(String atheleteId : eventSection.getKeys(false)) {
      ConfigurationSection data = eventSection.getConfigurationSection(atheleteId);
      ConfigSectionReader reader = new ConfigSectionReader(this, data, atheleteId);

      final Class<? extends Event> eventClass;
      final EventPriority eventPriority;
      final String execPath;
      final File workDir;
      final boolean announce;
      final List<String> execArgs;

      try {
        eventClass = reader.getEvent();
        eventPriority = reader.getPriority();
        execPath = reader.getExecPath();
        workDir = reader.getWorkDir();
        announce = reader.getAnnounce();
        execArgs = reader.getArgs();
      } catch (RuntimeException e) {
        this.getLogger().severe(e.getMessage());
        this.getLogger().warning("Entry Skipped");
        continue;
      }

      runners.add(new Athlete(this, atheleteId, eventClass, eventPriority, execPath, workDir, announce, execArgs));
    }

    return runners.toArray(new Athlete[0]);
  }

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

    Athlete[] runners = this.gatherAthletes();

    for(Athlete athelete : runners) {
      athelete.onMyMark();
    }
    this.getLogger().info(String.format("%d runners parsed successfully.", runners.length));
  }

  @Override
  public void onDisable() {
    this.getLogger().info("Disabled!");
  }

  // Constructors for MockBukkit
  public PluginMain() {
    super();
  }

  public PluginMain(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
    super(loader, description, dataFolder, file);
  }
}
