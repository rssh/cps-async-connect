package cps.celoom

import cats.effect.*
import cats.effect.kernel.*

import cps.*
import cps.monads.catsEffect.{given,*}

import munit.CatsEffectSuite


class LoomHOFunSuite extends CatsEffectSuite {


  test("check apply function with await as argument to MyList.map") {
    val c=async[IO] {
      val list0 = MyList.create(1,2,3,4,5)
      val list1 = list0.map(x => await(IO(x+1)))
      assert (list1 == MyList.create(2,3,4,5,6))
    }
    c
  }

  test("check apply function with await as argument to MyList.foldLeft") {
    val c=async[IO] {
      val list0 = MyList.create(1,2,3,4,5)
      val sum = list0.foldLeft(0)((s,x) => await(IO(s+x)))
      assert (sum == 15)
    }
    c
  }

  def twice[A](f: A=>A)(a:A):A = f(f(a))

  test("check await in the argument of twice") {
    val c = async[IO] {
      val r = twice[Int](x => await(IO(x+1)))(1)
      assert(r == 3)
    }
    c
  }

  test("check await in the argument of twice inside MyList.map") {
    val c = async[IO] {
      val list0 = MyList.create(1,2,3,4,5)
      val list1 = list0.map(x => twice[Int](x => await(IO(x+1)))(x))
      assert(list1 == MyList.create(3,4,5,6,7))
    }
    c
  }

  test("catch exception from failed operation inside runtime await") {
    val c = async[IO] {
      val list0 = MyList.create(1, 2, 3, 4, 5)
      try {
        val list1 = list0.map[Int](x => await(IO.raiseError(new RuntimeException("test"))))
        assert(false)
      } catch {
        case ex: RuntimeException =>
          assert(ex.getMessage() == "test")
      }
    }
    c
  }


}
