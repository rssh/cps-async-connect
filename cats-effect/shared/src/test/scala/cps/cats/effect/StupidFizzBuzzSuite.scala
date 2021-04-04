package cps.cats.effect


import cats.effect.{IO, SyncIO}
import munit.CatsEffectSuite
import concurrent.duration._

import cps._
import cps.monads.cats.given

class StupidFizzBuzzSuite extends CatsEffectSuite {

  test("make shure that FizBuzz run N times") {
    val run = 
      for {
         ctr <- IO.ref(0)

         wait = IO.sleep(100.millisecond)
         poll = wait *> ctr.get

         _ <- (poll.flatMap(x => 
                      for{ _ <- IO.println(x) 
                           _ <- if(x % 3 == 0) { IO.println("fizz") } else IO.unit
                           _ <- if(x % 5 == 0) { IO.println("buzz") } else IO.unit
                      } yield ()
                    ) *> ctr.update(_ + 1) *> ctr.get ).iterateWhile(_ <= 10)
      } yield ()
    run
  }

  test("make shure that FizBuzz run N times in async loop") {
    val run = async {
       val ctr = await(IO.ref(0))
       while {
          await(IO.sleep(100.millisecond))
          val v = await(ctr.get)
          await(IO.println(v))
          if v % 3 == 0 then 
             await(IO.println("fizz"))
          if v % 5 == 0 then 
             await(IO.println("buzz"))
          await(ctr.update(_ + 1))
          v < 10 
       } do ()
    }
    run
  }


}

