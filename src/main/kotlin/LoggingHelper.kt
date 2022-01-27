package not.here.ykis.eventhook

import java.io.InputStream
import java.util.logging.Level
import java.util.logging.Logger

/** A helper class for outsourcing the relaying of logs from the [Athlete]s  */
internal class LoggingHelper internal constructor(
    private val logger: Logger,
    private val level: Level,
    stream: InputStream
) : Runnable {

    private val lineReader: java.io.BufferedReader = stream.bufferedReader()
    private var shouldStop: Boolean = false

    override fun run() {
        while(true) {
            val thisLine: String? = try {
                this.lineReader.readLine()
            } catch(e: java.io.IOException) {
                e.printStackTrace()
                this.logger.severe("Error occurred while relaying program output.")
                return
            }
            if(thisLine == null || this.shouldStop) break
            this.logger.log(this.level, thisLine)
        }
    }

    fun end() {
        this.shouldStop = true
    }
}
