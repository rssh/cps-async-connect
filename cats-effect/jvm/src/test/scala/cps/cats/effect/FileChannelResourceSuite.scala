package cps.cats.effecct

import scala.concurrent.duration._
import scala.collection.mutable.ArrayBuffer

import cats.effect.*
import cats.effect.kernel.*

import cps.*
import cps.monads.catsEffect.{given,*}

import munit.CatsEffectSuite

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import java.nio.file.{OpenOption, StandardOpenOption}

import cps.automaticColoring.given
import scala.language.implicitConversions



def openFileChannel(name: Path, options: OpenOption*): Resource[IO, FileChannel] =
  Resource.make(acquire = IO.delay(FileChannel.open(name,options:_*)))(release = f=>IO(f.close()))

  

class FileChannelResourceSuite extends CatsEffectSuite {

    test("use cats resource") {
        import StandardOpenOption.*
        val prg = asyncScope[IO] {
            val input = openFileChannel(Paths.get("cats-effect/jvm/src/test/resources/input"),READ)
            val outputName = Files.createTempFile("output",null)
            val output = openFileChannel(outputName,WRITE, CREATE, TRUNCATE_EXISTING)
            (output.transferFrom(input,0,Long.MaxValue), outputName)
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




