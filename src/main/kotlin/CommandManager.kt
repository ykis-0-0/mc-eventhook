package not.here.ykis.eventhook

import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

import org.bukkit.plugin.java.annotation.command.*

@Commands(
  Command(
    name = Constants.COMMAND_NAME,
    desc = "Control the activation state of hooked scripts",
    usage = "Usage: /<command> <load | unload | reload | help>"
  )
)
internal class CommandManager : CommandExecutor {
  private val subCommands: MutableMap<String, (CommandSender, Array<String>) -> Boolean> = HashMap()

  override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, label: String, args: Array<String>): Boolean {
    if(sender is Player) {
      sender.sendMessage("This command is intended for use in server console only")
      return false
    }

    @Suppress("ReplaceSizeZeroCheckWithIsEmpty")
    if(args.size < 1) {
      sender.sendMessage("Too few arguments")
      return false
    }

    val maybeSubCommand = args[0]
    val remainArgs = args.drop(1)

    val handler = subCommands[maybeSubCommand] ?: { _, _ ->
      // Default handler, only to complain
      sender.sendMessage("Invalid Subcommand")
      false
    }

    return handler(sender, remainArgs.toTypedArray())
  }

  fun register(name: String, unboundHandler: (String) -> (CommandSender, Array<String>) -> Boolean): Boolean {
    if(name in subCommands) return false

    subCommands[name] = unboundHandler(name)
    return true
  }
}