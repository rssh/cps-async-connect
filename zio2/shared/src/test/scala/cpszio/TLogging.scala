package cpszio

import zio._


object TLogging {

  sealed class LogRecord
  case class OpRecord(op: String) extends LogRecord
  case class MsgRecord(msg: String) extends LogRecord
  case class ExceptionRecord(ex: Throwable) extends LogRecord

  trait Service {
    def lastOp(): Task[Option[String]]
    def logOp(op: String): UIO[Unit]
    def logMsg(msg: String): UIO[Unit]
    def logThrowable(ex: Throwable): Task[Unit]

    def lastRecords(n: Int): Task[IndexedSeq[LogRecord]]
  }

}


object TLog {

  def lastOp(): RIO[TLogging.Service, Option[String]] =
    ZIO.serviceWithZIO[TLogging.Service](_.lastOp())

  def logOp(op:String): RIO[TLogging.Service, Unit] =
    ZIO.serviceWithZIO(_.logOp(op))

  def logMsg(msg:String): RIO[TLogging.Service, Unit] =
    ZIO.serviceWithZIO(_.logMsg(msg))

  def logThrowable(ex: Throwable): RIO[TLogging.Service, Unit] =
    ZIO.serviceWithZIO(_.logThrowable(ex))
   
  def lastRecords(n: Int): RIO[TLogging.Service, IndexedSeq[TLogging.LogRecord]] =
    ZIO.serviceWithZIO(_.lastRecords(n))

}


object TLoggingImpl {

  import TLogging._

  val layer: URLayer[Any,TLogging.Service] = ZLayer.fromFunction(()=>Service())


  class Service extends TLogging.Service {

     // will not use multithreading in tests.
     var lastOpCache: Option[String] = None
     var log: IndexedSeq[LogRecord] = IndexedSeq()

     def lastOp(): Task[Option[String]] = ZIO.attempt(lastOpCache)

     def logOp(op: String): UIO[Unit] =
          ZIO.succeed{
             lastOpCache = Some(op)
             log = log :+ OpRecord(op)
          }
     
     def logMsg(msg: String): UIO[Unit] =
          ZIO.succeed{
             log = log :+ MsgRecord(msg)
          }

     def logThrowable(ex: Throwable): Task[Unit] =
          ZIO.attempt{
             log = log :+ ExceptionRecord(ex)
          }

     def lastRecords(n: Int): Task[IndexedSeq[LogRecord]] =
          ZIO.attempt{ log.takeRight(n)  }

  }

}
