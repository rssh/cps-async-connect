package cpszio

import zio._
import munit._

import scala.concurrent._
import scala.concurrent.duration._

import cps._
import cps.monads.given
import cps.monads.zio.{given,*}
import java.util.concurrent.atomic.AtomicInteger


import scala.concurrent.ExecutionContext.Implicits.global

class ConcurrentSuite extends munit.FunSuite {

  test("basic concurrent test") {

     val run = async[Task] {
        val m = summon[CpsConcurrentEffectMonad[Task]]
        val x = new AtomicInteger(0)
        val fiber1 = await(m.spawnEffect{
          ZIO.attempt{ x.incrementAndGet() }
        })
        val fiber2 = await(m.spawnEffect{
          ZIO.attempt{ x.incrementAndGet() }
        })
        val y1 = await(m.join(fiber1))
        val y2 = await(m.join(fiber2))
        assert(x.get() == 2 )
     }

  }


}
