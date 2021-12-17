/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package xxx.ykis.eventhook;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.event.EventPriority;
import org.bukkit.event.command.UnknownCommandEvent;

public class PluginMain extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("Enabled!");

        Class<UnknownCommandEvent> evt = org.bukkit.event.command.UnknownCommandEvent.class;
        PsuedoListener listener = new PsuedoListener();
        EventPriority priority = EventPriority.MONITOR;
        EventExecutor executor = new CustomExecutor(this.getLogger(), priority);

        this.getServer().getPluginManager().registerEvent(evt, listener, priority, executor, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled!");
    }
}
