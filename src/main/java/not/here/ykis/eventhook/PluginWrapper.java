package not.here.ykis.eventhook;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.annotation.plugin.*;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * The entry point of the whole plugin,
 * a wrapper existing for the purpose of
 * properly generating the plugin.yml
 */
// Base Information for plugin.yml
@Plugin(name = "EventHook", version = "0.0-alpha")
@ApiVersion(ApiVersion.Target.v1_15)
@Author(value = "ykis-0-0")
@LogPrefix(value = "EventHook(Test)")
@LoadOrder(PluginLoadOrder.STARTUP)
public class PluginWrapper extends JavaPlugin {
  private final CommandDispatcher cmdDispatcher = new CommandDispatcher();
  private final Registry registry = new Registry(this);
  private final PluginController controller = new PluginController(this, this.registry);

  @Override
  public void onLoad() {
    this.controller.registerSubcommands(this.cmdDispatcher);
    //noinspection ConstantConditions
    this.getCommand(Constants.COMMAND_NAME).setExecutor(this.cmdDispatcher);

    // In case for first launch
    //noinspection ResultOfMethodCallIgnored
    this.getDataFolder().mkdir();
  }

  //#region External Lifecycle compliance
  @Override
  public void onEnable() {
    // Provide an explanation if required
    this.saveResource("config.kts", false);

    boolean loaded = this.controller.announceCommencement();
    if(!loaded)
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
