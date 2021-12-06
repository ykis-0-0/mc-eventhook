package xxx.ykis.eventhook;

import java.util.logging.Logger;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

class CustomExecutor implements org.bukkit.plugin.EventExecutor {

  private Logger logger;
  private EventPriority priority;

  CustomExecutor(Logger logger, EventPriority priority) {
    this.logger = logger;
    this.priority = priority;
  }

  @Override
  public void execute(@NotNull Listener listener, @NotNull Event event) throws EventException {
    // TODO Auto-generated method stub
    this.logger.info(String.format("Received Event %s at priority %s", event.getClass().getCanonicalName(), priority.name()));
  }

}
