package not.here.ykis.eventhook

import java.io.File
import java.util.logging.Logger
import kotlin.script.experimental.api.ResultValue

import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

import org.junit.jupiter.api.*

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TestScripting {
  private val logger = Logger.getLogger("Test/scripting")
  private val host = BasicJvmScriptingHost()

  @BeforeEach
  fun setUp() {
  }

  @AfterEach
  fun tearDown() {
  }

  private inline fun <reified T: Any> getTestScriptFile(name: String): File {
    val url = T::class.java.classLoader.getResource(name)!!
    return File(url.file)
  }

  @Test
  @Order(0)
  fun testDefault() {
    val defaultScript = getTestScriptFile<PluginWrapper>("config.kts")

    val proxy = ScriptingProxy(logger, defaultScript, ScriptProxyConfig(host = this.host))
    proxy.evalFile()
  }

  @Test
  @Order(1)
  fun testSimpleReturn() {
    val returnScript = getTestScriptFile<TestScripting>("script_load/simple_return.test.kts")

    val proxy = ScriptingProxy(logger, returnScript, ScriptProxyConfig(host = this.host))
    proxy.evalFile()
  }

  @Test
  @Order(2)
  fun testPrintAndReturn() {
    val printScript = getTestScriptFile<TestScripting>("script_load/println.test.kts")

    val proxy = ScriptingProxy(logger, printScript, ScriptProxyConfig(host = this.host))
    proxy.evalFile()
  }

  @Test
  @Order(3)
  fun testInternal() {
    val internalScript = getTestScriptFile<TestScripting>("script_load/internal.test.kts")

    val proxy = ScriptingProxy(logger, internalScript, ScriptProxyConfig(host = this.host))
    val result = proxy.evalFile()
    Assertions.assertFalse(result is ResultValue.Error)
  }

  @Test
  @Order(4)
  fun testExternal() {
    val externalScript = getTestScriptFile<TestScripting>("script_load/external.test.kts")

    val proxy = ScriptingProxy(logger, externalScript, ScriptProxyConfig(host = this.host))
    val result = proxy.evalFile()
    Assertions.assertFalse(result is ResultValue.Error)
  }
}
