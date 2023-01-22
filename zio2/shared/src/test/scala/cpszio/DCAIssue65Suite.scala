package cpszio

import scala.concurrent.*
import scala.concurrent.duration.*

import zio.{Duration=>_,*}
import zio.stream.*
import cps.{async, asyncStream, await}
import cps.stream.AsyncList
import cps.monads.given
import cps.monads.zio.{*,given}
import cps.stream.zio.{*,given}


import munit.*


class DCAIssue65Suite extends FunSuite {

  val N = 100_000

  type ZF[X] = ZIO[Any, Throwable, X]

  override val munitTimeout = Duration(300, "s")

  given cps.macros.flags.PrintCode.type = cps.macros.flags.PrintCode

  def readingByIterator(ec: ExecutionContext)(implicit loc: munit.Location):Future[Long] = {
    given ExecutionContext = ec
    val stream: AsyncList[ZF, Int] = asyncStream[AsyncList[ZF, Int]] { out =>
      out.emit(0)
      for i <- 1 to N do out.emit(i)
    }
    val ite = stream.iterator
    val compute: ZF[Long] = async[ZF] {
      var n = await(ite.next)
      var res: Long = 0
      while (n.nonEmpty) {
        res += n.get
        n = await(ite.next)
      }
      res
    }
    Unsafe.unsafe { implicit unsafe => zio.Runtime.default.unsafe.runToFuture(compute) }
  }
  
  test("dotty-cps-async:65:global:reading by iterator with global execution context") {
    readingByIterator(ExecutionContext.global)
  }

  test("dotty-cps-async:65:global:reading by iterator with parasitic execution context") {
    readingByIterator(ExecutionContext.parasitic)
  }



}
