package not.here.ykis.eventhook

@kotlin.script.experimental.annotations.KotlinScript(
  displayName = "Hooks Configuration",
  // fileExtension = "config.kts", // default kts
)
abstract class ScriptClosure(
  private val exportedAthletes: MutableSet<AthleteSpec<out org.bukkit.event.Event>>
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
    priority: org.bukkit.event.EventPriority,
    definition: HandlerBuilder<Te>.() -> Unit,
  ) {
    val handler = HandlerBuilder(eventClass, priority, name).apply(definition)
    this.exportedAthletes.add(handler.fixture)
  }
}
