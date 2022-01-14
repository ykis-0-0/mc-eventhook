package not.here.ykis.eventhook

import java.io.File
import java.util.logging.Level

import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

import org.bukkit.plugin.Plugin
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

/** The registry, as its name suggests, builds, keeps record of and deregisters individual [Athlete]s  */
class Registry(private val plugin: Plugin) : Listener {

  private var theRegister: MutableSet<Athlete<*>> = mutableSetOf()
  val isLoaded: Boolean
    get() = !this.theRegister.isEmpty()

  // May be able to extract into a standalone ConfigManager if we need to support a multi-file structure
  /**
   * Create [Athlete]s from a given [Iterable] of [AthleteSpec]
   *
   * the `applicationForms` should be a mapping from a `name`
   * to the applicable parts of the arguments of [Athlete#Athlete][Athlete]
   *
   * @param applicationForms the collection of parsed data
   * @return total number of entries parsed and constructed
   */
  fun processApplications(): Int {
    val instances: MutableSet<AthleteSpec<out Event>> = mutableSetOf()

    val sourceCode = File(this.plugin.dataFolder, Constants.KTS_FILENAME).toScriptSource()
    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ScriptDef>()
    val evaluationConfiguration = object: ScriptEvaluationConfiguration({
      constructorArgs(instances)
    }) {}
    val maybeResults = BasicJvmScriptingHost().eval(sourceCode, compilationConfiguration, evaluationConfiguration)

    this.showDiagnostics(maybeResults.reports)
    this.showResults(maybeResults)

    this.theRegister = instances.mapTo(mutableSetOf()) {
      Athlete(plugin, it)
    }

    return this.theRegister.size
  }

  /** Make each of the recorded [Athlete]s ready and register themselves to their corresponding [Event]s */
  fun makeReady() = theRegister.forEach { athlete -> athlete.onMyMark(this) }

  /** Centrally Unregisters all of the [Athlete]s from all of the [Event]s  */
  fun dismissParticipants() {
    HandlerList.unregisterAll(this)
    this.theRegister.clear()
  }

  private fun showDiagnostics(reports: List<ScriptDiagnostic>) = with(this.plugin.logger) {
    info("Script finished with following diagnostics:")
    reports.forEach {
      val level = when(it.severity) {
        ScriptDiagnostic.Severity.DEBUG -> Level.FINE
        ScriptDiagnostic.Severity.INFO -> Level.INFO
        ScriptDiagnostic.Severity.WARNING -> Level.WARNING
        ScriptDiagnostic.Severity.ERROR -> Level.SEVERE
        ScriptDiagnostic.Severity.FATAL -> Level.SEVERE
      }
      val locationFragment = it.location?.start?.run { "[%3d:%2d]".format(this.line, this.col) } ?: ""
      log(level, "(%s)%s: %s".format(it.code, locationFragment, it.message))
    }
  }

  private fun showResults(maybeResults: ResultWithDiagnostics<EvaluationResult>) {
    when(maybeResults) {
      is ResultWithDiagnostics.Failure ->
        this.plugin.logger.warning("Script failed to evaluate")

      is ResultWithDiagnostics.Success ->
        when(val result = maybeResults.value.returnValue) {
          is ResultValue.Value -> {
            val returnValue = result.value
            this.plugin.logger.info(
              "Script returned a %s: %s".format(
                returnValue?.javaClass?.name ?: "null", returnValue.toString()
              )
            )
          }
          is ResultValue.Unit ->
            this.plugin.logger.info("Script returned without value")

          is ResultValue.Error -> {
            this.plugin.logger.severe("Script thrown an Exception:")
            val exception = result.error
            exception.printStackTrace()
          }

          ResultValue.NotEvaluated ->
            this.plugin.logger.info("Script is not evaluated")
        }
    }
  }

}
