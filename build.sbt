import play.sbt.PlayImport.PlayKeys.playDefaultPort
import sbt.Keys.unmanagedResourceDirectories
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.ServiceManagerPlugin.Keys.itDependenciesList
import uk.gov.hmrc.ServiceManagerPlugin.serviceManagerSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.ExternalService

lazy val appName: String = "pact-broker-frontend"

lazy val externalServices = List(
  ExternalService("DATASTREAM")
)

lazy val root = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion                     := 0,
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    Test / unmanagedResourceDirectories += (Test / baseDirectory).value / "test-resources",
    Test / unmanagedJars += (Test / baseDirectory).value / "test-resources" / "pacts" / "pact-file.jar"
  )

  .settings(scalaVersion := "2.12.16")
  .settings(playDefaultPort := 9866)
  .settings(publishingSettings: _*)
  .settings(ScoverageSettings())
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(serviceManagerSettings: _*)
  .settings(itDependenciesList := externalServices)
  .settings(resolvers += Resolver.jcenterRepo)
