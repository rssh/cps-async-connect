package cps.catsEffect

import scala.language.implicitConversions
import scala.util.*
import scala.concurrent.duration.*

import cats.*
import cats.effect.*


import cps.*

import cps.monads.catsEffect.{given,*}

import munit.CatsEffectSuite


class LazyEffectSuite extends CatsEffectSuite {

  test("make sure that async expressions are not evaluating early") {
    //implicit val printCode = cps.macroFlags.PrintCode
    var x = 0
    val c = async {
      x = 1
    }
    assert(x == 0)
    c 
  }   

  test("make sure that exception is catched inside async expression ") {
     //implicit val printCode = cps.macroFlags.PrintCode
     val c1 = async {
        throw new RuntimeException("AAA")
     }
     async {
       var x = 0
       var y = 0
       try {
         await(c1)
         x = 1
       }catch{
         case ex: RuntimeException =>
            assert(ex.getMessage()=="AAA")
            y = 2
       }
       assert(x == 0)
       assert(y == 2)
     }
  }   

  

}
