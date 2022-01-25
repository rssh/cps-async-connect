package cps.cats.effecct

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

import cps.automaticColoring.given
import scala.language.implicitConversions




def openAsyncFileChannel(name: Path, options: OpenOption*): Resource[IO, AsynchronousFileChannel] =
  Resource.make(acquire = IO.delay(AsynchronousFileChannel.open(name,options:_*)))(release = f=>IO(f.close()))

  
def read(input: AsynchronousFileChannel, buffer: ByteBuffer): IO[Int] =
  IO.async_[Int]{ cb =>
    input.read(buffer, 0, (), new CompletionHandler[Integer, Unit]() {
                              def completed(result: Integer, attachment: Unit) =
                                val res: Int = result
                                cb(Right(res))	
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


class AsyncFileChannelResourceSuite extends CatsEffectSuite {

    val BUF_SIZE = 64000

    test("use cats resource with AsynchronousFileChannel") {
        import StandardOpenOption.*
        implicit val printCode = cps.macros.flags.PrintCode
        implicit val debugLavel = cps.macros.flags.DebugLevel(20) 
        val prg = asyncScope[IO] {
            val input = openAsyncFileChannel(Paths.get("cats-effect/jvm/src/test/resources/input"),READ)
             val outputName = Files.createTempFile("output-async",null)
            val output = openAsyncFileChannel(outputName,WRITE, CREATE, TRUNCATE_EXISTING)
            val buffer = ByteBuffer.allocate(BUF_SIZE)
            var nBytes = 0
            var cBytes = 0
            while 
              cBytes = read(input, buffer.clear())
              println(s"read loop, cBytes=${cBytes}")
              if (cBytes > 0) then
                  write(output, buffer)
                  nBytes += cBytes
              cBytes == BUF_SIZE
            do ()
            (nBytes, outputName)
        }
        prg.map{ 
             (n, name) =>
                println(s"n = $n")
                Files.delete(name)
                //println(s"messages = $messages")
                assert(n > 0)
        }

    }

}


