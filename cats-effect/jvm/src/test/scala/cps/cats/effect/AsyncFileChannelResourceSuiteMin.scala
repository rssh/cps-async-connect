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



//def openAsyncFileChannelM(name: String, options: OpenOption*): Resource[IO, AsynchronousFileChannel] =
def openAsyncFileChannelM(name: String, options: OpenOption*): Resource[IO, AsynchronousFileChannel] =
  throw RuntimeException("???")

  

def writeM(output: AsynchronousFileChannel, buffer: Int): IO[Int] =
  throw RuntimeException("???")


class AsyncFileChannelResourceSuiteMin extends CatsEffectSuite {

    val BUF_SIZE = 64000

    test("use cats resource with AsynchronousFileChannel - compile only") {
        import StandardOpenOption.*
        //implicit val printCode = cps.macros.flags.PrintCode
        //implicit val debugLavel = cps.macros.flags.DebugLevel(15) 
        try {
          val prg = asyncScope[IO] {
            val output = openAsyncFileChannelM("outputName")
            writeM(output, 1)
            1
          }
          prg.recover{case e => e.getMessage}
        } catch {
          case ex: RuntimeException =>
            IO.pure(ex.getMessage)
        }
    }

}


