package xxx.ykis.eventhook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

class Referee implements Runnable {
  private final Plugin plugin;
  private final String target;
  private final Level level;
  private final BufferedReader lineReaader;
  private boolean enough;

  Referee(Plugin plugin, String target, Level level, InputStream stream) {
    this.plugin = plugin;
    this.target = target;
    this.level = level;
    this.lineReaader = new BufferedReader(new InputStreamReader(stream));
    this.enough = false;
  }

  @Override
  public void run() {
    String thisLine = "";
    while(!this.enough) {
      try {
        thisLine = lineReaader.readLine();
      } catch (IOException e) {
        e.printStackTrace();
        String message = " => [" + this.target + "] Error occured while relaying program output.";
        this.plugin.getLogger().severe(message);
        return;
      }

      if(thisLine == null) break;

      String outLine = String.format("=>[%s] %s", this.target, thisLine);

      this.plugin.getLogger().log(this.level, outLine);
    }
  }

  void end() {
    this.enough = true;
  }
}
