import not.here.ykis.eventhook.DummyEvent
import not.here.ykis.eventhook.TestHandlers

println("Went into Scripts")

handler<DummyEvent>(EventPriority.NORMAL, "test_logger") {
  filter { true }

  script { logger ->
    logger.info("Handler triggered!")
  }
}