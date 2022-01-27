package not.here.ykis.eventhook

import java.io.File
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

import org.bukkit.plugin.Plugin
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

/** The registry, as its name suggests, builds, keeps record of and (de)registers individual [Athlete]s  */
class Registry(private val plugin: Plugin) : Listener {

  private var theRegister: MutableSet<Athlete<*>> = mutableSetOf()
  val isLoaded: Boolean
    get() = this.theRegister.isNotEmpty()

  /**
   * Create [Athlete]s from the specified .kts script file
   *
   * @return total number of entries parsed and constructed
   */
  fun processApplications(): Int {
    val sourceFile = File(this.plugin.dataFolder, Constants.NAME_KTSFILE)
    val host = BasicJvmScriptingHost()
    val config = ScriptProxyConfig(host, returns = mutableSetOf())
    val proxy = ScriptingProxy(this.plugin.logger, sourceFile, config)

    with(this.plugin.logger){
      when(val result = proxy.evalFile()) {
        is ResultValue.Value -> {
          val value = result.value
          warning("Script %s returned a %s: %s".format(
              sourceFile.name, value?.javaClass?.name ?: "null", value.toString()
          ))
          // TODO Maybe further handling here?
        }
        is ResultValue.Unit ->
          info("Script %s finished".format(sourceFile.name))
        is ResultValue.Error -> {
          severe("Script %s thrown an Exception:".format(sourceFile.name))
          severe(result.error.stackTraceToString())
          return -1
        }
        ResultValue.NotEvaluated ->
          warning("Script %s did not ran".format(sourceFile.name))
        null -> {
          severe("Script %s failed to run".format(sourceFile.name))
          return -1
        }
      }
    }

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
