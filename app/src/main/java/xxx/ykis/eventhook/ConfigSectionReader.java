package xxx.ykis.eventhook;

import java.io.File;
import java.util.List;

import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

class ConfigSectionReader {
  private final Plugin plugin;
  private final ConfigurationSection section;
  private final String name;

  ConfigSectionReader(Plugin plugin, ConfigurationSection section, String name) {
    this.plugin = plugin;
    this.section = section;
    this.name = name;
  }

  Class<? extends Event> getEvent() {
    final String eventFQCN = section.getString("event.class");
    String message = null;

    if(eventFQCN == null) {
      message = String.format("In config.yml[events.%s]: Target Event not specified", this.name);
      throw new RuntimeException(message);
    }

    Class<? extends Event> eventClass = null;

    try {
      final Class<?> classInput = Class.forName(eventFQCN);
      eventClass = classInput.asSubclass(Event.class);
    } catch (ClassNotFoundException e) {
      message = String.format("In config.yml[events.%s]: Class not found: [%s]", this.name, eventFQCN);
    } catch (ClassCastException e) {
      message = String.format("In config.yml[events.%s]: Class [%s] is not (subclass of) [%s]", this.name, eventFQCN, Event.class.getName());
    } finally {
      if(eventClass == null) { // Final Guard
        message = String.format("In config.yml[events.%s]: Unable to retrieve class object: [%s]", this.name, eventFQCN);
      }
      if(message != null) {
        throw new RuntimeException(message);
      }
    }

    return eventClass;
  }

  EventPriority getPriority() {
    final String priorityName = section.getString("event.priority");
    String message = null;

    if(priorityName == null) {
      message = String.format("In config.yml[events.%s]: Runner Priority not specified", this.name);
      throw new RuntimeException(message);
    }

    final EventPriority priority;

    try {
      priority = EventPriority.valueOf(priorityName);
    } catch (IllegalArgumentException e) {
      message = String.format("In config.yml[events.%s]: [%s] is not a valid EventPriority", this.name, priorityName);
      throw new RuntimeException(message);
    }

    return priority;
  }

  String getExecPath() {
    final String execPath = section.getString("run.exec");

    if(execPath == null) {
      String message = String.format("In config.yml[events.%s]: Executable not specified", this.name);
      throw new RuntimeException(message);
    }

    return execPath;
  }

  File getWorkDir() {
    final String workDirPath = section.getString("run.workdir");

    File workDir = workDirPath == null ? null : new File(workDirPath);
    if(workDir != null && !workDir.isDirectory()) {
      String message = String.format("In config.yml[events.%s]: Working Directory [%s] specified but not a valid directory", this.name, workDir);
      this.plugin.getLogger().severe(message);
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
