package not.here.ykis.eventhook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

/** A helper class for outsourcing the relaying of logs from the {@link Athlete}s */
class LoggingHelper implements Runnable {
  private final Plugin plugin;
  private final String target;
  private final Level level;
  private final BufferedReader lineReader;
  private boolean shouldStop;

  LoggingHelper(Plugin plugin, String target, Level level, InputStream stream) {
    this.plugin = plugin;
    this.target = target;
    this.level = level;
    this.lineReader = new BufferedReader(new InputStreamReader(stream));
    this.shouldStop = false;
  }

  @Override
  public void run() {
    String thisLine = "";
    while(true) {
      try {
        thisLine = lineReader.readLine();
      } catch (IOException e) {
        e.printStackTrace();
        this.plugin.getLogger().severe(String.format(
          "=>[%s] Error occured while relaying program output.",
          this.target
        ));
        return;
      }

      if(thisLine == null || this.shouldStop) break;
      String outLine = String.format("=>[%s] %s", this.target, thisLine);

      this.plugin.getLogger().log(this.level, outLine);
    }
  }

  void end() {
    this.shouldStop = true;
  }
}
