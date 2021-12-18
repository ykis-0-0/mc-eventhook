package not.here.ykis.eventhook;

import java.util.ArrayList;

import org.bukkit.plugin.Plugin;
import org.bukkit.event.Listener;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;

import java.io.File;
import java.util.List;

/** The registry, as its name suggests, builds, keeps record of and deregisters individual {@link Athlete}s */
class Registry implements Listener {

  private Plugin plugin;
  private ArrayList<Athlete> theRegister;

  Registry(Plugin plugin) {
    this.plugin = plugin;
    this.theRegister = new ArrayList<>();
  }

  /**
   * Create {@link Athlete}s from a given {@link ConfigurationSection}
   *
   * the {@code applicationForms} should be a mapping from a {@code name}
   * to the applicable parts of the arguments of {@link Athlete#Athlete(Plugin, String, Class, EventPriority, String, File, boolean, List) Athlete#Athlete}
   *
   * @param applicationForms the relevant section of configuration
   * @return total number of entries parsed and constructed
   */
  int processApplications(ConfigurationSection applicationForms) {
    for(String applicantName : applicationForms.getKeys(false)) {
      ConfigurationSection application = applicationForms.getConfigurationSection(applicantName);
      ConfigHelper reader = new ConfigHelper(this.plugin, application, applicantName);

      final Class<? extends Event> eventClass;
      final EventPriority eventPriority;
      final String execPath;
      final File workDir;
      final boolean announce;
      final List<String> execArgs;

      try {
        eventClass = reader.getEvent();
        eventPriority = reader.getPriority();
        execPath = reader.getExecPath();
        workDir = reader.getWorkDir();
        announce = reader.getAnnounce();
        execArgs = reader.getArgs();
      } catch (RuntimeException e) {
        this.plugin.getLogger().severe(String.format(
          "In config.yml[%s.%s]: %s",
          Constants.RUNNERS_CONTAINER, applicantName, e.getMessage()
        ));
        this.plugin.getLogger().warning("Entry Skipped");
        continue;
      }

      this.theRegister.add(new Athlete(this.plugin, applicantName, eventClass, eventPriority, execPath, workDir, announce, execArgs));
    }

    return this.theRegister.size();
  }

  /** Make each of the recorded {@link Athlete}s ready and register themselves to their corresponding {@link Event}s*/
  void makeReady() {
    this.theRegister.forEach(athlete -> athlete.onMyMark(this));
  }

  /** Centrally Unregisters all of the {@link Athlete}s from all of the {@link Event}s */
  void dismissParticipants() {
    HandlerList.unregisterAll(this);
  }
}
