val dottyVersion = "3.0.0-RC2"
//val dottyVersion = "0.28.0-bin-20200907-101e620-NIGHTLY"
//val dottyVersion = dottyLatestNightlyBuild.get

ThisBuild/version := "0.3.0-SNAPSHOT"
ThisBuild/organization := "com.github.rssh"

lazy val commonSettings = Seq(
   scalaVersion := dottyVersion,
   libraryDependencies += "com.github.rssh" %%% "dotty-cps-async" % "0.5.0",
   libraryDependencies += "org.scalameta" %%% "munit" % "0.7.23" % Test,
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
    libraryDependencies += "org.typelevel" %%% "cats-effect" % "3.0.1",
    libraryDependencies += "org.typelevel" %%% "munit-cats-effect-3" % "1.0.1" % Test
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
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    libraryDependencies += "dev.zio" %%% "zio" % "0.0.0+1-4732439f-SNAPSHOT",
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
                .aggregate(scalaz.jvm, scalaz.js, catsEffect.jvm, catsEffect.js,
                           zio.jvm)
                .settings(
                   publish := {},
                   publishLocal := {},
                   publishArtifact := false
                )


