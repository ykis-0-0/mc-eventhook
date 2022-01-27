package not.here.ykis.eventhook

import java.util.logging.LogRecord
import java.util.logging.Logger

internal class SubordinateLogger(
  // private val master: Plugin,
  attachedTo: Logger,
  listenerName: String
): Logger(PluginWrapper::class.java.canonicalName, null) {
  private val prefix2 = "=> [%s] ".format(listenerName)

  init {
    this.parent = attachedTo// master.logger
    this.level = attachedTo.level// master.logger.level
  }

  override fun log(record: LogRecord) {
    record.message = this.prefix2 + record.message
    record.loggerName = this.parent.name
    this.parent.log(record)
  }
}