package cps.monads.scalaz

import cps._
import munit._

import scalaz.effect.IO

class IOPrintSute extends FunSuite {

  test("simple async/await over scalaz IO") {
    val program = async {
         val line = "line"
         await(IO.putStrLn(line))
    }
    program.unsafePerformIO()
  }

}

