/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package not.here.ykis.eventhook

import org.bukkit.plugin.Plugin
import org.bukkit.command.CommandSender
import java.util.function.BiPredicate

// For MockBukkit

/**
 * The manager of the internal plugin lifecycle
 * and the interface to commands
 */
internal class PluginController(private val plugin: Plugin, private val registry: Registry) {
  private val logger = plugin.logger

  //#region Internal Lifecycle management
  fun announceCommencement(): Boolean? {
    if(this.registry.isLoaded) return false
    val participants = this.registry.processApplications()

    when {
      participants < 0 -> return null
      participants == 0 -> return true // TODO may need to change in future
    }

    this.registry.makeReady()
    this.logger.info("%d handler(s) parsed, registered and readied".format(participants))

    return true
  }

  fun endOfEvent(): Boolean {
    this.registry.dismissParticipants()
    return true
  }
  //#endregion

  fun registerSubcommands(dispatcher: CommandDispatcher) {
    val wrapper: ((CommandSender) -> Boolean).() -> (String) -> BiPredicate<CommandSender, Array<String>>
        = wrapped@ { { BiPredicate{ sender, args -> // Using _ since we don't need to use the label for now
          if (args.isNotEmpty()) {
            sender.sendMessage("Too many arguments")
            return@BiPredicate false
          }

          this@wrapped(sender)
        } } }

    val loadAction: (CommandSender) -> Boolean = {
      val result = this.announceCommencement()

      it.sendMessage(when(result) {
          true -> "Configuration loaded"
          false -> "A loaded configuration already exists"
          null -> "No handlers parsed into memory"
      })
      true
    }

    val unloadAction: (CommandSender) -> Boolean = {
      val result = this.endOfEvent()
      it.sendMessage(
        if(result) "Configuration unloaded"
        else "There is nothing left to unload"
      )

      true
    }

    val reloadAction: (CommandSender) -> Boolean = {
      unloadAction(it)
      loadAction(it)
      true
    }

    dispatcher.register("help", { _: CommandSender -> false }.wrapper())
    dispatcher.register("load", loadAction.wrapper())
    dispatcher.register("unload", unloadAction.wrapper())
    dispatcher.register("reload", reloadAction.wrapper())
  }
}
