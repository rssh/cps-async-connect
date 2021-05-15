package cps.monixtest

import monix.execution.*
import monix.eval.*


class ToyLogger:


  var lines = Vector.empty[String]
  lazy val localScheduler = Scheduler.singleThread(name="monix-toy-logger")


  def log(message: String):Task[Unit] =
    Task{lines = lines :+ message}.executeOn(localScheduler, forceAsync=true)    
 
  def all(): Task[Vector[String]] =
    Task(lines).executeOn(localScheduler, forceAsync = true)
  

object ToyLogger:

  def make(): ToyLogger =
    new ToyLogger()

