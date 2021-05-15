package cps.monixtest

import monix.execution.*
import monix.execution.schedulers.*


object SingleThreadScheduler:

  def apply(): SchedulerService = Scheduler.singleThread(name="monix-toy-logger")

