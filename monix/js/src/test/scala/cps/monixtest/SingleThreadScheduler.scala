package cps.monixtest

import monix.execution.*
import monix.execution.schedulers.*


object SingleThreadScheduler:

  def apply(): Scheduler = Scheduler.global

