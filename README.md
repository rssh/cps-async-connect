
 This is a helper connect objects for providing dotty-cps-async CpsAsyncMonad typeclasses for


* cats-effects:
  * IO
  * Generic `F[_]:Async` 

```
  libraryDependencies += "com.github.rssh" %%% "cps-async-connect-cats-effect" % "0.1.0"  
```

Usage:

```scala
import cps.monads.cats.given

...
def doSomething(): IO[T] = async {
  

}

```


* scalaz IO:

```
  libraryDependencies += "com.github.rssh" %%% "cps-async-connect-scalaz" % "0.1.0"  
```

