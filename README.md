

 This is a helper connect objects for providing [dotty-cps-async](http://https://github.com/rssh/dotty-cps-async) CpsAsyncMonad typeclasses for common effect stacks.


## cats-effects:

```
  libraryDependencies += "com.github.rssh" %%% "cps-async-connect-cats-effect" % "0.2.0"  
```


Usage:

```scala
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
  libraryDependencies += "com.github.rssh" %%% "cps-async-connect-scalaz" % "0.2.0"  
```
  * IO - cps.monads.scalaz.scalazIO  (implements CpsTryMonad)

