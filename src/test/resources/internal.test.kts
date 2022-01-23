import not.here.ykis.eventhook.DummyEvent

handler<DummyEvent>(EventPriority.MONITOR, "internal") {
  filter { // [DummyEvent] -> Boolean
    positive == true
  }

  script { logger ->
    logger.info("Hello from handler")
  }
}
