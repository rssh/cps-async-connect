package cps.monixtest

import cps.*
import cps.monads.monix.{given,*}
import monix.eval.*
import java.util.concurrent.atomic.AtomicInteger

import munit.*

class ConcurrentSuite extends FunSuite {

  test("basic concurrent test") {

     val run = async[Task] {
        val m = summon[CpsConcurrentEffectMonad[Task]]
        val x = new AtomicInteger(0)
        val fiber1 = await(m.spawnEffect{
           Task.now{ x.incrementAndGet() }
        })
        val fiber2 = await(m.spawnEffect{
          Task.now{ x.incrementAndGet() }
        })
        val y1 = await(m.join(fiber1))
        val y2 = await(m.join(fiber2))
        assert(x.get() == 2 )
     }

  }


}
