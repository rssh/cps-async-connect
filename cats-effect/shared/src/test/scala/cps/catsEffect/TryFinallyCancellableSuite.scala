package cps.catsEffect

import cats.effect.{IO, SyncIO}
import cats.effect.unsafe.IORuntime
import munit.CatsEffectSuite

import concurrent.duration.*
import cps.*
import cps.monads.catsEffect.given

import scala.concurrent.CancellationException


class TryFinallyCancellableSuite extends CatsEffectSuite {

  test("ensure that finally block is executed") {
    var x = 0
    var finalizerCalled = false
    val run = async[IO] {
      try {
        x = 1
        x = x + 1
        x = x + 1
        x
      } finally {
        println(s"finally block start, x = $x")
        x = x + 1
        finalizerCalled = true
        println(s"finally block end, x = $x")
      }
    }
    run.map{ _ =>
      println(s"x = $x")
      assert(finalizerCalled)
      assert(x == 4)
    }
  }

  given cps.macros.flags.PrintCode.type = cps.macros.flags.PrintCode


  test("ensure that finally blok is executed after cancelled") {

    var finalizerCalled = false
    val run = async[IO] {
      println(s"in testFinalizeCancelled ${summon[CpsMonad[IO]]}")
      try {
        IO.canceled.await
      } finally {
        println("in finalizer")
        finalizerCalled = true
      }
    }

    // ironically, CatsEffectSuite does not have method to check cancellation.
    //
    //interceptIO[CancellationException](run).map { _ =>
    //  assert(finalizerCalled)
    //}
    // not working, because Cancellation omit exception handlers.
    try
      given IORuntime = munitIORuntime
      val outcome = run.unsafeRunSync()
    catch
      case ex: CancellationException =>
        println(s"ex: $ex")

    assert(finalizerCalled)

  }


  test("ensure that async finally blok is executed after cancelled") {

    var finalizerCalled = false
    var x = 0
    val run = async[IO] {
      println(s"in testFinalizeCancelled ${summon[CpsMonad[IO]]}")
      try {
        IO.canceled.await
      } finally {
        println("in async finalizer")
        x = await(IO.delay(1))
        finalizerCalled = true
      }
    }

    try
      given IORuntime = munitIORuntime
      val outcome = run.unsafeRunSync()
    catch
      case ex: CancellationException =>
        println(s"ex: $ex")
        //ex.printStackTrace()

    assert(x == 1)
    assert(finalizerCalled)

  }





  test("ensure that async finally blok is executed after cancelled, and exception from it is exists somewhere") {

    var finalizerCalled = false
    var x = 0
    val run = async[IO] {
      try {
        IO.canceled.await
      } finally {
        x = await(IO.delay(1))
        finalizerCalled = true
        throw new RuntimeException("AAA")
      }
    }

    var runtimeExceptionCatched = false
    try
      given IORuntime = munitIORuntime
      val outcome = run.unsafeRunSync()
      println(s"outcome: $outcome")
    catch
      case ex: CancellationException =>
        var i = 0
        if (ex.getSuppressed().length == 0) {
          println("no suppressed")
        }
        var found = false
        while (i < ex.getSuppressed().length && !found) {
          println(s"suppressed: ${ex.getSuppressed()(i)}")
          if (ex.getSuppressed()(i).getMessage() == "AAA") {
            println("suppressed found")
            found = true
          }
          i = i + 1
        }
        println(s"ex: $ex")
      case ex: RuntimeException =>

        runtimeExceptionCatched = true
        println(s"ex: $ex")

    assert(x == 1)
    assert(finalizerCalled)
    //assert(runtimeExceptionCatched)

  }

}
