package cpszio

import cps.*
import cps.monads.zio.{given,*}

import zio._
import zio.clock._

import munit.*


case class RichError(ex:Throwable, lastOp: Option[String], supressed:List[Throwable]=List())

given richErrorAdapter[R <: TLogging] : ThrowableAdapter[ R, RichError] with

   def toThrowable(e:RichError): Throwable =
      e.ex

   def fromThrowable[A](e:Throwable): ZIO[ R, RichError, A] =
        for{
           op <- TLog.lastOp().mapError(exLastOp => RichError(e,None,List(exLastOp)))
           r <-  ZIO.fail(RichError(e,op))
        } yield r
     

  

class TestAsyncZIO extends munit.FunSuite {


      test("simple test of asyncZIO-1") {
         import scala.concurrent.ExecutionContext.Implicits.global
         val program = asyncZIO[TLogging with Clock , RichError] {
             val intRef = await(Ref.make(0))
             await(TLog.logOp("createRef"))
             val date = await(currentDateTime)
             await(TLog.logOp("getDate"))
             await(TLog.lastRecords(10))
         }
         val logService: TLogging.Service = new TLoggingImpl.Service
         val r = program.provideLayer( ZLayer.succeed(logService) ++ Clock.live )
         Runtime.default.unsafeRunToFuture(
          r.mapError{e => 
             println("last operation was ${e.lastOp}") 
             e.ex
         }).map{ logs =>
            //println(s"logs=$logs") 
            assert(logs(0)==TLogging.OpRecord("createRef"))
            assert(logs(1)==TLogging.OpRecord("getDate"))
         }
      }

}

