

 This is a helper connect objects for providing [dotty-cps-async](http://https://github.com/rssh/dotty-cps-async) CpsAsyncMonad typeclasses for common effect stacks.


## cats-effects:

```
  libraryDependencies += "com.github.rssh" %%% "cps-async-connect-cats-effect" % "0.5.0"  
```


Usage:

```scala
import cps._
import cps.monads.cats.given

...
def doSomething(): IO[T] = async[IO] {
   ...
}

```

 or import specific class to allow compiler to deduce given monad automatically.

  * IO  -  catsIO  (implements CpsAsyncMonad with conversion to Future)
  * Generic `F[_]:Async` - catsAsync (implements CpsAsyncMonad)
  * Generic `F[_]:MonadThrow` - catsMonadThrow (implements CpsTryMonad)
  * Generic `F[_]:Monad` - catsMonad (implements CpsMonad)


## scalaz IO:

```
  libraryDependencies += "com.github.rssh" %%% "cps-async-connect-scalaz" % "0.3.0"  
```

  * IO - cps.monads.scalaz.scalazIO  (implements CpsTryMonad)


## zio:

```
  libraryDependencies += "com.github.rssh" %%% "cps-async-connect-zio" % "0.4.0"  
```

Usage:

```scala
import cps.*
import cps.monads.zio.{given,*}

 val program = asyncRIO[R] {
    .....
 }

```

or for task:

```scala

 val program = async[Task] {
   ....
 }


```

for ZIO with custom error `E` you should have given `ThrowableAdapter[R,E]` which will map `E` and `Throwable` in both directions.

```scala
case class MyError(...)

given ThrowableAdapter[R] with

     def toThrowable(e: MyError): Throwable = ...
        
     def fromThrowable[A](e:Throwable): ZIO[R,E,A] = ...

```


  * ZIO  -  `asyncZIO[R,E]` as shortcat for `async[[X]=>>ZIO[R,E,X]]` (implements `CpsAsyncMonad` with conversion to `Future` if we have given `Runtime` in scope.)
  * RIO  -  use asyncRIO[R]  (implements CpsAsyncMonad with conversion)
  * Task  -  use async[Task]  (implements CpsAsyncMonad with conversion)
  * URIO  -  use asyncURIO[R]  (implements CpsMonad)
  


