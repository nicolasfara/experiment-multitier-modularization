import scala.scalanative.build.*

val scala3Version = "3.6.2"

ThisBuild / scalaVersion := scala3Version
ThisBuild / organization := "it.nicolasfarabegoli"
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / homepage := Some(
  url(
    "https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects"
  )
)
ThisBuild / licenses := List(
  "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")
)
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / developers := List(
  Developer(
    "nicolasfara",
    "Nicolas Farabegoli",
    "nicolas.farabegoli@gmail.com",
    url("https://nicolasfarabegoli.it")
  )
)
ThisBuild / libraryDependencies ++= Seq(
  "ch.epfl.lamp" %%% "gears" % "0.2.0",
  "org.typelevel" %%% "cats-core" % "2.12.0",
  "org.scalatest" %%% "scalatest" % "3.2.19" % Test
)
ThisBuild / coverageEnabled := true
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / scalacOptions ++= Seq(
  "-Werror",
  "-rewrite",
  "-indent",
  "-unchecked",
  "-explain",
  "-experimental",
  "-Xcheck-macros",
  "-Yretain-trees",
  "-Xprint:all"
)

lazy val root = project //crossProject(JSPlatform, JVMPlatform, NativePlatform)
//  .crossType(CrossType.Pure)
//  .in(file("."))
//  .nativeSettings(
//    nativeConfig ~= {
//      _.withLTO(LTO.default)
//        .withMode(Mode.releaseSize)
//        .withGC(GC.immix)
//    }
//  )
//  .jsSettings(
//    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.8.0",
//    scalaJSUseMainModuleInitializer := true,
//    scalaJSLinkerConfig ~= { _.withOptimizer(true) }
//  )
  .settings(
    name := "root",
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
    sonatypeProfileName := "it.nicolasfarabegoli",
    libraryDependencies ++= Seq()
  )
