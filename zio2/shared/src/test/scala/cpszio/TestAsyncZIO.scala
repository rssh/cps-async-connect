package cpszio

import cps.*
import cps.monads.zio.{given,*}

import zio._

import munit.*


  
/*
class TestAsyncZIO extends munit.FunSuite {


      test("simple test of asyncZIO-1") {
         import scala.concurrent.ExecutionContext.Implicits.global
         val program = asyncZIO[TLogging with Clock , Throwable] {
             val intRef = await(Ref.make(0))
             await(TLog.logOp("createRef"))
             val date = await(currentDateTime)
             await(TLog.logOp("getDate"))
             await(TLog.lastRecords(10))
         }
         val logService: TLogging.Service = new TLoggingImpl.Service
         val r = program.provideLayer( ZLayer.succeed(logService) ++ Clock.live )
         Runtime.default.unsafeRunToFuture(r)
          .map{ logs =>
            //println(s"logs=$logs") 
            assert(logs(0)==TLogging.OpRecord("createRef"))
            assert(logs(1)==TLogging.OpRecord("getDate"))
         }
      }

}
*/

