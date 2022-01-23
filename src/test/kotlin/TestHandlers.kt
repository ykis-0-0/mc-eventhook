package not.here.ykis.eventhook

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

  private val plmgr get() = this.server!!.pluginManager
  private val logger get() = this.plugin!!.logger

  @BeforeEach
  fun setUp() {
    val srv = MockBukkit.mock()
    this.server = srv
    this.plugin = srv.pluginManager.loadPlugin(PluginWrapper::class.java, arrayOf()) as PluginWrapper
  }

  @AfterEach
  fun tearDown() {
    this.plugin = null

    MockBukkit.unmock()
    this.server = null
  }

  private fun getScriptFile(path: String) = Paths.get(
    Objects.requireNonNull(
      this.javaClass.classLoader.getResource(path)
    ).toURI()
  ).toFile()

  private fun getSet(): MutableSet<AthleteSpec<out Event>> = mutableSetOf()


  private companion object {

    @JvmStatic
    private fun assertGoodScript(resultVal: ResultValue?) {
      Assertions.assertNotNull(resultVal)
      Assertions.assertFalse(resultVal is ResultValue.NotEvaluated)
      Assertions.assertFalse(resultVal is ResultValue.Error)
    }
  }

  @Test
  fun testLoggerInternal() {
    val scriptFile = getScriptFile("internal.test.kts")

    val returns = this.getSet()
    val proxy = ScriptingProxy(this.logger, scriptFile, ScriptProxyConfig(host, returns = returns))

    val resultValue = proxy.evalFile()
    TestHandlers.Companion.assertGoodScript(resultValue)

    Assertions.assertNotEquals(0, returns.size)
    for(spec in returns) {
      // Simplified Assumptions
      @Suppress("UNCHECKED_CAST")
      val handler = spec.closure(this.logger) as (DummyEvent) -> Unit
      handler(DummyEvent())
    }
  }

  @Test
  fun testLoggerExternal() {
    val scriptFile = getScriptFile("external.test.kts")

    val returns = this.getSet()
    val proxy = ScriptingProxy(this.logger, scriptFile, ScriptProxyConfig(host, returns = returns))

    val resultValue = proxy.evalFile()
    TestHandlers.Companion.assertGoodScript(resultValue)

    Assertions.assertNotEquals(0, returns.size)
    for(spec in returns) {
      // Simplified Assumptions
      @Suppress("UNCHECKED_CAST")
      val handler = spec.closure(this.logger) as (DummyEvent) -> Unit
      handler(DummyEvent())
    }
  }
}