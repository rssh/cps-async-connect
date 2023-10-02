import sbt.Keys.autoCompilerPlugins

//val dottyVersion = "3.4.0-RC1-bin-SNAPSHOT"
val dottyVersion = "3.3.1"
val dottyCpsAsyncVersion = "0.9.19"

ThisBuild/version := "0.9.19"
ThisBuild/versionScheme := Some("semver-spec")
ThisBuild/organization := "com.github.rssh"
ThisBuild/resolvers += Resolver.mavenLocal

Global / concurrentRestrictions += Tags.limit(ScalaJSTags.Link, 1)
Global / concurrentRestrictions += Tags.limit(ScalaJSTags.Link, 1)

lazy val commonSettings = Seq(
   scalaVersion := dottyVersion,
   libraryDependencies += "com.github.rssh" %%% "dotty-cps-async" % dottyCpsAsyncVersion,
   libraryDependencies += "org.scalameta" %%% "munit" % "1.0.0-M10" % Test,
   testFrameworks += new TestFramework("munit.Framework"),
   autoCompilerPlugins := true,
   addCompilerPlugin("com.github.rssh" %% "dotty-cps-async-compiler-plugin" % dottyCpsAsyncVersion)
)


lazy val scalaz  = crossProject(JSPlatform, JVMPlatform)
  .in(file("scalaz"))
  .settings(
    commonSettings,
    name := "cps-async-connect-scalaz",
    libraryDependencies += "org.scalaz" %%% "scalaz-effect" % "7.4.0-M13" ,
    libraryDependencies += "org.scalaz" %%% "scalaz-core" % "7.4.0-M13" 
  ).jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    scalaJSUseMainModuleInitializer := true
  )


lazy val catsEffect  = crossProject(JSPlatform, JVMPlatform)
  .in(file("cats-effect"))
  .settings(
    commonSettings,
    name := "cps-async-connect-cats-effect",
    libraryDependencies += "org.typelevel" %%% "cats-effect" % "3.5.2",
    libraryDependencies += "org.typelevel" %%% "munit-cats-effect" % "2.0.0-M3" % Test
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
                                       "org.typelevel" %%% "munit-cats-effect" % "2.0.0-M3" % Test
                                     ),
                                     scalacOptions += "-Xtarget:21"
                                 )


lazy val monix  = crossProject(JSPlatform, JVMPlatform)
  .in(file("monix"))
  .settings(
    commonSettings,
    name := "cps-async-connect-monix",
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
    name := "cps-async-connect-zio",
    libraryDependencies ++= Seq(
      "dev.zio" %%% "zio" % "1.0.18",
      "dev.zio" %%% "zio-streams" % "1.0.18",
    )
  ).jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    //scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.3.0",
      "io.github.cquiroz" %%% "scala-java-time-tzdb" % "2.3.0"
    ),
  ).jvmSettings(
    scalacOptions ++= Seq( "-unchecked", "-Ydebug-trace", "-Ydebug-names", "-Xprint-types",
                            "-Ydebug", "-uniqid", "-Ycheck:macros",  "-Yprint-syms" )
  )

lazy val zio2  = crossProject(JSPlatform,JVMPlatform) 
  .in(file("zio2"))
  .settings(
    commonSettings,
    name := "cps-async-connect-zio2",
    libraryDependencies ++= Seq(
      "dev.zio" %%% "zio" % "2.0.18",
      "dev.zio" %%% "zio-managed" % "2.0.18",
      "dev.zio" %%% "zio-streams" % "2.0.18",
    )
  ).jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    //scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.4.0-M1",
      "io.github.cquiroz" %%% "scala-java-time-tzdb" % "2.4.0-M1"
    ),
  ).jvmSettings(
    scalacOptions ++= Seq( "-unchecked", "-Ydebug-trace", "-Ydebug-names", "-Xprint-types",
                            "-Ydebug", "-uniqid", "-Ycheck:macros",  "-Yprint-syms" )
  )

lazy val zio2Loom = project.in(file("zio2-loom"))
  .dependsOn(zio2.jvm)
  .settings(
    commonSettings,
    name := "cps-async-connect-zio2-loom",
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
                         name := "cps-async-connect-fs2",
                         libraryDependencies ++= Seq(
                             "co.fs2" %%% "fs2-core" % "3.9.2",
                             "org.typelevel" %%% "munit-cats-effect" % "2.0.0-M3" % Test
                         )
                      )

lazy val streamAkka = (project in file("stream-akka")).
                      settings(
                         commonSettings,
                         name := "cps-async-connect-akka-stream",
                         scalacOptions += "-explain",
                         libraryDependencies ++= Seq(
                            ("com.typesafe.akka" %% "akka-stream" % "2.8.5")
                         )
                      )

lazy val streamPekko = (project in file("stream-pekko")).
  settings(
    commonSettings,
    name := "cps-async-connect-pekko-stream",
    scalacOptions += "-explain",
    libraryDependencies ++= Seq(
      ("org.apache.pekko" %% "pekko-stream" % "1.0.1")
    )
  )


lazy val probabilityMonad = (project in file("probability-monad")).
                             settings(
                               commonSettings,
                               name := "cps-async-connect-probabiliy-monad",
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


