package not.here.ykis.eventhook;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.InputStream;
import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.io.IOException;


/** A helper class for outsourcing the relaying of logs from the {@link Athlete}s */
class LoggingHelper implements Runnable {
  private final Logger logger;
  private final String target;
  private final Level level;
  private final BufferedReader lineReader;
  private boolean shouldStop;

  LoggingHelper(Plugin plugin, String target, Level level, InputStream stream) {
    this.logger = plugin.getLogger();
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
        this.logger.severe(String.format(
          "=>[%s] Error occured while relaying program output.",
          this.target
        ));
        return;
      }

      if(thisLine == null || this.shouldStop) break;

      this.logger.log(this.level, String.format(
        "=>[%s] %s", this.target, thisLine
      ));
    }
  }

  void end() {
    this.shouldStop = true;
  }
}
