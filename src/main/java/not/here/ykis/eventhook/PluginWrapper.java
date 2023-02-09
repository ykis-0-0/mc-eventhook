package not.here.ykis.eventhook;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.annotation.plugin.*;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.bukkit.plugin.java.annotation.dependency.Library;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

/**
 * The entry point of the whole plugin,
 * a wrapper existing for the purpose of
 * properly generating the plugin.yml
 */
// Base Information for plugin.yml
@Plugin(name = "EventHook", version = "0.0-alpha")
@ApiVersion(ApiVersion.Target.v1_15)
@Author(value = "ykis-0-0")
@LogPrefix(value = Constants.LOG_PREFIX)
@LoadOrder(PluginLoadOrder.STARTUP)
public class PluginWrapper extends JavaPlugin {
  private final CommandDispatcher cmdDispatcher = new CommandDispatcher();
  private final Registry registry = new Registry(this);
  private final PluginController controller = new PluginController(this, this.registry);

  //#region External Lifecycle compliance
  @Override
  public void onLoad() {
    // In case for first launch
    //noinspection ResultOfMethodCallIgnored
    this.getDataFolder().mkdir();

    this.controller.registerSubcommands(this.cmdDispatcher);
  }

  @Override
  public void onEnable() {
    //noinspection ConstantConditions
    this.getCommand(Constants.COMMAND_NAME).setExecutor(this.cmdDispatcher);

    // Provide an explanation if required
    if(!(new File(this.getDataFolder(), Constants.NAME_KTSFILE).exists())) {
      this.saveResource(Constants.NAME_KTSFILE, false);
      this.getLogger().warning(String.format(
        "Default %s saved, please check and edit accordingly",
        Constants.NAME_KTSFILE
      ));
    }

    // If suppressor exists skip loading
    if(new File(this.getDataFolder(), Constants.NAME_HOLDFILE).exists()) {
      this.getLogger().warning(String.format(
        "%s found in data folder, suppressing loading of scripts",
        Constants.NAME_HOLDFILE
      ));
      return;
    }

    /*
      Can't be primitive since it may return null,
      Can't == true since it's not primitive anymore,
      Can't .equals() also since it may be null again,
      *sigh*
     */
    Boolean loaded = this.controller.announceCommencement();
    if(Objects.equals(loaded, false))
      this.getLogger().severe("Unable to recruit runners, staying idle");
  }

  @Override
  public void onDisable() {
    this.controller.endOfEvent();
    this.getLogger().info("Disabled!");
  }
  //#endregion

  //#region Constructors for MockBukkit
  @SuppressWarnings("unused")
  public PluginWrapper() {
    super();
  }

  @SuppressWarnings("unused")
  public PluginWrapper(@NotNull JavaPluginLoader loader, @NotNull PluginDescriptionFile description, @NotNull File dataFolder, @NotNull File file) {
    super(loader, description, dataFolder, file);
  }
  //#endregion
}
