import sbt.Keys._
import com.typesafe.sbt.packager.docker._

name := """Task"""

version := "0.1.0"

val zioVersion = "1.0.0-RC21"

scalaVersion := "2.13.1"

resolvers ++= Seq(
  "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  ("Bintray sbt plugin releases" at "http://dl.bintray.com/sbt/sbt-plugin-releases/").withAllowInsecureProtocol(true),
  Resolver.mavenLocal
)

// Dependencies
libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-streams" % zioVersion,
  "dev.zio" %% "zio-logging" % "0.3.2",
  "dev.zio" %% "zio-logging-slf4j" % "0.3.2",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.7",
  "org.apache.logging.log4j" % "log4j-core" % "2.7",
  "dev.zio" %% "zio-test"     % zioVersion % Test,
  "dev.zio" %% "zio-test-sbt" % zioVersion % Test
)

testFrameworks ++= Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

assemblyJarName in assembly := "task.jar"

mainClass in assembly := Some("task.EntryPoint")

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

dockerBaseImage := "openjdk:11-jre-slim"

packageName in Docker := packageName.value
version in Docker := version.value

daemonUser in Docker := "nobody"

dockerPermissionStrategy in Docker := DockerPermissionStrategy.CopyChown

