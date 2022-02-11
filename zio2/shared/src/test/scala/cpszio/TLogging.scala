package cpszio

import zio._

/*
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

type TLogging = Has[TLogging.Service]

object TLog {

  def lastOp(): RIO[TLogging, Option[String]] =
    ZIO.accessM(_.get.lastOp())

  def logOp(op:String): RIO[TLogging, Unit] =
    ZIO.accessM(_.get.logOp(op))

  def logMsg(msg:String): RIO[TLogging, Unit] =
    ZIO.accessM(_.get.logMsg(msg))

  def logThrowable(ex: Throwable): RIO[TLogging, Unit] =
    ZIO.accessM(_.get.logThrowable(ex))
   
  def lastRecords(n: Int): RIO[TLogging, IndexedSeq[TLogging.LogRecord]] =
    ZIO.accessM(_.get.lastRecords(n))

}

object TLoggingImpl {

  import TLogging._

  class Service extends TLogging.Service {

     // will not use multithreading in tests.
     var lastOpCache: Option[String] = None
     var log: IndexedSeq[LogRecord] = IndexedSeq()

     def lastOp(): Task[Option[String]] = Task(lastOpCache)

     def logOp(op: String): UIO[Unit] =
          UIO.apply{
             lastOpCache = Some(op)
             log = log :+ OpRecord(op)
          }
     
     def logMsg(msg: String): UIO[Unit] =
          UIO.apply{
             log = log :+ MsgRecord(msg)
          }

     def logThrowable(ex: Throwable): Task[Unit] =
          Task.effect{
             log = log :+ ExceptionRecord(ex)
          }

     def lastRecords(n: Int): Task[IndexedSeq[LogRecord]] =
          Task{ log.takeRight(n)  }

  }

}*/
