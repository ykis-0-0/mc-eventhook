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
  private ServerMock server;

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
    PluginWrapper plugin = (PluginWrapper) manager.loadPlugin(PluginWrapper.class, new Object[0]);

    @SuppressWarnings("KotlinInternalInJava")
    File suppressor = new File(plugin.getDataFolder(), Constants.NAME_HOLDFILE);
    //noinspection ResultOfMethodCallIgnored
    suppressor.createNewFile();

    manager.enablePlugin(plugin);
  }
}
