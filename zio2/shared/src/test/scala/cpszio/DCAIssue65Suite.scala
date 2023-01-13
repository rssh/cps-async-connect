package cpszio

import scala.concurrent.*

import zio.*
import zio.stream.*
import cps.{async, asyncStream, await}
import cps.stream.AsyncList
import cps.monads.given
import cps.monads.zio.{*,given}
import cps.stream.zio.{*,given}


import munit.*


class DCAIssue65Suite extends FunSuite {

  val N = 10000

  type ZF[X] = ZIO[Any, Throwable, X]

  def readingByIterator(ec: ExecutionContext)(implicit loc: munit.Location):Future[Long] = {
    given ExecutionContext = ExecutionContext.global
    println("start")
    val stream: AsyncList[ZF, Int] = asyncStream[AsyncList[ZF, Int]] { out =>
      println("begin generator")
      out.emit(0)
      println("plop")
      for i <- 1 to 10_000_000 do out.emit(i)
    }
    println("before for loop")
    val ite = stream.iterator
    val compute: ZF[Long] = async[ZF] {
      println("begin sink")
      var n = await(ite.next)
      var res: Long = 0
      println("before while")
      while (n.nonEmpty) {
        res += n.get
        n = await(ite.next)
      }
      res
    }
    println("before unsafe")
    //Unsafe.unsafe { implicit unsafe => zio.Runtime.default.unsafe.run(compute).getOrThrowFiberFailure() }
    Unsafe.unsafe { implicit unsafe => zio.Runtime.default.unsafe.runToFuture(compute) }
  }
  
  test("reading by iterator with global execution context") {
    readingByIterator(ExecutionContext.global)
  }



}
