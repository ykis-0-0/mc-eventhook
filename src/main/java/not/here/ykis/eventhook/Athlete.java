package not.here.ykis.eventhook;

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

/** A single executor, reacting to a single event, and running a single task */
class Athlete implements EventExecutor, Runnable {

  private final Plugin plugin;
  private final String name;
  private final Class<? extends Event> eventClass;
  private final EventPriority eventPriority;
  private final String execPath;
  private final File workDir;
  private final boolean announce;
  private final List<String> args;


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

  /**
   * Register itself as an {@link EventExecutor} of the specified {@link Event}
   * @param commander The {@link Registry} it affiliates with
   */
  void onMyMark(Registry commander) {
    Bukkit.getPluginManager().registerEvent(this.eventClass, commander, this.eventPriority, this, this.plugin);
  }

  @Override
  public String toString() {
    return String.format(
      "Runner %s [%s %s] => \"%s\" (%s)",
      this.name,
      this.eventPriority.name(), this.eventClass.getName(),
      this.execPath, String.join("; " , this.args)
    );
  }

  /** Log its start */
  private void reportStart() {
    this.plugin.getLogger().info(String.format(
      "Athlete %s heard signal [%s %s], start running %s",
      this.name,
      this.eventClass.getName(), this.eventPriority.name(),
      this.execPath
    ));
  }

  /** Log its finish */
  private void reportEnd(int exitCode){
    this.plugin.getLogger().info(String.format(
      "Runner %s on %s finished %s with exit code of %d (seems %s)",
      this.name,
      this.eventClass.getName() , this.execPath,
      exitCode, exitCode == 0 ? "okay" : "failed"
    ));
  }

  /** Constuct the command line from the arguments and options given */
  private List<String> prepCmdline() {
    Stream.Builder<String> preamble = Stream.builder();

    preamble.add(this.execPath);
    if(this.announce) {
      preamble.add(this.eventClass.getName());
    }

    return Stream.concat(preamble.build(), this.args.stream()).collect(Collectors.toList());
  }

  /** Start the task, while also setting up {@link LoggingHelper}s to relay the programs output to the log */
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

      LoggingHelper chief = new LoggingHelper(this.plugin, this.name, Level.INFO, proc.getInputStream());
      LoggingHelper side = new LoggingHelper(this.plugin, this.name, Level.SEVERE, proc.getErrorStream());

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

  /**
   * Internal helper method to wait for the {@link Process} to exit with the illusion of successfully avoided nesting try and catch clauses
   *
   * tbh why should this ever happen in the first place? Fucking filthy
   * @param runner The relevant {@link Process} started
   * @return Exit code of the process, or {@code -1} if the process was interrupted
   */
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
