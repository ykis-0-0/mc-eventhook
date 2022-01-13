import not.here.ykis.eventhook.HandlerBuilder
import not.here.ykis.eventhook.ScriptDef
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.server.ServerLoadEvent

/*
Usage:
[Event]: Some subclass of org.bukkit.event.Event
[priority]: Some value of org.bukkit.event.EventPriority
[name]: Custom name of handler

handler<[Event]>([priority], [name])
 */
/*
handler("announce_up") {
  listensTo(ServerLoadEvent::class, EventPriority.MONITOR)
  filter { it: ServerLoadEvent -> it.type == ServerLoadEvent.LoadType.STARTUP }
  execute {
    argsProvider {
      listOf("up")
    }
  }
}
*/

inline fun <reified Te: org.bukkit.event.Event> handler(
  priority: EventPriority,
  name: String,
  //@BuilderInference
  noinline definition: HandlerBuilder<Te>.() -> Unit,
) = (object: ScriptDef(mutableSetOf()) {}).handler(priority, name, definition)

handler<PlayerTeleportEvent>(EventPriority.MONITOR, "script_test") {
  filter {
    this.player.displayName == "YouSuck"
  }
  script { logger ->
    player.displayName
    logger.warning("That bitch moves again!")
    true
  }
}

handler<ServerLoadEvent>(EventPriority.MONITOR, "announce_up") {
  filter {
    type == ServerLoadEvent.LoadType.STARTUP
  }

  execute {
    executable = "discord.sh"
    workdir = "/announcer/"

    // For fixed args:
    args("up")
    // Equivalent to:
    // argsProvider {
    //  listOf("up")
    // }
    // fun argsProvider(block: (Event) -> List<String>)
    argsProvider {
      listOf()
    }
  }
}