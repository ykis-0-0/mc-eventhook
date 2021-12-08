package xxx.ykis.eventhook;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.EventExecutor;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

class Athlete implements Listener, EventExecutor, Runnable {

  private final Plugin plugin;
  private final String name;
  private final Class<? extends Event> eventClass;
  private final EventPriority eventPriority;
  private final String execPath;
  private final boolean announce;
  private final List<String> args;
  private final File workDir;


  public Athlete(Plugin plugin, String name, Class<? extends Event> eventClass, EventPriority eventPriority,
      String execPath, File workDir, boolean announce, List<String> args) {
    this.plugin = plugin;
    this.name = name;

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
    String format = "Runner %s [%s %s] => \"%s\" (%s)";
    return String.format(format,
      this.name,
      this.eventPriority.name(), this.eventClass.getName(),
      this.execPath, String.join("; " , this.args)
    );
  }

  private void reportStart() {
    String message = "Athlete %s heard signal [%s %s], start running %s";
    message = String.format(message,
      this.name,
      this.eventClass.getName(), this.eventPriority.name(),
      this.execPath
    );
    this.plugin.getLogger().info(message);
  }

  private void reportEnd(int exitCode){
    String message = "Runner %s on %s finished %s with exit code of %d (seems %s)";
    message = String.format(message,
      this.name,
      this.eventClass.getName() , this.execPath,
      exitCode, exitCode == 0 ? "okay" : "failed"
    );
    this.plugin.getLogger().info(message);
  }

  private List<String> prepCmdline() {
    Stream.Builder<String> preamble = Stream.builder();

    preamble.add(this.execPath);
    if(this.announce) {
      preamble.add(this.eventClass.getName());
    }

    return Stream.concat(preamble.build(), this.args.stream()).collect(Collectors.toList());
  }

  @Override
  public void run() {
    this.reportStart();

    ProcessBuilder buildur = new ProcessBuilder();
    buildur.command(this.prepCmdline());

    if(this.workDir != null) {
      buildur.directory(this.workDir);
    }

    int exitCode = -1;

    try {
      Process proc = buildur.start();

      Referee chief = new Referee(this.plugin, this.name, Level.INFO, proc.getInputStream());
      Referee side = new Referee(this.plugin, this.name, Level.SEVERE, proc.getErrorStream());

      Thread stdout = new Thread(chief);
      Thread stderr = new Thread(side);

      stdout.start();
      stderr.start();

      exitCode = this.getExitCode(proc); // Fuck you Java
    } catch (IOException e) {
      e.printStackTrace();
      this.plugin.getLogger().severe("Unable to run process");
    }

    this.reportEnd(exitCode);
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
