package not.here.ykis.eventhook

import org.bukkit.plugin.Plugin

import java.io.InputStream
import java.util.logging.Level
import java.util.logging.Logger

/** A helper class for outsourcing the relaying of logs from the [Athlete]s  */
internal class LoggingHelper internal constructor(plugin: Plugin, private val target: String, private val level: Level, stream: InputStream) : Runnable {

    private val logger: Logger = plugin.logger
    private val lineReader: java.io.BufferedReader = stream.bufferedReader()
    private var shouldStop: Boolean = false

    override fun run() {
        while(true) {
            val thisLine: String? = try {
                this.lineReader.readLine()
            } catch(e: java.io.IOException) {
                e.printStackTrace()
                this.logger.severe("=>[%s] Error occurred while relaying program output.".format(this.target))
                return
            }
            if(thisLine == null || this.shouldStop) break
            this.logger.log(this.level, "=>[%s] %s".format(this.target, thisLine))
        }
    }

    fun end() {
        this.shouldStop = true
    }
}
