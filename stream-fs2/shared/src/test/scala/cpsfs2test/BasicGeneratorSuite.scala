package cpsfs2test

import scala.concurrent.*

import cps.*
import cps.monads.catsEffect.{*,given}
import cps.stream.*
import cps.stream.fs2stream.{*,given}

import munit.*
import munit.CatsEffectSuite

import cats.effect.*



class BasicGeneratorSuite extends CatsEffectSuite {

  val N = 10000

  test("simple loop in fs2") {
      given ExecutionContext = ExecutionContext.global
      val stream = asyncStream[fs2.Stream[IO,Int]] { out =>
        for(i <- 1 to N) {
          out.emit(i)
        }
      }

      val fVector = stream.compile.toVector

      fVector.map{ v =>
        assert(v.length == N)
        assert(v(0)==1)
        assert(v(1)==2)
      }
  }

  test("exception should break loop: fs2") {
    given ExecutionContext = ExecutionContext.global
    val stream = asyncStream[fs2.Stream[IO,Int]] { out =>
      for(i <- 1 to N) {
        if (i == N/2) then
          throw new RuntimeException("bye")
        out.emit(i)
      }
    }

    // shouls throw bye
    val ioVector = stream.compile.toVector

    ioVector.map(_ => "normal").handleError{ v =>
        v.getMessage()
    }.map{ s =>
      assert( s == "bye" )
    }
  }

}
