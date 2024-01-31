package cpszio


import zio.*
import munit.*
import concurrent.duration.*

import cps.*
import cps.monads.zio.{given,*}


class StupidFizzBuzzSuite extends FunSuite {

  import concurrent.ExecutionContext.Implicits.global

  test("make sure that FizBuzz run N times in async loop") {
 
   val program = asyncRIO[TLogging] {
       val ctr = await(Ref.make(0))
       while {
          val v = await(ctr.get)
          await(TLog.logMsg(v.toString))
          if v % 3 == 0 then 
             await(TLog.logMsg("fizz"))
          if v % 5 == 0 then 
             await(TLog.logMsg("buzz"))
          await(ctr.update(_ + 1))
          v < 10 
       } do ()
       await(TLog.lastRecords(20))
    }
  
    val logService: TLogging.Service = new TLoggingImpl.Service
    val r = program.provideLayer( ZLayer.succeed(logService) )
    Runtime.default.unsafeRunToFuture(r).map{ logs =>
            //println(s"logs=$logs")
            assert(logs(0)==TLogging.MsgRecord("0"))
            assert(logs(1)==TLogging.MsgRecord("fizz"))
            assert(logs(2)==TLogging.MsgRecord("buzz"))
            assert(logs(3)==TLogging.MsgRecord("1"))
            assert(logs(4)==TLogging.MsgRecord("2"))
            assert(logs(5)==TLogging.MsgRecord("3"))
            assert(logs(6)==TLogging.MsgRecord("fizz"))
            assert(logs(7)==TLogging.MsgRecord("4"))
         }
  }

   test("minimal [disabled automatic coloring, short syntax also not work on ZIO. ]") {
      import cps.syntax.*

      //implicit val printCode = cps.macroFlags.PrintCode

      val program = asyncRIO[TLogging] {
         val ctr = await(Ref.make(0))
         val v = await(ctr.get)
         await(TLog.logMsg("AAA"))
         val records: IndexedSeq[TLogging.LogRecord] = await(TLog.lastRecords(20))
         records
      }
      val logService: TLogging.Service = new TLoggingImpl.Service
      val r = program.provideLayer( ZLayer.succeed(logService) )
      Runtime.default.unsafeRunToFuture(r)
   
   }

   
   test("make sure that FizBuzz run N times in async loop // automatic coloring removed") {

     import cps.syntax.*

      //implicit val printCode = cps.macroFlags.PrintCode
      //implicit val printTree = cps.macroFlags.PrintTree
      //implicit val debugLevel = cps.macroFlags.DebugLevel(20)

      val program = asyncRIO[TLogging] {
         // TODO: find issue, while ctr.get search for option.
         //  Now, let's do type ascription to force await.
         val ctr: Ref[Int] = await(Ref.make(0))
         while {
            val v: Int = await(ctr.get)
            await(TLog.logMsg(v.toString))
            if v % 3 == 0 then 
               await(TLog.logMsg("fizz"))
            if v % 5 == 0 then 
               await(TLog.logMsg("buzz"))
            await(ctr.update(_ + 1))
            v < 10 
         } do ()
         await(TLog.lastRecords(20))
      }

      val logService: TLogging.Service = new TLoggingImpl.Service
      val r = program.provideLayer( ZLayer.succeed(logService) )
      Runtime.default.unsafeRunToFuture(r).map{ logs =>
         //println(s"logs=$logs")
         assert(logs(0)==TLogging.MsgRecord("0"))
         assert(logs(1)==TLogging.MsgRecord("fizz"))
         assert(logs(6)==TLogging.MsgRecord("fizz"))
         assert(logs(7)==TLogging.MsgRecord("4"))
      }

   }
   
   
}
