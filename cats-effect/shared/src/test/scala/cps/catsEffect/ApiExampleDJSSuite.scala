package cps.catsEffect

import scala.util.*
import scala.concurrent.duration.*

import cats.*
import cats.effect.*


import cps.*

import cps.monads.catsEffect.{given,*}

import munit.CatsEffectSuite


object TestFuns {

  def myFun(using RunContext): IO[Boolean] = async[IO] {
    val results1 = await(talkToServer("request1", None))
    await(IO.sleep(100.millis))
    val results2 = await(talkToServer("request2", Some(results1.data)))
    if results2.isOk then
       await(writeToFile(results2.data))
       await(IO.println("done"))
       true
    else
       await(IO.println("abort abort abort"))
       false
  }

  

  case class Result(
    isOk: Boolean,
    data: String
  )

  case class RunContext(bingings: Map[String,Try[Result]])

  def talkToServer(request:String, arg: Option[String])(using ctx: RunContext): IO[Result] = 
    val key = s"talk-${request}-${arg.toString}"
    handleKey(key) 

  def writeToFile(data:String)(using ctx:RunContext): IO[Unit] =
    val key = s"writeToFile-${data}"
    handleKey(key).map(_ => ())

  def handleKey(key: String)(using ctx: RunContext): IO[Result] =
    ctx.bingings.get(key) match
      case Some(tryValue) => 
        tryValue match
          case Success(r) => IO.delay(r)
          case Failure(ex) => IO.raiseError(ex)
      case None =>
          IO.raiseError(RuntimeException(s"key $key is not found in run context"))

}

class ApiExampleDJSSuite extends CatsEffectSuite {

  import TestFuns.*
  
  test("make sure than API text running ok when all is ok") {
    given RunContext = RunContext(Map(
      "talk-request1-None" -> Success(Result(true,"answer1")),
      "talk-request2-Some(answer1)" -> Success(Result(true,"answer2")),
      "writeToFile-answer2" -> Success(Result(true,"ok"))
    )) 
    TestFuns.myFun
  }   

  test("make sure than we return error when 1-st text failed") {
    given RunContext = RunContext(Map(
      "talk-request1-None" -> Failure(RuntimeException("x1")),
      "talk-request2-Some(answer1)" -> Success(Result(true,"answer2")),
      "writeToFile-answer2" -> Success(Result(true,"ok"))
    )) 
    interceptIO[RuntimeException](
      TestFuns.myFun
    )
  }   

  test("make sure than we return false when 2-st request is not ok") {
    given RunContext = RunContext(Map(
      "talk-request1-None" -> Success(Result(true,"answer1")),
      "talk-request2-Some(answer1)" -> Success(Result(false,"answer2")),
      "writeToFile-answer2" -> Success(Result(true,"ok"))
    )) 
    assertIO(TestFuns.myFun, false)

  }
  

}
