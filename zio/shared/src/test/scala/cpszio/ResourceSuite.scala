package cpszio


import zio.*
import munit.*
import scala.concurrent.duration.*
import scala.collection.mutable.ArrayBuffer

import cps.*
import cps.monads.zio.{given,*}


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



class ResourceSuite extends FunSuite {

    import concurrent.ExecutionContext.Implicits.global


    def makeWriterEmu(): TaskManaged[WriterEmu] =
        ZManaged.make(ZIO.effect(new WriterEmu()))(a => ZIO.effectTotal(a.close()))

    test("using ZManaged") {
        implicit val printCode = cps.macros.flags.PrintCode
        val prg = async[Task] {
            val r = makeWriterEmu()
            val ctr = await(Ref.make(0))
            ZManaged.using(r) { a => 
                val c0 = await(ctr.get)
                a.write(s"AAA-${c0}")
                await(ctr.update(_ + 1))
                val c1 = await(ctr.get)
                a.write(s"BBB-${c1}")
                (a, a.messages.toList, a.isClosed)
            }
        }
        Runtime.default.unsafeRunToFuture(prg).map{ 
            (a, messages, isClosedInside) =>
                //println(s"a = $a")
                //println(s"messages = $messages")
                assert(a.isClosed)
                assert(!isClosedInside)
        }

    }

}




