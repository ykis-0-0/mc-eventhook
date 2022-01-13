package not.here.ykis.eventhook

import java.io.File
import java.util.ArrayList

import org.bukkit.plugin.Plugin
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.configuration.ConfigurationSection

/** The registry, as its name suggests, builds, keeps record of and deregisters individual [Athlete]s  */
internal class Registry(private val plugin: Plugin) : Listener {

  private val theRegister: ArrayList<Athlete> = ArrayList()

  // REVIEW: This thing will be redone in kotlin anyway lol
  /**
   * Create [Athlete]s from a given [ConfigurationSection]
   *
   * the `applicationForms` should be a mapping from a `name`
   * to the applicable parts of the arguments of [Athlete#Athlete][Athlete]
   *
   * @param applicationForms the relevant section of configuration
   * @return total number of entries parsed and constructed
   */
  fun processApplications(applicationForms: ConfigurationSection): Int {
    for (applicantName in applicationForms.getKeys(false)) {
      val application = applicationForms.getConfigurationSection(applicantName)!!
      val reader = ConfigHelper(plugin, application, applicantName)
      val eventClass: Class<out Event?>
      val eventPriority: EventPriority
      val execPath: String
      val workDir: File?
      val announce: Boolean
      val execArgs: List<String>
      try {
        eventClass = reader.event
        eventPriority = reader.priority
        execPath = reader.execPath
        workDir = reader.workDir
        announce = reader.announce
        execArgs = reader.args
      } catch (e: RuntimeException) {
        with(this.plugin.logger) {
          severe("In config.yml[%s.%s]: %s".format(
            Constants.RUNNERS_CONTAINER, applicantName, e.message
          ))
          warning("Entry Skipped")
        }
        continue
      }
      theRegister.add(Athlete(plugin, applicantName, eventClass, eventPriority, execPath, workDir, announce, execArgs))
    }
    return theRegister.size
  }

  /** Make each of the recorded [Athlete]s ready and register themselves to their corresponding [Event]s */
  fun makeReady() = theRegister.forEach { athlete -> athlete.onMyMark(this) }

  /** Centrally Unregisters all of the [Athlete]s from all of the [Event]s  */
  fun dismissParticipants() = HandlerList.unregisterAll(this)
}