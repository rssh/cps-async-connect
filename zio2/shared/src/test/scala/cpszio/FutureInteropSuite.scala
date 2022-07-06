package cpszio


import zio._
import munit._

import scala.concurrent._
import scala.concurrent.duration._

import cps.*
import cps.monads.{given,*}
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
     Unsafe.unsafe(Runtime.default.unsafe.runToFuture(run))
  }

  test("make sure that ZIO async can adopt Future in RIO") {
     class Apis {
       val futureApi = new FutureBasedAPI()
       val zioApi = new ZIOBasedAPI()
     }
     val program = asyncRIO[Apis] {
       val apis = await(ZIO.environment[Apis]).get
       val futureApi = apis.futureApi
       val x = await(futureApi.getX)
       val y = await(ZIO.environmentWithZIO[Apis](_.get.zioApi.getY))
       assert(x + y == 5)
     }
     val runtime = Runtime.default.mapEnvironment(_ => ZEnvironment[Apis](new Apis))
     Unsafe.unsafe(runtime.unsafe.runToFuture(program))
  }

  test("make sure that Future async can adopt ZIO with given Runtime") {
     val futureApi = new FutureBasedAPI()
     val zioApi = new ZIOBasedAPI()
     implicit val runtime = Runtime.default
     val futurePrg = async[Future] {
       val x = await(futureApi.getX)
       val y = await(zioApi.getY)
       assert(x + y == 5)
     }
     futurePrg
  }


}

