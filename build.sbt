import sbt.Keys.autoCompilerPlugins

//val dottyVersion = "3.4.0-RC1-bin-SNAPSHOT"
val dottyVersion = "3.6.2"
val dottyCpsAsyncVersion = "0.9.23"

ThisBuild/version := "0.9.23"
ThisBuild/versionScheme := Some("semver-spec")
ThisBuild/organization := "com.github.rssh"
ThisBuild/resolvers += Resolver.mavenLocal

Global / concurrentRestrictions += Tags.limit(ScalaJSTags.Link, 1)
Global / concurrentRestrictions += Tags.limit(ScalaJSTags.Link, 1)

lazy val commonSettings = Seq(
   scalaVersion := dottyVersion,
   libraryDependencies += "com.github.rssh" %%% "dotty-cps-async-next" % dottyCpsAsyncVersion,
   libraryDependencies += "org.scalameta" %%% "munit" % "1.0.3" % Test,
   testFrameworks += new TestFramework("munit.Framework"),
   scalacOptions ++= Seq( "-Wvalue-discard", "-Wnonunit-statement"),
   autoCompilerPlugins := true,
   addCompilerPlugin("com.github.rssh" %% "dotty-cps-async-compiler-plugin" % dottyCpsAsyncVersion)
)


lazy val scalaz  = crossProject(JSPlatform, JVMPlatform)
  .in(file("scalaz"))
  .settings(
    commonSettings,
    name := "cps-async-connect-scalaz-next",
    libraryDependencies += "org.scalaz" %%% "scalaz-effect" % "7.4.0-M14" ,
    libraryDependencies += "org.scalaz" %%% "scalaz-core" % "7.4.0-M14" 
  ).jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    scalaJSUseMainModuleInitializer := true
  )


lazy val catsEffect  = crossProject(JSPlatform, JVMPlatform)
  .in(file("cats-effect"))
  .settings(
    commonSettings,
    name := "cps-async-connect-cats-effect-next",
    libraryDependencies += "org.typelevel" %%% "cats-effect" % "3.5.7",
    libraryDependencies += "org.typelevel" %%% "munit-cats-effect" % "2.0.0" % Test
  ).jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    scalaJSUseMainModuleInitializer := true,
  ).jvmSettings(
    scalacOptions ++= Seq( "-unchecked", "-explain")
  )

lazy val catsEffectLoom = project.in(file("cats-effect-loom"))
                                 .dependsOn(catsEffect.jvm)
                                 .settings(
                                     commonSettings,
                                     name := "cps-async-connect-cats-effect-loom",
                                     libraryDependencies ++= Seq(
                                       "com.github.rssh" %% "dotty-cps-async-loom" % dottyCpsAsyncVersion,
                                       "org.typelevel" %%% "munit-cats-effect" % "2.0.0" % Test
                                     ),
                                     scalacOptions += "-Xtarget:21"
                                 )


lazy val monix  = crossProject(JSPlatform, JVMPlatform)
  .in(file("monix"))
  .settings(
    commonSettings,
    name := "cps-async-connect-monix-next",
    libraryDependencies += "io.monix" %%% "monix" % "3.4.1",
  ).jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    scalaJSUseMainModuleInitializer := true
  ).jvmSettings(
  )

lazy val zio  = crossProject(JSPlatform, JVMPlatform)   
  .in(file("zio"))
  .settings(
    commonSettings,
    name := "cps-async-connect-zio-next",
    libraryDependencies ++= Seq(
      "dev.zio" %%% "zio" % "1.0.18",
      "dev.zio" %%% "zio-streams" % "1.0.18",
    )
  ).jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    //scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.6.0",
      "io.github.cquiroz" %%% "scala-java-time-tzdb" % "2.6.0"
    ),
  ).jvmSettings(
    scalacOptions ++= Seq( "-unchecked", "-Ydebug-trace", "-Ydebug-names", "-Xprint-types",
                            "-Ydebug", "-uniqid", "-Ycheck:macros",  "-Yprint-syms" )
  )

lazy val zio2  = crossProject(JSPlatform,JVMPlatform) 
  .in(file("zio2"))
  .settings(
    commonSettings,
    name := "cps-async-connect-zio2-next",
    libraryDependencies ++= Seq(
      "dev.zio" %%% "zio" % "2.1.13",
      "dev.zio" %%% "zio-managed" % "2.1.13",
      "dev.zio" %%% "zio-streams" % "2.1.13",
    )
  ).jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    //scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.6.0",
      "io.github.cquiroz" %%% "scala-java-time-tzdb" % "2.6.0"
    ),
  ).jvmSettings(
    scalacOptions ++= Seq( "-unchecked", "-Ydebug-trace", "-Ydebug-names", "-Xprint-types",
                            "-Ydebug", "-uniqid", "-Ycheck:macros",  "-Yprint-syms" 
                           )
  )

lazy val zio2Loom = project.in(file("zio2-loom"))
  .dependsOn(zio2.jvm)
  .settings(
    commonSettings,
    name := "cps-async-connect-zio2-loom-next",
    libraryDependencies ++= Seq(
      "com.github.rssh" %% "dotty-cps-async-loom" % dottyCpsAsyncVersion
    ),
    scalacOptions += "-Xtarget:21"
  )


lazy val streamFs2 = crossProject(JSPlatform, JVMPlatform)
                     .in(file("stream-fs2"))
                     .dependsOn(catsEffect)
                     .settings(
                         commonSettings,
                         name := "cps-async-connect-fs2-next",
                         libraryDependencies ++= Seq(
                             "co.fs2" %%% "fs2-core" % "3.11.0",
                             "org.typelevel" %%% "munit-cats-effect" % "2.0.0" % Test
                         )
                      )

lazy val streamAkka = (project in file("stream-akka")).
                      settings(
                         commonSettings,
                         name := "cps-async-connect-akka-stream-next",
                         scalacOptions += "-explain",
                         resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
                         libraryDependencies ++= Seq(
                            ("com.typesafe.akka" %% "akka-stream" % "2.10.0")
                         )
                      )

lazy val streamPekko = (project in file("stream-pekko")).
  settings(
    commonSettings,
    name := "cps-async-connect-pekko-stream-next",
    scalacOptions += "-explain",
    libraryDependencies ++= Seq(
      ("org.apache.pekko" %% "pekko-stream" % "1.1.2")
    )
  )


lazy val probabilityMonad = (project in file("probability-monad")).
                             settings(
                               commonSettings,
                               name := "cps-async-connect-probabiliy-monad-next",
                               libraryDependencies ++= Seq(
                                  ("org.jliszka" %%% "probability-monad" % "1.0.4").cross(CrossVersion.for3Use2_13)
                               )
                             )


lazy val root = (project in file("."))
                .aggregate(catsEffect.jvm, catsEffect.js,
                           catsEffectLoom,
                           monix.jvm, monix.js,
                           scalaz.jvm, scalaz.js , 
                           zio.jvm,  zio.js,
                           zio2.jvm,  zio2.js, 
                           zio2Loom,
                           streamFs2.jvm, streamFs2.js,
                           streamAkka,
                           streamPekko,
                           probabilityMonad
                )
                .settings(
                   publish := {},
                   publishLocal := {},
                   publishArtifact := false
                )


