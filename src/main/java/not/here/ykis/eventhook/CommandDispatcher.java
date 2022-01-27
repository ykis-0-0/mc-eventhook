package not.here.ykis.eventhook;

import java.util.Map;
import java.util.HashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.plugin.java.annotation.command.*;
import org.jetbrains.annotations.NotNull;

@Commands(
  @Command(
    name = Constants.COMMAND_NAME,
    desc = "Control the activation state of hooked scripts",
    usage = "Usage: /<command> <load | unload | reload | help>"
  )
)
class CommandDispatcher implements CommandExecutor {
  private final Map<String, BiPredicate<CommandSender, String[]>> subCommands = new HashMap<>();

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
    if(sender instanceof Player) {
      sender.sendMessage("This command is intended for use in server console only");
      return false;
    }

    if(args.length < 1) {
      sender.sendMessage("Too few arguments");
      return false;
    }

    String maybeSubCommand = args[0];
    String[] remainArgs = Stream.of(args).skip(1).toArray(String[]::new);

    final BiPredicate<CommandSender, String[]> handler = this.subCommands.getOrDefault(maybeSubCommand, (entity, remArgs) -> {
      // Default handler, only to complain
      entity.sendMessage("Invalid Subcommand");
      return false;
    });

    return handler.test(sender, remainArgs);
  }

  public boolean register(String name, Function<String, BiPredicate<CommandSender, String[]>> unboundHandler) {
    if(this.subCommands.containsKey(name)) return false;

    this.subCommands.put(name, unboundHandler.apply(name));
    return true;
  }
}
