package cpszio


import zio._
import munit._

import scala.concurrent._
import scala.concurrent.duration._

import cps._
import cps.monads.given
import cps.monads.zio.{given,*}

import scala.concurrent.ExecutionContext.Implicits.global

class FutureInteropSuite extends munit.FunSuite {

  class FutureBasedAPI {

     def getX: Future[Int] = Future successful 3

  }


  class ZIOBasedAPI {

     def getY: Task[Int] = ZIO.succeed(2)

  }

  test("make sure that ZIO async can adopt Future in Task") {
     val futureApi = new FutureBasedAPI()
     val zioApi = new ZIOBasedAPI()
     val run = async[Task] {
       val x = await(futureApi.getX)
       val y = await(zioApi.getY)
       assert(x + y == 5)
     }
     Runtime.default.unsafeRunToFuture(run)
  }

  test("make sure that ZIO async can adopt Future in RIO") {
     class Apis {
       val futureApi = new FutureBasedAPI()
       val zioApi = new ZIOBasedAPI()
     }
     val program = asyncRIO[Apis] {
       val futureApi = await(ZIO.access[Apis](_.futureApi))
       val x = await(futureApi.getX)
       val y = await(ZIO.accessM[Apis](_.zioApi.getY))
       assert(x + y == 5)
     }
     Runtime.default.unsafeRunToFuture(program.provide(new Apis))
  }

  test("make sure that Future async can adopt ZIO with given Runtime") {
     val futureApi = new FutureBasedAPI()
     val zioApi = new ZIOBasedAPI()
     given zio.Runtime[ZEnv] = zio.Runtime.default
     val run = async[Future] {
       val x = await(futureApi.getX)
       val y = await(zioApi.getY)
       assert(x + y == 5)
     }
     run
  }


}

