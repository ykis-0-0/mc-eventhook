import not.here.ykis.eventhook.DummyEvent

handler<DummyEvent>(EventPriority.MONITOR, "external") {
  filter { // [DummyEvent] -> Boolean
    positive == true
  }

  execute {
    executable = "java" // Relative to `workdir`
    args("-version")
  }
}
