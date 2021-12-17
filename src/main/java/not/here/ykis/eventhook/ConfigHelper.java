package not.here.ykis.eventhook;

import java.io.File;
import java.util.List;

import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

/** Utility & Wrapper class to parse and validate the individual configuration entries */
class ConfigHelper {
  private final Plugin plugin;
  private final ConfigurationSection section;
  private final String name;

  ConfigHelper(Plugin plugin, ConfigurationSection section, String name) {
    this.plugin = plugin;
    this.section = section;
    this.name = name;
  }

  Class<? extends Event> getEvent() {
    final String eventFQCN = section.getString("event.class");

    if(eventFQCN == null) {
      throw new RuntimeException(String.format(
        "In config.yml[%s.%s]: Target Event not specified",
        Constants.RUNNERS_CONTAINER, this.name
      ));
    }

    Class<? extends Event> eventClass = null;

    try {
      eventClass = Class.forName(eventFQCN).asSubclass(Event.class);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(String.format(
        "In config.yml[%s.%s]: Class not found: [%s]",
        Constants.RUNNERS_CONTAINER, this.name, eventFQCN
      ));
    } catch (ClassCastException e) {
      throw new RuntimeException(String.format(
        "In config.yml[%s.%s]: Class [%s] is not (subclass of) [%s]",
        Constants.RUNNERS_CONTAINER, this.name, eventFQCN, Event.class.getName()
      ));
    }

    if(eventClass == null) { // Final Guard
      throw new RuntimeException(String.format(
        "In config.yml[%s.%s]: Unable to retrieve class object: [%s]",
        Constants.RUNNERS_CONTAINER, this.name, eventFQCN
      ));
    }

    return eventClass;
  }

  EventPriority getPriority() {
    final String priorityName = section.getString("event.priority");

    if(priorityName == null) {
      throw new RuntimeException(String.format(
        "In config.yml[%s.%s]: Runner Priority not specified",
        Constants.RUNNERS_CONTAINER, this.name
      ));
    }

    final EventPriority priority;

    try {
      priority = EventPriority.valueOf(priorityName);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(String.format(
        "In config.yml[%s.%s]: [%s] is not a valid EventPriority",
        Constants.RUNNERS_CONTAINER, this.name, priorityName
      ));
    }

    return priority;
  }

  String getExecPath() {
    final String execPath = section.getString("run.exec");

    if(execPath == null) {
      throw new RuntimeException(String.format(
        "In config.yml[%s.%s]: Executable not specified",
        Constants.RUNNERS_CONTAINER, this.name
      ));
    }

    return execPath;
  }

  File getWorkDir() {
    final String workDirPath = section.getString("run.workdir");

    File workDir = workDirPath == null ? null : new File(workDirPath);
    if(workDir != null && !workDir.isDirectory()) {
      // This is not a fatal error, or we should delay it until execution
      this.plugin.getLogger().severe(String.format(
        "In config.yml[%s.%s]: Working Directory [%s] specified but not a valid directory",
        Constants.RUNNERS_CONTAINER, this.name, workDir
      ));
      this.plugin.getLogger().warning("Ignoring this line");
      workDir = null;
    }

    return workDir;
  }

  boolean getAnnounce() {
    return section.getBoolean("run.announce", false);
  }

  List<String> getArgs() {
    return section.getStringList("run.args");
  }
}
