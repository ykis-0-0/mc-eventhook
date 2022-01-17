package not.here.ykis.eventhook;

import be.seeseemelk.mockbukkit.plugin.PluginManagerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.MockBukkit;

import java.io.File;
import java.io.IOException;

public class TestMain {
  @SuppressWarnings(value = "unused") // It's not time to be a proper warning
  private ServerMock server;
  @SuppressWarnings(value = "unused") // It's not time to be a proper warning
  private PluginWrapper plugin;

  @BeforeEach
  public void setUp() {
    this.server = MockBukkit.mock();
  }

  @AfterEach
  public void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  public void testNoScript() throws IOException {
    PluginManagerMock manager = this.server.getPluginManager();
    this.plugin = (PluginWrapper) manager.loadPlugin(PluginWrapper.class, new Object[0]);

    @SuppressWarnings("KotlinInternalInJava")
    File suppressor = new File(this.plugin.getDataFolder(), Constants.NAME_HOLDFILE);
    //noinspection ResultOfMethodCallIgnored
    suppressor.createNewFile();

    manager.enablePlugin(this.plugin);
  }
}
