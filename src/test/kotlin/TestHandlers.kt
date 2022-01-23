package not.here.ykis.eventhook

import java.io.File
import java.nio.file.Paths
import java.util.*
import kotlin.script.experimental.api.ResultValue

import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import org.bukkit.event.Event

class TestHandlers {
  private var server: ServerMock? = null
  private var plugin: PluginWrapper? = null
  private val host = BasicJvmScriptingHost()
  @BeforeEach
  fun setUp() {
    server = MockBukkit.mock()
  }

  @AfterEach
  fun tearDown() {
    plugin = null
    MockBukkit.unmock()
    server = null
  }

  @Test
  fun testLogger() {
    this.plugin = this.server!!.pluginManager.loadPlugin(PluginWrapper::class.java, arrayOf()) as PluginWrapper

    val scriptFile = Paths.get(
      Objects.requireNonNull(
        this.javaClass.classLoader.getResource("script_handlers/logger.test.kts")
      ).toURI()
    ).toFile()

    val returns = HashSet<AthleteSpec<out Event>>()
    val config = ScriptProxyConfig(
      this.host,
      returns = returns
    )
    val proxy = ScriptingProxy(plugin!!.logger, scriptFile, config)

    val resultValue = proxy.evalFile()
    Assertions.assertFalse(resultValue is ResultValue.NotEvaluated)
    Assertions.assertFalse(resultValue is ResultValue.Error)

    Assertions.assertNotEquals(0, returns.size)
    for((_, _, _, _, closure) in returns) {
      // Simplified Assumptions
      val aa = closure(plugin!!.logger)
      @Suppress("UNCHECKED_CAST")
      (aa as (DummyEvent) -> Unit)(DummyEvent())
    }
  }
}