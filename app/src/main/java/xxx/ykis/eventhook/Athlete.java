package xxx.ykis.eventhook;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.EventExecutor;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Athlete implements EventExecutor, Runnable {

  private Plugin plugin;
  private String name;
  private Class<? extends Event> eventClass;
  private EventPriority priority;
  private String execPath;
  private List<String> args;

  public Athlete(Plugin plugin, String name, Class<? extends Event> eventClass, EventPriority priority, String execPath, List<String> args) {
    this.plugin = plugin;
    this.name = name;
    this.eventClass = eventClass;
    this.priority = priority;
    this.execPath = execPath;
    this.args = args;
  }

  @Override
  public void execute(Listener listener, Event event) {
    // TODO Auto-generated method stub
    new Thread(this).start();
  }

  @Override
  public void run() {
    // TODO Auto-generated method stub
    String message = "Runner %s starting on event [%s] with priority %s";
    message = String.format(message, this.name, this.eventClass.getName(), this.priority.name());
    this.plugin.getLogger().info(message);

    /*ArrayList<String> cmdline = new ArrayList<>();
    cmdline.add(execPath);
    cmdline.addAll(args);

    ProcessBuilder builder = new ProcessBuilder(cmdline);
    builder.start();*/
    this.plugin.getLogger().info(String.format("Runner %s finished", this.name));
  }

  @Override
  public String toString() {
    String format = "Runner [%s %s] => \"%s\" (%s)";
    return String.format(format, priority.name(), eventClass.getName(), execPath, String.join("; " , args));
  }

  public void onMyMark() {
    Bukkit.getPluginManager().registerEvent(this.eventClass, new PsuedoListener(), this.priority, this, this.plugin);
  }
}
