package cps.zioloomtest

import zio.*
import cps.*
import cps.monads.zio.{given,*}
import munit.*


class LoomHOFunSuite extends FunSuite {

  def incr(x:Int)(using trace:Trace): ZIO[Any,Throwable,Int] =
    ZIO.suspend[Any,Int](  ZIO.succeed(x+1))(trace)

  test("check apply function with await as argument to MyList.map") {
    val c=async[[X]=>>ZIO[Any,Throwable,X]] {
      val list0 = MyList.create(1,2,3,4,5)
      val list1 = list0.map(x => await(ZIO.succeed(x+1)))
      assert (list1 == MyList.create(2,3,4,5,6))
    }
    Unsafe.unsafe(implicit unsafe => Runtime.default.unsafe.runToFuture(c))
  }


  test("catch exception from failed operation inside runtime await") {
    val c=async[[X]=>>ZIO[Any,Throwable,X]] {
      val list0 = MyList.create(1,2,3,4,5)
      try {
        val list1 = list0.map[Int](x => await(ZIO.fail(new RuntimeException("test"))))
        assert (false)
      }catch{
        case ex: RuntimeException =>
          assert(ex.getMessage() == "test")
      }
    }
    Unsafe.unsafe(implicit unsafe => Runtime.default.unsafe.runToFuture(c))
  }



  test("check apply function with await as argument to MyList.foldLeft") {
    val c=async[[X]=>>ZIO[Any,Throwable,X]] {
      val list0 = MyList.create(1,2,3,4,5)
      val sum = list0.foldLeft(0)((s,x) => await(ZIO.succeed(s+x)))
      assert (sum == 15)
    }
    Unsafe.unsafe(implicit unsafe => Runtime.default.unsafe.runToFuture(c))
  }


  def twice[A](f: A=>A)(a:A):A = f(f(a))

  test("check await in the argument of twice") {
    val c = async[[X]=>>ZIO[Any,Throwable,X]] {
      val r = twice[Int](x => await(incr(x)))(1)
      assert(r == 3)
    }
    c
  }


  test("check await in the argument of twice inside MyList.map") {
    val c = async[[X]=>>ZIO[Any,Throwable,X]] {
      val list0 = MyList.create(1,2,3,4,5)
      val list1 = list0.map(x => twice[Int](x => await(incr(x)))(x))
      assert(list1 == MyList.create(3,4,5,6,7))
    }
    c
  }




}
