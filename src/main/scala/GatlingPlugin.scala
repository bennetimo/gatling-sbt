import com.excilys.ebi.gatling.recorder.config.RecorderOptions
import com.excilys.ebi.gatling.recorder.controller.RecorderController
import sbt._
import Keys._
import sbt.Tests.{SubProcess, Group}

object GatlingPlugin extends Plugin {

  //REPOs
  val gatlingReleases = "Excilys" at "http://repository.excilys.com/content/groups/public"

  //DEPENDENCIES
  val gatlingVersion = "1.4.7"
  val gatlingApp = "com.excilys.ebi.gatling" % "gatling-app" % gatlingVersion
  val gatlingRecorder = "com.excilys.ebi.gatling" % "gatling-recorder" % gatlingVersion
  val gatlingParent = "com.excilys.ebi.gatling" % "gatling-parent" % gatlingVersion
  val gatlingCharts = "com.excilys.ebi.gatling" % "gatling-charts" % gatlingVersion
  val gatlingHighcharts = "com.excilys.ebi.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion

  // LoadTest configuration to hold all gatling sources, under src/lt/scala
  val LoadTest = config("lt") extend (Runtime)

  lazy val runRecorder = TaskKey[Unit]("gatling-recorder", "Start the Gatling Recorder utility")
  lazy val gatlingConfFile = SettingKey[File]("gatling-conf-file", "The Gatling-Tool configuration file") in LoadTest

  def runRecorderTask = runRecorder <<= (sourceDirectory) map {
    (sourceDirectory:File) =>
      println("Starting Gatling Recorder...")
      RecorderController(new RecorderOptions(
        outputFolder = Some(sourceDirectory.getPath + "/scala/scenarios"),
        simulationClassName = Some("RecordedSimulation"),
        simulationPackage = Some("com.example.simulation"),
        requestBodiesFolder = Some("")))
  }

  val gatlingSettings = inConfig(LoadTest)(baseGatlingSettings)

  val gatlingTestFramework = new TestFramework("net.tbennett.gatling.sbt.plugin.GatlingFramework")

  lazy val baseGatlingSettings = Defaults.testSettings ++ Seq(
    resolvers ++= Seq(gatlingReleases),
    libraryDependencies ++= gatlingDependencies ++ frameworkDependencies,
    testFrameworks := Seq(gatlingTestFramework),
    parallelExecution in LoadTest := false, //Doesn't make sense to launch multiple load tests simultaneously
    fork in LoadTest := true,
    scalaVersion in LoadTest := "2.9.2",
    testGrouping <<= definedTests in LoadTest map singleTests,

    runRecorderTask,
    gatlingConfFile <<= baseDirectory { _ / "src" / "lt" / "resources" / "galing.conf" }
    //gatlingResultDir <<= target { _ / "gatling-test" / "result" },
    //logLevel := Level.Debug
  )

  // Group tests to be just a single test, and run each in a forked jvm.
  // This gets round the fact that the Gatling runner shutsdown the ActorSystem after the test is finished
  def singleTests(tests: Seq[TestDefinition]) =
    tests map {
      test =>
        new Group(
          name = test.name,
          tests = Seq(test),
          runPolicy = SubProcess(javaOptions = Seq.empty[String]))
    }

  val gatlingDependencies = Seq(
    gatlingApp,
    gatlingRecorder,
    gatlingCharts,
    gatlingHighcharts
  )

  val frameworkDependencies = Seq(
    "net.tbennett" %% "gatling-sbt-test-framework" % "0.0.1-SNAPSHOT" % "lt"
  )
}
