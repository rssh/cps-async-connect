package cps.monixtest

import scala.language.implicitConversions
import scala.concurrent.duration.*

import monix.execution.atomic.*
import monix.eval.*

import cps.*
import cps.monads.monix.given

import munit.*

class StupidFizzBuzzSuite extends FunSuite {

  import monix.execution.Scheduler.Implicits.global

  test("make sure that FizBuzz run N times in async loop") {
    val run = async {
       val logger = ToyLogger.make()
       val ctr = Atomic(0)
       while {
          val v = ctr.get()
          await(logger.log(v.toString))
          if v % 3 == 0 then 
             await(logger.log("fizz"))
          if v % 5 == 0 then 
             await(logger.log("buzz"))
          ctr += 1
          v < 10 
       } do ()
       await(logger.all())
    }
    run.map{logs =>
      assert(logs(0)=="0")
      assert(logs(6)=="fizz")
      //IO.println(logs)
    }.runToFuture
  }

  /*
  test("make sure that FizBuzz run N times in async loop with automatic coloring") {
    import cps.automaticColoring.given
    val run = async {
       val logger = ToyLogger.make()
       val ctr = IO.ref(0)
       while {
          //await(IO.sleep(100.millisecond))
          val v = ctr.get
          logger.log(await(v).toString)
          if v % 3 == 0 then 
             logger.log("fizz")
          if v % 5 == 0 then 
             logger.log("buzz")
          ctr.update(_ + 1)
          v < 10 
       } do ()
       await(logger.all())
    }
    run.flatMap{logs =>
      assert(logs(0)=="0")
      assert(logs(6)=="fizz")
      //IO.println(logs)
      IO.unit
    }
  }
   */

}

