package xxx.ykis.eventhook;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.EventExecutor;

import java.util.List;
import java.util.stream.Stream;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

class Athlete implements Listener, EventExecutor, Runnable {

  private Plugin plugin;
  private Class<? extends Event> eventClass;
  private EventPriority eventPriority;
  private String execPath;
  private boolean announce;
  private List<String> args;
  private File workDir;


  public Athlete(Plugin plugin, Class<? extends Event> eventClass, EventPriority eventPriority,
      String execPath, File workDir, boolean announce, List<String> args) {
    this.plugin = plugin;

    this.eventClass = eventClass;
    this.eventPriority = eventPriority;

    this.execPath = execPath;
    this.workDir = workDir;
    this.announce = announce;
    this.args = args;
  }

  @Override
  public void execute(Listener listener, Event event) {
    new Thread(this).start();
  }

  void onMyMark() {
    Bukkit.getPluginManager().registerEvent(this.eventClass, this, this.eventPriority, this, this.plugin);
  }

  @Override
  public String toString() {
    String format = "Runner [%s %s] => \"%s\" (%s)";
    return String.format(format,
      this.eventPriority.name(), this.eventClass.getName(),
      this.execPath, String.join("; " , this.args)
    );
  }

  @Override
  public void run() {
    {
      String message = "Athlete heard signal [%s %s], start running %s";
      message = String.format(message,
        this.eventClass.getName(), this.eventPriority.name(),
        this.execPath
      );
      this.plugin.getLogger().info(message);
    }

    ProcessBuilder buildah = new ProcessBuilder();

    {
      Stream.Builder<String> preamble = Stream.builder();
      preamble.add(this.execPath);
      if(this.announce) {
        preamble.add(this.eventClass.getName());
      }
      buildah.command(Stream.concat(preamble.build(), this.args.stream()).toList());
    }

    if(this.workDir != null) {
      buildah.directory(this.workDir);
    }

    int exitCode = -1;
    try {
      exitCode = this.getExitCode(buildah.start()); // Fuck you Java
    } catch (IOException e) {
      e.printStackTrace();
      this.plugin.getLogger().severe("Unable to run process");
    }

    {
      String message = "Runner on %s finished %s with exit code of %d (seems %s)";
      message = String.format(message,
        this.eventClass.getName() , this.execPath,
        exitCode, exitCode == 0 ? "okay" : "failed"
      );
      this.plugin.getLogger().info(message);
    }
  }

  // Why this should ever happen? Fucking filthy
  private int getExitCode(Process runner){
    int exitCode = -1;
    try {
      exitCode = runner.waitFor();
    } catch (InterruptedException e) {
      e.printStackTrace();
      this.plugin.getLogger().severe("Interrupted, disquallifying the runner");
      runner.destroy();
    }
    return exitCode;
  }
}
