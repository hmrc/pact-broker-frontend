import play.sbt.PlayImport.PlayKeys.playDefaultPort
import sbt.Keys.unmanagedResourceDirectories
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings

lazy val appName: String = "pact-broker-frontend"

lazy val root = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .configs(IntegrationTest)
  .settings(ScoverageSettings())
  .settings(integrationTestSettings() *)
  .settings(
    majorVersion := 0,
    libraryDependencies ++= AppDependencies(),
    IntegrationTest / unmanagedResourceDirectories += (Test / baseDirectory).value / "test-resources",
    IntegrationTest / unmanagedJars += (Test / baseDirectory).value / "test-resources" / "pacts" / "pact-file.jar",
    scalaVersion := "2.13.8",
    playDefaultPort := 9866,
    resolvers += Resolver.jcenterRepo,
    scalafmtOnCompile := true
  )
