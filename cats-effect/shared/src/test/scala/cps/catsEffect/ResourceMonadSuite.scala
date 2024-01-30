package cps.catsEffect

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
import java.nio.file.{OpenOption, StandardOpenOption}

class DataEmu(val data: String):
  var closed = false

  def close(): Unit =
    closed = true


class FileMinimalEmu(val name: String, val ref: Ref[IO,Vector[Array[Byte]]]):
  var closed = false

  def write(data: Array[Byte]): IO[Unit] =
      if (closed)
         throw new RuntimeException("File is closed")
      ref.update(parts => parts :+ data)

  def readAll(): IO[Array[Byte]] =
    ref.get.map(v => _readAll(v))

  def close(): Unit =
     closed = true
  
  def _readAll(v: Vector[Array[Byte]] ): Array[Byte] =
      val len = v.map(_.length).sum
      val retval = new Array[Byte](len)
      val _ = v.foldLeft(0){ (s,e) =>
        System.arraycopy(e,0,retval,s,e.length)
        s + e.length
      }
      retval



class ResourceMonadSuite extends CatsEffectSuite {

    def createDataEmu(data: String):Resource[IO,DataEmu] =
        Resource.make(acquire=IO.delay(new DataEmu(data)))(release = data => IO(data.close()))

    def createFileEmu(name: String):Resource[IO,FileMinimalEmu] =
        Resource.make(
             acquire=Ref.of[IO,Vector[Array[Byte]]](Vector.empty).map(r => new FileMinimalEmu(name,r))
        )(release = data => IO(data.close()))

    test("use cats resource as scope") {
        val prg = asyncScope[IO] {
            val input = await(createDataEmu("AAA AC"))
            val output = await(createFileEmu("output"))
            await(output.write(input.data.getBytes()))
            await(output.write("\nBBB BC".getBytes()))
            val data = await(output.readAll())
            (input.closed, output.closed, input, output, data)
        }
        prg.map{ v =>
           val (inputClosedInside, outputClosedInside, input, output, data) = v
           assert(!inputClosedInside)
           assert(!outputClosedInside)
           assert(input.closed)
           assert(output.closed)
           assert(new String(data).startsWith("AAA AC"))
        }
    }


    test("use cats resource as scope [removed automaticColoring]") {
      val prg = asyncScope[IO] {
          val input = await(createDataEmu("AAA AC"))
          val output = await(createFileEmu("output"))
          await(output.write(input.data.getBytes()))
          await(output.write("\nBBB BC".getBytes()))
          val data = output.readAll()
          (input.closed, output.closed, input, output, await(data))
      }
      prg.map{ v =>
         val (inputClosedInside, outputClosedInside, input, output, data) = v
         println(s"data=$data, new String(data)=${new String(data)}")
         assert(!inputClosedInside)
         assert(!outputClosedInside)
         assert(input.closed)
         assert(output.closed)
         assert(new String(data).startsWith("AAA AC"))
      }

  }


}