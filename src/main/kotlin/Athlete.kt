package not.here.ykis.eventhook

import org.bukkit.event.EventPriority
import org.bukkit.plugin.EventExecutor
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import java.util.logging.Level

/** A single executor, reacting to a single event, and running a single task  */
internal class Athlete(
  private val plugin: Plugin, private val name: String,
  private val eventClass: Class<out Event>, private val eventPriority: EventPriority,
  private val execPath: String, private val workDir: java.io.File?,
  private val announce: Boolean, private val args: List<String>
) : EventExecutor, Runnable {

  // REVIEW: do we need to expose the real triggered event?
  override fun execute(listener: Listener, event: Event) {
    Thread(this).start()
  }

  /**
   * Register itself as an [EventExecutor] of the specified [Event]
   * @param commander The [Registry] it affiliates with
   */
  fun onMyMark(commander: Registry) {
    Bukkit.getPluginManager().registerEvent(eventClass, commander, eventPriority, this, plugin)
  }

  override fun toString(): String = "Runner %s [%s %s] => \"%s\" (%s)".format(
    name,
    eventPriority.name, eventClass.name,
    execPath, args.joinToString("; ")
  )

  /** Log its start  */
  private fun reportStart() = plugin.logger.info(
    "Athlete %s heard signal [%s %s], start running %s".format(
      name,
      eventClass.name, eventPriority.name,
      execPath
    )
  )

  /** Log its finish  */
  private fun reportEnd(exitCode: Int) = plugin.logger.info(
    "Runner %s on %s finished %s with exit code of %d (seems %s)".format(
      name,
      eventClass.name, execPath,
      exitCode, if (exitCode == 0) "okay" else "failed"
    )
  )

  /** Construct the command line from the arguments and options given  */
  private fun prepCmdline(): List<String> = buildList {
    add(execPath)
    if (announce) add(eventClass.name)
    addAll(this@Athlete.args)
  }

  /** Start the task, while also setting up [LoggingHelper]s to relay the programs output to the log  */
  override fun run() {
    this.reportStart()

    val buildur = ProcessBuilder()
    buildur.command(prepCmdline())

    this.workDir?.let { buildur.directory(it) }

    @Suppress("CanBeVal")
    var exitCode:Int = -1

    try {
      val proc = buildur.start()

      val chief = LoggingHelper(plugin, name, Level.INFO, proc.inputStream).apply { Thread(this@apply).start() }
      val side = LoggingHelper(plugin, name, Level.SEVERE, proc.errorStream).apply { Thread(this@apply).start() }

      exitCode = this.getExitCode(proc) // Fuck you Java
    } catch (e: java.io.IOException) {
      e.printStackTrace()
      this.plugin.logger.severe("Unable to run process")
    }
    this.reportEnd(exitCode)
  }

  /**
   * Internal helper method to wait for the [Process] to exit with the illusion of successfully avoided nesting try and catch clauses
   *
   * tbh why should this ever happen in the first place? Fucking filthy
   * @param runner The relevant [Process] started
   * @return Exit code of the process, or `-1` if the process was interrupted
   */
  private fun getExitCode(runner: Process): Int = try {
    runner.waitFor()
  } catch (e: java.lang.InterruptedException) {
    e.printStackTrace()
    plugin.logger.severe("Interrupted, disquallifying the runner")
    runner.destroy()
    -1
  }
}