package cps.monixtest

import scala.concurrent.*

import cps.*
import monix.*
import monix.reactive.*
import cps.monads.monix.given
import cps.stream.monix.given


import munit.*

import monix.execution.Scheduler.Implicits.global

class BasicGeneratorSuite extends FunSuite {

  val N = 10000

  test("simple loop in monix") {

     val stream = asyncStream[Observable[Int]] { out =>
       for(i <- 1 to N) {
         out.emit(i)
       }
     }

     stream.consumeWith(Consumer.toList).runToFuture.map{ v =>
      assert(v.length == N)
      assert(v(0)==1)
      assert(v(1)==2)
     }

  }

  test("exception should break loop: monix") {
    val stream = asyncStream[Observable[Int]] { out =>
      for(i <- 1 to N) {
        if (i == N/2) then
          throw new RuntimeException("bye")
        out.emit(i)
      }
    }

    val res = stream.consumeWith(Consumer.toList).runToFuture
      
    res.failed.map(ex => assert(ex.getMessage()=="bye"))
    
  }


}