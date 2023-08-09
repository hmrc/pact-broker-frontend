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
      majorVersion                     := 0,
      libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
      Test / unmanagedResourceDirectories += (Test / baseDirectory).value / "test-resources",
      Test / unmanagedJars += (Test / baseDirectory).value / "test-resources" / "pacts" / "pact-file.jar",
      scalaVersion := "2.12.16",
      playDefaultPort := 9866,
      resolvers += Resolver.jcenterRepo
  )
