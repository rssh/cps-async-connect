package cps.monixtest

import scala.language.implicitConversions
import scala.concurrent.duration.*

import monix.execution.atomic.*
import monix.eval.*

import cps.*
import cps.monads.monix.given

import munit.*

class LazyEffectSuite extends FunSuite {

  import monix.execution.Scheduler.Implicits.global

  test("monix: make sure that async expressions are not evaluating early") {
     //implicit val printCode = cps.macroFlags.PrintCode
     var x = 0
     val c = async {
       x = 1
     }
     assert(x == 0)
     c.runToFuture.map{r =>
       assert(x == 1 )
     }
  }


  test("monix: make sure that exception is catched inside async expression ") {
     //implicit val printCode = cps.macroFlags.PrintCode
     val c1 = async {
        throw new RuntimeException("AAA")
     }
     val c2 = async {
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
     c2.runToFuture
  }


}

