package not.here.ykis.eventhook

import java.util.logging.Logger

import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.host.with
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

import org.bukkit.event.Event
import org.bukkit.event.EventPriority

data class ScriptProxyConfig(
  val host: BasicJvmScriptingHost = BasicJvmScriptingHost(),
  val returns: MutableSet<AthleteSpec<out Event>> = mutableSetOf(),
  val compile: ScriptCompilationConfiguration = createJvmCompilationConfigurationFromTemplate<ScriptStub> {
    defaultImports(EventPriority::class)//, ScriptClosure::class)
    jvm {
      // compilerOptions("-jvm-target", "16")
      dependenciesFromCurrentContext(wholeClasspath = true)
    }
    implicitReceivers(ScriptClosure::class)
  },
  val eval: ScriptEvaluationConfiguration = createJvmEvaluationConfigurationFromTemplate<ScriptStub> {
    hostConfiguration(defaultJvmScriptingHostConfiguration.with {
      jvm {
        baseClassLoader(ScriptProxyConfig::class.java.classLoader)
      }
    })
    implicitReceivers(ScriptClosure(returns))
  },
)

class ScriptingProxy(
  private val logger: Logger,
  private val scriptSource: SourceCode,
  config: ScriptProxyConfig = ScriptProxyConfig()
) {
  constructor(logger: Logger, sourcePath: java.io.File, config: ScriptProxyConfig = ScriptProxyConfig()): this(logger, sourcePath.toScriptSource(), config)

  private val executor = ScriptExecutor(this.scriptSource, config.compile, config.eval, config.host)

  fun evalFile(): ResultValue? {
    val compileResults = this.executor.doCompile()

    val notableMentions = compileResults.reports.filter { it.severity > ScriptDiagnostic.Severity.DEBUG }.size
    this.logger.info(when(notableMentions) {
      0 -> "Compilation complete for script %s."
      else -> "Script %s compiled with following diagnostics:"
    }.format(this.scriptSource.name))
    this.executor.renderDiagnostics(compileResults.reports).forEach { (level, msg) ->
      this.logger.log(level, msg)
    }

    if(compileResults is ResultWithDiagnostics.Failure) return null

    val evalResults = this.executor.doEvaluate(compileResults.valueOrThrow())

    @Suppress("SpellCheckingInspection")
    val logMsgs = this@ScriptingProxy.executor.renderDiagnostics(evalResults.reports)
    logMsgs.forEach { (level, msg) ->
      this.logger.log(level, msg)
    }

    if(evalResults is ResultWithDiagnostics.Failure) {
      this.logger.warning("Script failed to evalutate")
      return null
    }

    return evalResults.valueOrThrow().returnValue
  }
}
