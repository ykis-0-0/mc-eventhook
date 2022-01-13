/*
Usage:
[Event]: Some subclass of org.bukkit.event.Event
[priority]: Some value of org.bukkit.event.EventPriority
[name]: Custom name of handler

handler<[Event]>([priority], [name]) {
  [body]
}

Example 1:
import org.bukkit.event.player.PlayerTeleportEvent

handler<PlayerTeleportEvent>(EventPriority.MONITOR, "scripted") {
  filter { // [ServerLoadEvent] -> Boolean
    player.displayName == "Steve"
  }

  script { logger -> // java.util.logging.Logger -> [ServerLoadEvent] -> Unit
    logger.warning("Steve has teleported!")
  }
}

Example 2:
import org.bukkit.event.server.ServerLoadEvent

handler<ServerLoadEvent>(EventPriority.MONITOR, "external") {
  filter { // [ServerLoadEvent] -> Boolean
    type == ServerLoadEvent.LoadType.STARTUP}
  }

  execute {
    executable = "program.sh" // Relative to `workdir`
    workdir = "/path/to/workdir" // Server pwd is unspecified

    argsProvider { // [ServerLoadEvent] -> List<String>
      listOf("up")
    }
    // Can use args("arg1", "arg2", ...) if don't need to generate dynamically
  }
}
 */
