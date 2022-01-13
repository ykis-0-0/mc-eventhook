package not.here.ykis.eventhook

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor

/** A single executor, reacting to a single event, and running a single task  */
internal class Athlete<Te: Event>(
  private val plugin: Plugin,
  spec: AthleteSpec<Te>
) : EventExecutor {
  private val name = spec.name
  private val eventClass = spec.eventClass
  private val eventPriority = spec.priority
  private val predicate = spec.predicate
  private val executor = spec.closure(this.plugin.logger)

  override fun toString(): String = "Runner %s [%s %s]".format(
    this.name,
    this.eventPriority.name, this.eventClass.name
  )

  /**
   * Register itself as an [EventExecutor] of the specified [Event]
   * @param dispatcher The [Registry] it affiliates with
   */
  fun onMyMark(dispatcher: Registry) {
    Bukkit.getPluginManager().registerEvent(this.eventClass, dispatcher, this.eventPriority, this, this.plugin)
  }

  private fun reportStart() = plugin.logger.info(
    "Athlete %s heard signal [%s] and started running".format(
      this.name,
      this.eventClass.name, this.eventPriority.name
    )
  )

  private fun reportEnd() = plugin.logger.info(
    "Athlete %s finished".format(this.name)
  )

  override fun execute(listener: Listener, event: Event) {
    @Suppress("UNCHECKED_CAST")
    val eventArg: Te = event as Te
    if(!this.predicate(eventArg)) return

    this.getThreadObj(event).start()
  }

  private fun getThreadObj(event: Te) = Thread {
    this@Athlete.reportStart()
    this@Athlete.executor(event)
    this@Athlete.reportEnd()
  }
}
