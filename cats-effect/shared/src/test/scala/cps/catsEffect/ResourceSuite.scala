package cps.catsEffecct

import scala.concurrent.duration._
import scala.collection.mutable.ArrayBuffer

import cats.effect.*
import cats.effect.kernel.*

import cps.*
import cps.monads.catsEffect.{given,*}

import munit.CatsEffectSuite


class WriterEmu {

   var isClosed: Boolean = false
   val messages: ArrayBuffer[String] = ArrayBuffer.empty

   def write(message:String): Unit = {
        if (!isClosed)
            messages.append(message)
        else
            throw new IllegalStateException("resource is already closed")
   }

   def close(): Unit =
    isClosed = true

}




class ResourceSuite extends CatsEffectSuite {

    def makeWriterEmu(): Resource[IO,WriterEmu] =
        Resource.make(IO.delay(new WriterEmu()))(a => IO.delay(a.close()))

    test("use cats resource") {
        val prg = async[IO] {
            val r = makeWriterEmu()
            val ctr = await(IO.ref(0))
            using(r) { a => 
                val c0 = await(ctr.get)
                a.write(s"AAA-${c0}")
                await(ctr.update(_ + 1))
                val c1 = await(ctr.get)
                a.write(s"BBB-${c1}")
                (a, a.messages.toList, a.isClosed)
            }
        }
        prg.map{ 
            (a, messages, isClosedInside) =>
                //println(s"a = $a")
                //println(s"messages = $messages")
                assert(a.isClosed)
                assert(!isClosedInside)
        }

    }

}




