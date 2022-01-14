package not.here.ykis.eventhook;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.MockBukkit;

public class TestMain {
  @SuppressWarnings(value = "unused") // It's not time to be a proper warning
  private ServerMock server;
  @SuppressWarnings(value = "unused") // It's not time to be a proper warning
  private PluginWrapper plugin;

  @BeforeEach
  public void setUp() {
    this.server = MockBukkit.mock();

    this.plugin = MockBukkit.load(PluginWrapper.class);
  }

  @AfterEach
  public void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  public void testTest() {
    // TODO Write proper tests
    return;
  }
}
