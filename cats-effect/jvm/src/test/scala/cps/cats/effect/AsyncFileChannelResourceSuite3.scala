package cps.catsEffecct

import scala.annotation.experimental
import scala.concurrent.*
import scala.concurrent.duration.*
import scala.util.*
import scala.collection.mutable.ArrayBuffer

import cats.effect.*
import cats.effect.kernel.*

import cps.*
import cps.monads.{given,*}
import cps.monads.catsEffect.{given,*}

import munit.CatsEffectSuite

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.CompletionHandler
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import java.nio.file.{OpenOption, StandardOpenOption}



@experimental
object AsyncChannelApi3:

  type IOResourceDirect = CpsDirect[[X]=>>Resource[IO,X]]

  def openM(name: Path, options: OpenOption*): Resource[IO, AsynchronousFileChannel] =
     Resource.make(acquire = IO.delay(AsynchronousFileChannel.open(name,options:_*)))(release = f=>IO(f.close()))

  def open(name: Path, options: OpenOption*)(using IOResourceDirect): AsynchronousFileChannel =
     await(openM(name,options: _*))
  

  def readM(input: AsynchronousFileChannel, bufSize: Int): IO[ByteBuffer] =
    IO.async_[ByteBuffer]{ cb =>
       val buffer = ByteBuffer.allocate(bufSize)
       input.read(buffer, 0, (), new CompletionHandler[Integer, Unit]() {
                              def completed(result: Integer, attachment: Unit) =
                                cb(Right(buffer))	
                              def failed(ex: Throwable, attachment: Unit) =
                                cb(Left(ex))
                        })
    }

  // TODO: introduce subtype(CpsDirect) for conversions and use CpsDirect[IO] here
  def read(input: AsynchronousFileChannel, bufSize: Int)(using IOResourceDirect): ByteBuffer =
    await(readM(input, bufSize))

  def writeM(output: AsynchronousFileChannel, buffer: ByteBuffer): IO[Int] =
    IO.async_[Int]{ cb =>
        output.write(buffer, 0, (), new CompletionHandler[Integer, Unit]() {
                                  def completed(result: Integer, attachment: Unit) =
                                      val res: Int = result
                                      cb(Right(res))

                                   def failed(ex: Throwable, attachment: Unit) =
                                      cb(Left(ex))
                            })
    }
  

  def write(output: AsynchronousFileChannel, buffer: ByteBuffer)(using IOResourceDirect): Int =
    await(writeM(output, buffer))






@experimental
class AsyncFileChannelResourceSuite3 extends CatsEffectSuite {

    import AsyncChannelApi3.*

    val BUF_SIZE = 64000

    test("use cats resource with AsynchronousFileChannel3") {
        import StandardOpenOption.*
        //implicit val printCode = cps.macros.flags.PrintCode
        val prg = asyncScope[IO] {
            val input = open(Paths.get("cats-effect/jvm/src/test/resources/input"),READ)
            val outputName = Files.createTempFile("output-async3",null)
            val output = open(outputName,WRITE, CREATE, TRUNCATE_EXISTING)
            var nBytes = 0
            while 
              val buffer = read(input, BUF_SIZE)
              val cBytes = buffer.position()
              write(output, buffer)
              nBytes += cBytes
              cBytes == BUF_SIZE
            do ()
            (nBytes, outputName)
        }
        prg.map{ 
             (n, name) =>
                //println(s"n = $n")
                Files.delete(name)
                //println(s"messages = $messages")
                assert(n > 0)
        }

    }

}


