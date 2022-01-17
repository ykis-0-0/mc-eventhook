package not.here.ykis.eventhook

import java.io.File

import org.bukkit.plugin.Plugin
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

/** The registry, as its name suggests, builds, keeps record of and (de)registers individual [Athlete]s  */
class Registry(private val plugin: Plugin) : Listener {

  private var theRegister: MutableSet<Athlete<*>> = mutableSetOf()
  val isLoaded: Boolean
    get() = this.theRegister.isNotEmpty()

  // May be able to extract into a standalone ConfigManager if we need to support a multi-file structure
  /**
   * Create [Athlete]s from a given [Iterable] of [AthleteSpec]
   *
   * the `applicationForms` should be a mapping from a `name`
   * to the applicable parts of the arguments of [Athlete#Athlete][Athlete]
   *
   * @return total number of entries parsed and constructed
   */
  fun processApplications(): Int {
    val sourceFile = File(this.plugin.dataFolder, Constants.NAME_KTSFILE)
    val config = ScriptProxyConfig(returns = mutableSetOf())
    val proxy = ScriptingProxy(this.plugin.logger, sourceFile)

    proxy.evalFile() ?: return 0

    this.theRegister = config.returns.mapTo(mutableSetOf()) {
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

}
