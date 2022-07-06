package cpszio

import cps.*
import cps.monads.zio.{given,*}

import zio._

import munit.*


  

class TestAsyncZIO extends munit.FunSuite {


      test("simple test of asyncZIO-1") {
         import scala.concurrent.ExecutionContext.Implicits.global
         val program = asyncZIO[TLogging.Service, Throwable] {
             val intRef = await(Ref.make(0))
             await(TLog.logOp("createRef"))
             val date = await(Clock.currentDateTime)
             await(TLog.logOp("getDate"))
             await(TLog.lastRecords(10))
         }
         val logService: TLogging.Service = new TLoggingImpl.Service
         val r = program.provideLayer(ZLayer.succeed(logService))
         Unsafe.unsafe(Runtime.default.unsafe.runToFuture(r)
          .map{ logs =>
            //println(s"logs=$logs") 
            assert(logs(0)==TLogging.OpRecord("createRef"))
            assert(logs(1)==TLogging.OpRecord("getDate"))
         })
      }

}


