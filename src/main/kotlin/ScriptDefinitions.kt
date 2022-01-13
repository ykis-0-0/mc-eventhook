package not.here.ykis.eventhook

import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

object ScriptHookConfig: ScriptCompilationConfiguration({
  defaultImports(org.bukkit.event.EventPriority::class)
  jvm {
    dependenciesFromCurrentContext(wholeClasspath = true)
  }
})

@kotlin.script.experimental.annotations.KotlinScript(
  displayName = "Hooks Configuration",
  fileExtension = "config.kts", // default kts
  compilationConfiguration = ScriptHookConfig::class
)
abstract class ScriptDef(
  private val exportedAthletes: MutableSet<AthleteSpec<out Event>>
) {

  /**
   * A Helper function for avoid repetition in specifying event class,
   * Designed as the main entry point for config scripts
   */
  //@OptIn(ExperimentalTypeInference::class)
  inline fun <reified Te: org.bukkit.event.Event> handler(
    priority: org.bukkit.event.EventPriority,
    name: String,
    //@BuilderInference
    noinline definition: HandlerBuilder<Te>.() -> Unit,
  ) = this.registerHandler(name, Te::class.java, priority, definition)

  fun <Te: org.bukkit.event.Event> registerHandler(
    name: String,
    eventClass: Class<Te>,
    priority: EventPriority,
    definition: HandlerBuilder<Te>.() -> Unit,
  ) {
    val handler = HandlerBuilder(eventClass, priority, name).apply(definition)
    this.exportedAthletes.add(handler.fixture)
  }
}