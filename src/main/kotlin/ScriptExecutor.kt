package not.here.ykis.eventhook

import java.util.logging.Level

import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

internal class ScriptExecutor(
  private val scriptSource: SourceCode,
  private val compilationConfiguration: ScriptCompilationConfiguration,
  private val evaluationConfiguration: ScriptEvaluationConfiguration,
  private val host: BasicJvmScriptingHost = BasicJvmScriptingHost()
) {

  fun doCompile(): ResultWithDiagnostics<CompiledScript> {
    return with(this.host) {
      runInCoroutineContext {
        this@with.compiler(this@ScriptExecutor.scriptSource, this@ScriptExecutor.compilationConfiguration)
      }
    }
  }

  fun doEvaluate(clazz: CompiledScript): ResultWithDiagnostics<EvaluationResult> {
    return with(this.host) {
      runInCoroutineContext {
        this@with.evaluator(clazz, this@ScriptExecutor.evaluationConfiguration)
      }
    }
  }

  // Helper function
  private inline fun <reified L, reified R> MutableList<Pair<L, R>>.addP(left: L, right: R) = this.add(Pair(left, right))

  fun renderDiagnostics(reports: List<ScriptDiagnostic>): List<Pair<Level, String>> = buildList {
    reports.forEach {
      val level = when(it.severity) {
        ScriptDiagnostic.Severity.DEBUG -> Level.FINE
        ScriptDiagnostic.Severity.INFO -> Level.INFO
        ScriptDiagnostic.Severity.WARNING -> Level.WARNING
        ScriptDiagnostic.Severity.ERROR -> Level.SEVERE
        ScriptDiagnostic.Severity.FATAL -> Level.SEVERE
      }

      val locationFragment = it.location?.start?.run { "[%3d:%2d]".format(this.line, this.col) } ?: "[]"
      val scriptBasename = it.sourcePath?.substringAfterLast(java.io.File.separatorChar) ?: "<no file>"
      addP(level, "[%s](%s): %s: %s@%s".format(it.severity.name, it.code, it.message, locationFragment, scriptBasename))

      when(val ex = it.exception) {
        null -> Unit
        else -> addP(level, ex.toString() + '\n' + ex.stackTraceToString())
      }
    }
  }
}
