import play.core.PlayVersion
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "simple-reactivemongo"           % "7.23.0-play-26",
    "uk.gov.hmrc"       %% "logback-json-logger"            % "3.1.0",
    "uk.gov.hmrc"       %% "govuk-template"                 % "5.36.0-play-26",
    "uk.gov.hmrc"       %% "play-health"                    % "3.14.0-play-26",
    "uk.gov.hmrc"       %% "play-ui"                        % "8.2.0-play-26",
    "uk.gov.hmrc"       %% "bootstrap-play-26"              % "1.3.0"
  )

  val test: Seq[ModuleID] = Seq(
    "com.typesafe.play"           %% "play-test"                % PlayVersion.current       % "test, it",
    "org.scalatest"               %% "scalatest"                % "3.0.8"                   % "test, it",
    "org.scalatestplus.play"      %% "scalatestplus-play"       % "3.1.2"                   % "test, it",
    "org.pegdown"                 %  "pegdown"                  % "1.6.0"                   % "test, it",
    "org.jsoup"                   %  "jsoup"                    % "1.10.3"                  % "test, it",
    "org.mockito"                 %  "mockito-core"             % "3.2.0"                   % "test, it",
    "uk.gov.hmrc"                 %% "service-integration-test" % "0.9.0-play-26"           % "test, it"
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
