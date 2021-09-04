val dottyVersion = "3.0.2"
//val dottyVersion = "3.0.2-RC1-bin-SNAPSHOT"

ThisBuild/version := "0.8.2-SNAPSHOT"
ThisBuild/organization := "com.github.rssh"

lazy val commonSettings = Seq(
   scalaVersion := dottyVersion,
   libraryDependencies += "com.github.rssh" %%% "dotty-cps-async" % "0.9.3-SNAPSHOT",
   libraryDependencies += "org.scalameta" %%% "munit" % "0.7.27" % Test,
   testFrameworks += new TestFramework("munit.Framework")
)


lazy val scalaz  = crossProject(JSPlatform, JVMPlatform)
  .in(file("scalaz"))
  .settings(
    commonSettings,
    name := "cps-async-connect-scalaz",
    libraryDependencies += "org.scalaz" %%% "scalaz-effect" % "7.4.0-M7" ,
    libraryDependencies += "org.scalaz" %%% "scalaz-core" % "7.4.0-M7" 
  ).jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    scalaJSUseMainModuleInitializer := true
  ).jvmSettings(
  )


lazy val catsEffect  = crossProject(JSPlatform, JVMPlatform)
  .in(file("cats-effect"))
  .settings(
    commonSettings,
    name := "cps-async-connect-cats-effect",
    libraryDependencies += "org.typelevel" %%% "cats-effect" % "3.2.1",
    libraryDependencies += "org.typelevel" %%% "munit-cats-effect-3" % "1.0.5" % Test
  ).jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    scalaJSUseMainModuleInitializer := true
  ).jvmSettings(
  )


lazy val monix  = crossProject(JSPlatform, JVMPlatform)
  .in(file("monix"))
  .settings(
    commonSettings,
    name := "cps-async-connect-monix",
    libraryDependencies += "io.monix" %%% "monix" % "3.4.0",
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
      "dev.zio" %%% "zio" % "1.0.10",
      "dev.zio" %%% "zio-streams" % "1.0.10",
    )
  ).jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    //scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.3.0",
      "io.github.cquiroz" %%% "scala-java-time-tzdb" % "2.3.0"
    )
  ).jvmSettings(
    scalacOptions ++= Seq( "-unchecked", "-Ydebug-trace", "-Ydebug-names", "-Xprint-types",
                            "-Ydebug", "-uniqid", "-Ycheck:macros",  "-Yprint-syms" )
  )

lazy val streamFs2 = crossProject(JSPlatform, JVMPlatform)
                     .in(file("stream-fs2"))
                     .dependsOn(catsEffect)
                     .settings(
                         commonSettings,
                         name := "cps-async-connect-fs2",
                         libraryDependencies ++= Seq(
                             "co.fs2" %%% "fs2-core" % "3.1.0",
                             "org.typelevel" %%% "munit-cats-effect-3" % "1.0.5" % Test
                         )
                      )

lazy val streamAkka = (project in file("stream-akka")).
                      settings(
                         commonSettings,
                         name := "cps-async-connect-akka-stream",
                         libraryDependencies ++= Seq(
                            ("com.typesafe.akka" %% "akka-stream" % "2.6.16").cross(CrossVersion.for3Use2_13)
                         )
                      )


lazy val root = (project in file("."))
                .aggregate( catsEffect.jvm, catsEffect.js,
                           monix.jvm, monix.js,
                           scalaz.jvm, scalaz.js , 
                           zio.jvm,  zio.js,
                           streamFs2.jvm, streamFs2.js,
                           streamAkka
                )
                .settings(
                   publish := {},
                   publishLocal := {},
                   publishArtifact := false
                )


