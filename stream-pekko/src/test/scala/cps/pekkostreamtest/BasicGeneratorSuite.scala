package cps.pekkostreamtest

import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

import org.apache.pekko.*
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.*
import org.apache.pekko.stream.scaladsl.*

import cps.*
import cps.monads.given
import cps.stream.pekko.given


import munit.*


class BasicGeneratorSuite extends FunSuite {

  val N = 10000

  given system: ActorSystem = ActorSystem("BasicGeneratorSuite")

  test("simple loop in pekko-stream") {

     val source = asyncStream[Source[Int,NotUsed]] { out =>
       for(i <- 1 to N) {
         out.emit(i)
       }
     }
 

     source.runWith(Sink.seq).map{ v =>
      assert(v.length == N)
      assert(v(0)==1)
      assert(v(1)==2)
     }

  }

  test("exception should break loop: pekko-stream") {
    val source = asyncStream[Source[Int,NotUsed]] { out =>
      for(i <- 1 to N) {
        if (i == N/2) then
          throw new RuntimeException("bye")
        out.emit(i)
      }
    }

    val res = source.runWith(Sink.seq)
      
    res.failed.map(ex => assert(ex.getMessage()=="bye"))
    
  }


}