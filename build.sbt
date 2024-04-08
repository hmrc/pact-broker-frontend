import play.sbt.PlayImport.PlayKeys.playDefaultPort
import sbt.{Compile, Test}
import uk.gov.hmrc.DefaultBuildSettings

lazy val appName: String = "pact-broker-frontend"
ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "2.13.12"

lazy val root = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(ScoverageSettings())
  .settings(
    scalacOptions ++= Seq(
      "-Werror",
      "-Wconf:src=routes/.*:s"
    ),
    Test / unmanagedResourceDirectories += (Test / baseDirectory).value / "test-resources",
    Test / unmanagedJars += (Test / baseDirectory).value / "test-resources" / "pacts" / "pact-file.jar",
    libraryDependencies ++= AppDependencies(),
    Test / parallelExecution := false,
    Test / fork := false,
    playDefaultPort := 9866,
    resolvers += Resolver.jcenterRepo,
    retrieveManaged := true,
    scalafmtOnCompile := true,
    Compile / packageDoc / publishArtifact  := false,
    Compile / doc / sources := Seq.empty
  )
  .disablePlugins(JUnitXmlReportPlugin)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(root % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(
      DefaultBuildSettings.itSettings()
  )