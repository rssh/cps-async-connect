package cps.catsEffecct

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



object AsyncChannelApi:

  def open(name: Path, options: OpenOption*): Resource[IO, AsynchronousFileChannel] =
     Resource.make(acquire = IO.delay(AsynchronousFileChannel.open(name,options:_*)))(release = f=>IO(f.close()))

  def read(input: AsynchronousFileChannel, bufSize: Int): IO[ByteBuffer] =
    IO.async_[ByteBuffer]{ cb =>
       val buffer = ByteBuffer.allocate(bufSize)
       input.read(buffer, 0, (), new CompletionHandler[Integer, Unit]() {
                              def completed(result: Integer, attachment: Unit) =
                                cb(Right(buffer))	
                              def failed(ex: Throwable, attachment: Unit) =
                                cb(Left(ex))
                        })
    }


  def write(output: AsynchronousFileChannel, buffer: ByteBuffer): IO[Int] =
    IO.async_[Int]{ cb =>
        output.write(buffer, 0, (), new CompletionHandler[Integer, Unit]() {
                                  def completed(result: Integer, attachment: Unit) =
                                      val res: Int = result
                                      cb(Right(res))

                                   def failed(ex: Throwable, attachment: Unit) =
                                      cb(Left(ex))
                            })
    }

import AsyncChannelApi.*


class AsyncFileChannelResourceSuite2 extends CatsEffectSuite {

    val BUF_SIZE = 64000

    test("use cats resource with AsynchronousFileChannel2") {
        import StandardOpenOption.*
        //implicit val printCode = cps.macros.flags.PrintCode
        val prg = asyncScope[IO] {
            val input = await(open(Paths.get("cats-effect/jvm/src/test/resources/input"),READ))
            val outputName = Files.createTempFile("output-async2",null)
            val output = await(open(outputName,WRITE, CREATE, TRUNCATE_EXISTING))
            var nBytes = 0
            while 
              val buffer = await(read(input, BUF_SIZE))
              val cBytes = buffer.position()
              val _ = await(write(output, buffer))
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


