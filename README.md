

 This is a helper connect objects for providing [dotty-cps-async](https://github.com/rssh/dotty-cps-async) CpsAsyncMonad typeclasses for common effect stacks and streaming libraries.


## cats-effects:

```
  libraryDependencies += "com.github.rssh" %%% "cps-async-connect-cats-effect" % "0.9.9-1"  
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

  * IO  -  catsIO  (implements CpsConcurrentEffectMonad with conversion to Future)
  * Generic `F[_]:Async` - catsAsync (implements CpsAsyncMonad)
  * Generic `F[_]:MonadThrow` - catsMonadThrow (implements CpsTryMonad)
  * Generic `F[_]:Monad` - catsMonad (implements CpsMonad)

Also implemented pseudo-synchronious interface for resources, i.e. for `r:Resource[F,A]` it is possible to write:

```scala
async[F] {
  .......
  Resource.using(r1,r2){ file1, file2 =>
    val data = await(fetchData(url))
    file1.write(data)
    file1.write(s"data fetched from $url")
  }
} 
```

or

```scala
async[F] {
  ....
  r.useOn{file =>
     val data = await(fetchData())
     file.write(data)
  }
}
```

instead

```
 await(r.use{
    fetchData().map(data => f.write(data))
 })  
```

# monix:

```
  libraryDependencies += "com.github.rssh" %%% "cps-async-connect-monix" % "0.9.9-1"  
```


Usage:

```scala
import cps.*
import cps.monads.monix.given
import monix.eval.Task

...
def doSomething(): Task[T] = async[Task] {
   ...
}

```

```scala
import cps.*
import monix.*
import monix.reactive.*
import cps.monads.monix.given
import cps.stream.monix.given

def intStream() = asyncStream[Observable[Int]] { out =>
    for(i <- 1 to N) {
       out.emit(i)
    }
}

```


## scalaz IO:

```
  libraryDependencies += "com.github.rssh" %%% "cps-async-connect-scalaz" % "0.9.9-1"  
```

  * IO - cps.monads.scalaz.scalazIO  (implements CpsTryMonad)


## zio:

for 1.x:

```
  libraryDependencies += "com.github.rssh" %%% "cps-async-connect-zio" % "0.9.9-1"  
```

for 2.x:

```
  libraryDependencies += "com.github.rssh" %%% "cps-async-connect-zio2" % "0.9.9-1"  
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


  * ZIO  -  `asyncZIO[R,E]` as shortcat for `async[[X]=>>ZIO[R,E,X]]` (implements `CpsAsyncMonad` with conversion to `Future` if we have given `Runtime` in scope.)
  * RIO  -  use asyncRIO[R]  (implements CpsAsyncMonad with conversion)
  * Task  -  use async[Task]  (implements CpsAsyncMonad with conversion)
  * URIO  -  use asyncURIO[R]  (implements CpsMonad)
  
Also implement `using` pseudo-syntax for ZManaged: 

```
asyncRIO[R] {
  val managedResource = Managed.make(Queue.unbounded[Int])(_.shutdown)
  ZManaged.using(managedResource, secondResource) { queue =>
     doSomething()  // can use awaits inside.
  }

}
```

And generator syntax for ZStream:

```
val stream = asyncStream[Stream[Throwable,Int]] { out =>
       for(i <- 1 to N) {
         out.emit(i)
       }
}
```


## akka-streams


```
  libraryDependencies += "com.github.rssh" %%% "cps-async-connect-akka-stream" % "0.9.9-1"  
```

Generator syntax for akka source.


## fs2 streams

```
  libraryDependencies += "com.github.rssh" %%% "cps-async-connect-fs2 % "0.9.9-1"  
```

Generator syntax for fs2





