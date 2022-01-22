import org.bukkit.event.server.ServerLoadEvent

handler<ServerLoadEvent>(EventPriority.MONITOR, "external") {
  filter { // [ServerLoadEvent] -> Boolean
    type == ServerLoadEvent.LoadType.STARTUP
  }

  execute {
    executable = "java" // Relative to `workdir`
    args("-version")
  }
}
