import scala.scalanative.build.*

val scala3Version = "3.6.1"

ThisBuild / scalaVersion := scala3Version
ThisBuild / organization := "it.nicolasfarabegoli"
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / homepage := Some(url("https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects"))
ThisBuild / licenses := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / developers := List(
  Developer(
    "nicolasfara",
    "Nicolas Farabegoli",
    "nicolas.farabegoli@gmail.com",
    url("https://nicolasfarabegoli.it")
  )
)

lazy val root = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("."))
  .configs()
    .nativeSettings(
      nativeConfig ~= {
        _.withLTO(LTO.default)
          .withMode(Mode.releaseSize)
          .withGC(GC.immix)
      }
    )
    .jsSettings(
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.8.0",
      scalaJSUseMainModuleInitializer := true,
      scalaJSLinkerConfig ~= { _.withOptimizer(true) }
    )
  .settings(
    name := "Template-for-Scala-Multiplatform-Projects",
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
    sonatypeProfileName := "it.nicolasfarabegoli",
    scalacOptions ++= Seq(
      "-Werror",
      "-rewrite",
      "-indent",
      "-unchecked",
      "-explain",
    ),
    libraryDependencies ++= Seq()
  )

lazy val check = taskKey[Unit]("Runs all verification tasks like tests, linters, etc.")
check := {
  (root.jvm / Test / test).value
  (root.jvm / Compile / scalafmtCheck).value

  (root.js / Test / test).value
  (root.js / Compile / scalafmtCheck).value

  (root.native / Test / test).value
  (root.native / Compile / scalafmtCheck).value
}

compile := (Compile / compile dependsOn check).value
