package cps.monixtest

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


import monix.eval.*

import cps._
import cps.monads.given
import cps.monads.monix.given
import munit._




class FutureInteropSuite extends FunSuite {

  class FutureBasedAPI {

     def getX: Future[Int] = Future successful 3

  }

  class TaskBasedAPI {

     def getY: Task[Int] = Task.pure(2)

  }


  test("make sure that Task async can adopt Future") {
     val futureApi = new FutureBasedAPI()
     val taskApi = new TaskBasedAPI()
     val run = async[Task] {
       val x = await(futureApi.getX)
       val y = await(taskApi.getY)
       assert(x + y == 5)
     }
     import monix.execution.Scheduler.Implicits.global
     run.runToFuture
  }

  test("make sure that Future async can adopt Task") {
     import monix.execution.Scheduler.Implicits.global

     val futureApi = new FutureBasedAPI()
     val taskApi = new TaskBasedAPI()
     val run = async[Future] {
       val x = await(futureApi.getX)
       val y = await(taskApi.getY)
       assert(x + y == 5)
     }
     run
  }


}

