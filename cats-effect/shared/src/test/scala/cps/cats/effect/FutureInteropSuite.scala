package cps.cats.effect


import cats.effect.{IO, SyncIO}
import cats.effect.kernel.Async
import munit.CatsEffectSuite

import scala.concurrent._
import scala.concurrent.duration._

import cps._
import cps.monads.given
import cps.monads.cats.given

import scala.concurrent.ExecutionContext.Implicits.global

class FutureInteropSuite extends CatsEffectSuite {

  class FutureBasedAPI {

     def getX: Future[Int] = Future successful 3

  }

  class IOBasedAPI {

     def getY: IO[Int] = IO.delay(2)

  }

  class TaglessFinalAPI[F[_]:Async] {

      def getZ: F[Int] = summon[Async[F]].pure(1)

  }

  test("make shure that IO async can adopt Future") {
     val futureApi = new FutureBasedAPI()
     val ioApi = new IOBasedAPI()
     val run = async[IO] {
       val x = await(futureApi.getX)
       val y = await(ioApi.getY)
       assert(x + y == 5)
     }
     run
  }

  test("make shure that Future async can adopt IO") {
     val futureApi = new FutureBasedAPI()
     val ioApi = new IOBasedAPI()
     val run = async[Future] {
       val x = await(futureApi.getX)
       val y = await(ioApi.getY)
       assert(x + y == 5)
     }
     IO.fromFuture(IO.pure(run))
  }

  test("make shure that F:Async async can adopt Future") {
    val futureApi = new FutureBasedAPI()
    def genericFun[F[_]:Async]():F[Int] = async[F] {
       val taglessApi = new TaglessFinalAPI[F]()
       val x = await(futureApi.getX)
       val z = await(taglessApi.getZ)
       val r = x + z
       assert(r == 4)
       r
    }

    val run = genericFun[IO]()
    run
  }

  test("make shure that F:Async is callable from IO") {
    val futureApi = new FutureBasedAPI()
    val ioApi = new IOBasedAPI()
    def genericFun[F[_]:Async]():F[Int] = async[F] {
       val taglessApi = new TaglessFinalAPI[F]()
       val x = await(futureApi.getX)
       val z = await(taglessApi.getZ)
       val r = x + z
       assert(r == 4)
       r
    }
    val run = async[IO] {
       val q = await(genericFun[IO]())
       val y = await(ioApi.getY)
       assert(q == 4)
       assert(q + y == 6)
    }
    run 
  }

  /*
  test("make shure that F:Async is callable from Future") {
  }
  */

  /*
  test("make shure that F:Async async can run in non-io monad") {
    import _root_.cats.implicits._
    val futureApi = new FutureBasedAPI()
    def genericFun[F[_]:Async]():F[Int] = async[F] {
       val taglessApi = new TaglessFinalAPI[F]()
       val x = await(futureApi.getX)
       val z = await(taglessApi.getZ)
       val r = x + z
       assert(r == 4)
       r
    }
    val run = genericFun[]()
    IO.fromFuture(IO.pure(run))
  }
  */

}

