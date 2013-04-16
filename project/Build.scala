import sbt._
import Keys._

object GatlingSbtBuild extends Build
{
  val buildVersion = "0.0.1-SNAPSHOT"

  lazy val gatlingProject = Project("gatling-sbt", file("."), settings = gatlingSettings)

  //REPOs
  //val gatlingSnapshots = "Gatling Snapshot Repo" at "https://repository-gatling.forge.cloudbees.com/snapshot/"
  val gatlingReleases = "Excilys" at "http://repository.excilys.com/content/groups/public"

  //DEPENDENCIES
  val gatlingVersion = "1.4.7"
  val gatlingApp = "com.excilys.ebi.gatling" % "gatling-app" % gatlingVersion
  val gatlingRecorder = "com.excilys.ebi.gatling" % "gatling-recorder" % gatlingVersion
  val gatlingParent = "com.excilys.ebi.gatling" % "gatling-parent" % gatlingVersion
  val gatlingCharts = "com.excilys.ebi.gatling" % "gatling-charts" % gatlingVersion
  val gatlingHighcharts = "com.excilys.ebi.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion

  lazy val gatlingSettings = Defaults.defaultSettings ++ Seq(
    sbtPlugin := true,
    name := "gatling-sbt",
    version := buildVersion,
    scalaVersion := "2.9.2",
    organization := "net.tbennett.gatling",
    resolvers ++= Seq(gatlingReleases),
    libraryDependencies ++= gatlingDependencies,
    publishMavenStyle := true,
    publishTo := Some(Resolver.mavenLocal)
  )

  lazy val gatlingDependencies = Seq(
    gatlingApp,
    gatlingRecorder,
    gatlingParent,
    gatlingCharts,
    gatlingHighcharts
  )
}