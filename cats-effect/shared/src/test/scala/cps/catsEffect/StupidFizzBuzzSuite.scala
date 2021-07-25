package cps.catsEffect

import scala.language.implicitConversions


import cats.effect.{IO, SyncIO}
import munit.CatsEffectSuite
import concurrent.duration._

import cps._
import cps.monads.catsEffect.given


class StupidFizzBuzzSuite extends CatsEffectSuite {

  test("make sure that FizBuzz run N times") {
    val run = 
      for {
         logger <- ToyLogger.make()
         ctr <- IO.ref(0)

         wait = IO.sleep(100.millisecond)
         poll = wait *> ctr.get

         _ <- (poll.flatMap(x => 
                      for{ _ <- logger.log(x.toString) 
                           _ <- if(x % 3 == 0) { logger.log("fizz") } else IO.unit
                           _ <- if(x % 5 == 0) { logger.log("buzz") } else IO.unit
                      } yield ()
                    ) *> ctr.update(_ + 1) *> ctr.get ).iterateWhile(_ <= 10)
         logs <- logger.all()
      } yield logs
    run.flatMap{ logs => 
      assert(logs(0)=="0")
      assert(logs(1)=="fizz")
      assert(logs(2)=="buzz")
      assert(logs(3)=="1")
      assert(logs(4)=="2")
      assert(logs(5)=="3")
      assert(logs(6)=="fizz")
      assert(logs(7)=="4")
      //IO.println(logs) 
      IO.unit
    }
  }

  test("make sure that FizBuzz run N times in async loop") {
    val run = async {
       val logger = await(ToyLogger.make())
       val ctr = await(IO.ref(0))
       while {
          //await(IO.sleep(100.millisecond))
          val v = await(ctr.get)
          await(logger.log(v.toString))
          if v % 3 == 0 then 
             await(logger.log("fizz"))
          if v % 5 == 0 then 
             await(logger.log("buzz"))
          await(ctr.update(_ + 1))
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

}

