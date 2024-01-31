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
      //println(logs)
    }.runToFuture
  }

  test("make sure that FizBuzz run N times in async loop [automatic coloring removed]") {
    import cps.syntax.{*,given}
    val run = async {
       val logger = ToyLogger.make()
       val ctr = Atomic(0)
       while {
          val v = ctr.get()
          // ! cause compiler error
          await(logger.log(v.toString))
          if v % 3 == 0 then 
             ! logger.log("fizz")
          if v % 5 == 0 then 
             ! logger.log("buzz")
          ctr += 1
          v < 10 
       } do ()
       await(logger.all())
    }
    run.map{logs =>
      assert(logs(0)=="0")
      assert(logs(6)=="fizz")
      //println(logs)
    }.runToFuture
  }

}

