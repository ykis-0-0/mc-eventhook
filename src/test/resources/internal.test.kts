import org.bukkit.event.server.ServerLoadEvent

handler<ServerLoadEvent>(EventPriority.MONITOR, "internal") {
  filter { // [ServerLoadEvent] -> Boolean
    type == ServerLoadEvent.LoadType.STARTUP
  }

  script { logger ->
    logger.info("Hello from handler")
  }
}
