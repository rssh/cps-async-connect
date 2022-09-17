package cpszio


import zio.*
import munit.*
import concurrent.duration.*

import cps.*
import cps.monads.zio.{given,*}


class LazyEffectSuite extends FunSuite {

  import concurrent.ExecutionContext.Implicits.global

  test("zio: make sure that evaluation of async expression is delayed") {

     implicit val printCode = cps.macros.flags.PrintCode
     var x = 0
     val c = async[Task] {
       x = 1
     }
     assert(x == 0)
     Unsafe.unsafe(implicit unsafe => Runtime.default.unsafe.runToFuture(c).map{ r =>
       assert(x == 1 )
     })

  }

  test("zio: make sure that exception is catched inside async expression ") {
     //implicit val printCode = cps.macroFlags.PrintCode
     val c1 = async[Task] {
        throw new RuntimeException("AAA")
     }
     val c2 = async[Task] {
       var x = 0
       var y = 0
       try {
         await(c1)
         x = 1
       }catch{
         case ex: RuntimeException =>
            assert(ex.getMessage()=="AAA")
            y = 2
       }
       assert(x == 0)
       assert(y == 2)
     }
     Unsafe.unsafe(implicit unsafe => Runtime.default.unsafe.runToFuture(c2))
  }

}


