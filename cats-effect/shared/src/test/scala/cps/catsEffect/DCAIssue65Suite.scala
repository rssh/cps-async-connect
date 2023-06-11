package cps.catsEffect

import scala.concurrent.*
import scala.concurrent.duration.*

import cats._
import cats.effect._
import cps.{async, asyncStream, await}
import cps.stream.AsyncList
import cps.monads.given
import cps.monads.catsEffect.{*,given}


import munit.*


class DCAIssue65Suite extends FunSuite {

  import cats.effect.unsafe.implicits.global

  val N = 100_000

  override val munitTimeout = Duration(300, "s")

  //given cps.macros.flags.PrintCode.type = cps.macros.flags.PrintCode

  def readingByIterator(ec: ExecutionContext, nIterations: Int)(implicit loc: munit.Location):Future[Long] = {
    given ExecutionContext = ec
    val stream: AsyncList[IO, Int] = asyncStream[AsyncList[IO, Int]] { out =>
      out.emit(0)
      for i <- 1 to nIterations do out.emit(i)
    }
    val ite = stream.iterator
    val compute: IO[Long] = async[IO] {
      var n = await(ite.next)
      var res: Long = 0
      while (n.nonEmpty) {
        res += n.get
        n = await(ite.next)
      }
      res
    }
    compute.unsafeToFuture()
  }
  
  test("dotty-cps-async:65:global:reading by iterator with global execution context") {
    val nInteractions = System.getProperty("java.vm.name") match
      case "Scala Native" =>  1000
      case _ => N
    readingByIterator(ExecutionContext.global, nInteractions)
  }

  test("dotty-cps-async:65:global:reading by iterator with parasitic execution context") {
    readingByIterator(ExecutionContext.parasitic, N)
  }


}
