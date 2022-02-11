package cpszio

import scala.concurrent.*

import zio.*
import zio.stream.*
import cps.*
import cps.monads.given
import cps.monads.zio.{*,given}
import cps.stream.zio.{*,given}


import munit.*


class BasicGeneratorSuite extends FunSuite {

  val N = 10000

  given ExecutionContext = ExecutionContext.global
  
  test("simple loop in ZStream") {

     val stream = asyncStream[Stream[Throwable,Int]] { out =>
       for(i <- 1 to N) {
         out.emit(i)
       }
     }

     val res = stream.fold(0)(_ + _)

     Runtime.default.unsafeRunToFuture(res).map(x =>
       assert(x == (1 to N).sum)
     )

  }

  test("exception should break loop: ZStream") {
    val stream = asyncStream[Stream[Throwable, Int]] { out =>
      for(i <- 1 to N) {
        if (i == N/2) then
          throw new RuntimeException("bye")
        out.emit(i)
      }
    }

    val res = stream.fold(0)(_ + _)
      
    Runtime.default.unsafeRunToFuture(res).failed.map(ex => assert(ex.getMessage()=="bye"))
    
  }


}