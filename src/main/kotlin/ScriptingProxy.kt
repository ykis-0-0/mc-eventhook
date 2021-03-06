package not.here.ykis.eventhook

import java.util.logging.Logger

import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.createCompilationConfigurationFromTemplate
import kotlin.script.experimental.host.createEvaluationConfigurationFromTemplate
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

import org.bukkit.event.Event
import org.bukkit.event.EventPriority

//#region Overrides

private inline fun <reified T : Any> createJvmCompilationConfigurationFromTemplate(
  noinline body: ScriptCompilationConfiguration.Builder.() -> Unit = {}
): ScriptCompilationConfiguration = createCompilationConfigurationFromTemplate(
  KotlinType(T::class),
  defaultJvmScriptingHostConfiguration,
  T::class,
  body
)

private inline fun <reified T : Any> createJvmEvaluationConfigurationFromTemplate(
  noinline body: ScriptEvaluationConfiguration.Builder.() -> Unit = {}
): ScriptEvaluationConfiguration = createEvaluationConfigurationFromTemplate(
  KotlinType(T::class),
  defaultJvmScriptingHostConfiguration,
  T::class,
  body
)

//#endregion

internal data class ScriptProxyConfig(
  val host: BasicJvmScriptingHost = BasicJvmScriptingHost(),
  val returns: MutableSet<AthleteSpec<out Event>> = mutableSetOf(),
  val compile: ScriptCompilationConfiguration = createJvmCompilationConfigurationFromTemplate<ScriptClosure> {
    defaultImports(EventPriority::class)
    jvm {
      // compilerOptions("-jvm-target", "16")
      dependenciesFromClassloader(classLoader = PluginWrapper::class.java.classLoader, wholeClasspath = true)
    }
  },
  val eval: ScriptEvaluationConfiguration = createJvmEvaluationConfigurationFromTemplate<ScriptClosure> {
    jvm {
      baseClassLoader(ScriptProxyConfig::class.java.classLoader)
    }
    constructorArgs(returns)
  },
)

internal class ScriptingProxy(
  private val logger: Logger,
  private val scriptSource: SourceCode,
  config: ScriptProxyConfig
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
