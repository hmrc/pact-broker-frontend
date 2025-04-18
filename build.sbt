import play.sbt.PlayImport.PlayKeys.playDefaultPort
import sbt.{Compile, Test}
import uk.gov.hmrc.DefaultBuildSettings

lazy val appName: String = "pact-broker-frontend"
ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "3.3.4"

lazy val root = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(ScoverageSettings())
  .settings(
    scalacOptions ++= Seq(
      // The following compiler options are commented out as some features are still unsupported in Scala 3.3.3. We should be able to uncomment with Scala 3.3.4.
//      "-Xfatal-warnings"
//      "-Wconf:src=routes/.*:s"
    ),
    libraryDependencies ++= AppDependencies(),
    Test / parallelExecution := false,
    Test / fork := false,
    playDefaultPort := 9866,
    resolvers += Resolver.jcenterRepo,
    retrieveManaged := true,
    scalafmtOnCompile := true,
    Compile / packageDoc / publishArtifact := false,
    Compile / doc / sources := Seq.empty
  )
  .disablePlugins(JUnitXmlReportPlugin)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(root % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(
    DefaultBuildSettings.itSettings(),
    Test / fork := true,
    Test / unmanagedResourceDirectories += ((root / baseDirectory).value / "test-resources"),
    Test / unmanagedJars += ((root / baseDirectory).value / "test-resources" / "pacts" / "pact-file.jar")
  )
