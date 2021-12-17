package xxx.ykis.eventhook;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.MockBukkit;

public class TestMain {
  private ServerMock server;
  private PluginMain plugin;

  @BeforeEach
  public void setUp() {
    this.server = MockBukkit.mock();

    this.plugin = MockBukkit.load(PluginMain.class);
  }

  @AfterEach
  public void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  public void testTest() {
    return;
  }
}
