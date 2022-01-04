package not.here.ykis.eventhook

import org.bukkit.plugin.Plugin
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import java.io.File

/** Utility & Wrapper class to parse and validate the individual configuration entries  */
internal class ConfigHelper(private val plugin: Plugin, private val section: ConfigurationSection, private val name: String) {

  // Final Guard
  val event: Class<out Event>
    get() {
      val eventFQCN = this.section.getString("event.class") ?: throw NoSuchElementException("Target Event not specified")
      val eventClass: Class<out Event> = try {
        Class.forName(eventFQCN).asSubclass(Event::class.java)
      } catch(e: Exception) {
        val exception = when(e) {
          is ClassNotFoundException -> IllegalArgumentException("Class not found: [%s]".format(eventFQCN))
          is ClassCastException -> IllegalArgumentException("Class [%s] is not (subclass of) ${Event::class.java.name}".format(eventFQCN))
          else -> RuntimeException("Unknown Error when retrieving class %s".format(eventFQCN))
        }
        throw exception
      }
      return eventClass
    }

  val priority: EventPriority
    get() {
      val priorityName = this.section.getString("event.priority")
        ?: throw NoSuchElementException("Runner Priority not specified")
      return try {
        EventPriority.valueOf(priorityName)
      } catch(e: IllegalArgumentException) {
        throw IllegalArgumentException("[%s] is not a valid EventPriority".format(priorityName))
      }
    }
  val execPath: String
    get() = this.section.getString("run.exec") ?: throw NoSuchElementException("Executable not specified")

  val workDir: File? by lazy {
    val path = this.section.getString("run.workdir") ?: return@lazy null
    val maybeDir = File(path)
    if(!maybeDir.isFile) return@lazy maybeDir
    with(this.plugin.logger) {
      severe("In config.yml [%s.%s]: Working Directory [%s] specified but not a valid directory".format(
        Constants.RUNNERS_CONTAINER, this.name, path
      ))
      warning("Ignoring this line")
    }
    // We aren't throwing, this shouldn't be a fatal error :thinking:
    null
  }

  val announce: Boolean
    get() = this.section.getBoolean("run.announce", false)

  val args: List<String>
    get() = this.section.getStringList("run.args")
}