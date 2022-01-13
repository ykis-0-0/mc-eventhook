package not.here.ykis.eventhook

@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@DslMarker
private annotation class HandlerDsl

private typealias HandlerParsed<T> = (@HandlerDsl T).(java.util.logging.Logger) -> Unit
private typealias EventPredicateParsed<T> = (@HandlerDsl T).() -> Boolean
private typealias ArgvGenParsed<T> = (@HandlerDsl T).() -> List<String>

private typealias Handler<T> = (java.util.logging.Logger) -> (@HandlerDsl T) -> Unit
private typealias EventPredicate<T> = (@HandlerDsl T) -> Boolean
private typealias ArgvGen<T> = (@HandlerDsl T) -> List<String>

data class AthleteSpec<Te: org.bukkit.event.Event>(
  val name: String,
  val eventClass: Class<Te>,
  val priority: org.bukkit.event.EventPriority,
  val predicate: EventPredicate<Te>,
  val closure: Handler<Te>,
)

@HandlerDsl
sealed class BaseRunner<Te : org.bukkit.event.Event>(protected val listenerName: String) {
  abstract val callable: Handler<Te>
}

class ScriptedRunner<Te : org.bukkit.event.Event>(private val block: HandlerParsed<Te>, name: String) : BaseRunner<Te>(name) {
  override val callable: Handler<Te>
    get() = { logger ->
      val nestedLogger = object: java.util.logging.Logger(null, null) {
        private val prefix = "=> [%s]".format(this@ScriptedRunner.listenerName)

        init {
          this.parent = logger
          this.level = java.util.logging.Level.ALL
        }

        override fun log(record: java.util.logging.LogRecord) {
          record.message = prefix + record.message
          super.log(record)
        }
      }

      { this@ScriptedRunner.block.invoke(it, nestedLogger) }
    }
}

class ExternalRunner<Te : org.bukkit.event.Event>(name: String) : BaseRunner<Te>(name) {
  lateinit var executable: String
  lateinit var workdir: String
  private var argvGen: ArgvGen<Te> = { emptyList() }

  override val callable: Handler<Te>
    get() = { logger ->
      val watcherFactory = { level: java.util.logging.Level, stream: java.io.InputStream ->
        LoggingHelper(logger, this@ExternalRunner.listenerName, level, stream)
      }

      // Build callable from exec params
      {
        val builder = ProcessBuilder()

        // Prepare Command Line
        val execArgs = buildList {
          add(this@ExternalRunner.executable)
          addAll(this@ExternalRunner.argvGen.invoke(it))
        }
        builder.command(execArgs)

        this@ExternalRunner.assignWorkDir(builder, logger)

        this@ExternalRunner.announceArgs(logger, execArgs)

        val process = builder.start()

        // Attach the process' stdout and stderr
        setOf(
          java.util.logging.Level.INFO to process.inputStream,
          java.util.logging.Level.SEVERE to process.errorStream,
        ).forEach { (level, stream) ->
          Thread(watcherFactory(level, stream)).start()
        }

        val exitCode = process.waitFor()

        this@ExternalRunner.announceExitCode(logger, it, exitCode)
      }
    }

  /**
   * Use working directory if it's not a file,
   * Leaving it to fail at runtime otherwise
   */
  private fun assignWorkDir(builder: ProcessBuilder, logger: java.util.logging.Logger) {
    if(!this::workdir.isInitialized) return

    val handle = java.io.File(this.workdir)
    if(handle.isFile) {
      logger.warning("[%s] exists but is not a directory".format(this.workdir))
      return
    }

    builder.directory(handle)
  }

  private fun announceArgs(logger: java.util.logging.Logger, args: List<String>) {
    val output = buildString {
      append("Runner %s is starting process %s".format(
        this@ExternalRunner.listenerName,
        this@ExternalRunner.executable
      ))

      if(this@ExternalRunner::workdir.isInitialized)
        append(" in directory %s".format(this@ExternalRunner.workdir))

      append(" with arguments [%s]".format(
        args.joinToString("; ")
      ))
    }
    logger.info(output)
  }

  private fun announceExitCode(logger: java.util.logging.Logger, event: org.bukkit.event.Event, exitCode: Int) {
    logger.info(
      "Runner %s on %s finished %s with exit code of %d (seems %s)".format(
        this@ExternalRunner.listenerName,
        event::class.java.name, this@ExternalRunner.executable,
        exitCode, if(exitCode == 0) "okay" else "failed"
      )
    )
  }

  fun argsProvider(block: ArgvGenParsed<Te>) {
    this.argvGen = block
  }

  fun args(vararg args: String) {
    val argv = listOf(*args)
    this.argvGen = { argv }
  }

}

@HandlerDsl
class HandlerBuilder<Te: org.bukkit.event.Event>(
  private val eventClass: Class<Te>,
  private val priority: org.bukkit.event.EventPriority,
  private val name: String,
) {
  private lateinit var predicate: EventPredicateParsed<Te>
  private lateinit var executor: BaseRunner<Te>

  fun filter(condition: EventPredicateParsed<Te>) {
    this.predicate = condition
  }

  fun script(code: HandlerParsed<Te>) {
    this.executor = ScriptedRunner(code, this.name)
  }

  fun execute(block: ExternalRunner<Te>.() -> Unit) {
    this.executor = ExternalRunner<Te>(name).apply(block)
  }

  val fixture get() = AthleteSpec(this.name, this.eventClass, this.priority, this.predicate, this.executor.callable)
}

