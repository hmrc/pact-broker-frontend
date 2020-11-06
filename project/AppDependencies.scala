import play.core.PlayVersion
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "simple-reactivemongo"           % "7.30.0-play-27",
    "uk.gov.hmrc"       %% "logback-json-logger"            % "4.8.0",
    "uk.gov.hmrc"       %% "govuk-template"                 % "5.58.0-play-27",
    "uk.gov.hmrc"       %% "play-health"                    % "3.15.0-play-27",
    "uk.gov.hmrc"       %% "play-ui"                        % "8.13.0-play-27",
    "uk.gov.hmrc"       %% "bootstrap-backend-play-27"      % "3.0.0"
  )

  val test: Seq[ModuleID] = Seq(
    "com.typesafe.play"           %% "play-test"                % PlayVersion.current       % "test, it",
    "org.scalatest"               %% "scalatest"                % "3.0.8"                   % "test, it",
    "org.scalatestplus.play"      %% "scalatestplus-play"       % "3.1.2"                   % "test, it",
    "org.pegdown"                 %  "pegdown"                  % "1.6.0"                   % "test, it",
    "org.jsoup"                   %  "jsoup"                    % "1.13.1"                  % "test, it",
    "org.mockito"                 %  "mockito-core"             % "3.2.4"                   % "test, it",
    "uk.gov.hmrc"                 %% "service-integration-test" % "0.10.0-play-27"          % "test, it"
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
