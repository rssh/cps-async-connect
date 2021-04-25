val dottyVersion = "3.0.0-RC3"

ThisBuild/version := "0.4.0"
ThisBuild/organization := "com.github.rssh"

lazy val commonSettings = Seq(
   scalaVersion := dottyVersion,
   libraryDependencies += "com.github.rssh" %%% "dotty-cps-async" % "0.6.1",
   libraryDependencies += "org.scalameta" %%% "munit" % "0.7.25" % Test,
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
    libraryDependencies += "org.typelevel" %%% "cats-effect" % "3.1.0",
    libraryDependencies += "org.typelevel" %%% "munit-cats-effect-3" % "1.0.2" % Test
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
    libraryDependencies += "dev.zio" %%% "zio" % "1.0.7",
  ).jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.0.0",
      "io.github.cquiroz" %%% "scala-java-time-tzdb" % "2.0.0"
    )
  ).jvmSettings(
  )


lazy val root = (project in file("."))
                .aggregate(catsEffect.jvm, catsEffect.js,
                           zio.jvm)     // scalaz have no version for scala-3.0.0-RC3 yet
                .settings(
                   publish := {},
                   publishLocal := {},
                   publishArtifact := false
                )


