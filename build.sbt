import play.sbt.PlayImport.PlayKeys.playDefaultPort
import sbt.Keys.unmanagedResourceDirectories
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings

lazy val appName: String = "pact-broker-frontend"
ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always // it should not be needed but the build still fails without it

lazy val root = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .configs(IntegrationTest)
  .settings(ScoverageSettings())
  .settings(integrationTestSettings() *)
  .settings(
    libraryDependencies ++= AppDependencies(),
    IntegrationTest / unmanagedResourceDirectories += (Test / baseDirectory).value / "test-resources",
    IntegrationTest / unmanagedJars += (Test / baseDirectory).value / "test-resources" / "pacts" / "pact-file.jar",
    playDefaultPort := 9866,
    resolvers += Resolver.jcenterRepo,
    scalafmtOnCompile := true
  )
