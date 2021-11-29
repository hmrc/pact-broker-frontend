import play.core.PlayVersion
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "simple-reactivemongo"           % "8.0.0-play-28",
    "uk.gov.hmrc"       %% "logback-json-logger"            % "5.1.0",
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28"      % "5.16.0",
    "uk.gov.hmrc"       %% "mongo-lock"                   % "7.0.0-play-28",
  )

  val test: Seq[ModuleID] = Seq(
    "com.typesafe.play"           %% "play-test"                % PlayVersion.current       % "test, it",
    "org.scalatest"               %% "scalatest"                % "3.0.8"                   % "test, it",
    "org.scalatestplus.play"      %% "scalatestplus-play"       % "3.1.2"                   % "test, it",
    "com.vladsch.flexmark"        % "flexmark-all"              % "0.36.8",
    "org.jsoup"                   %  "jsoup"                    % "1.13.1"                  % "test, it",
    "org.mockito"                 %% "mockito-scala-scalatest"  % "1.16.37"                % "test, it",
    "uk.gov.hmrc"                 %% "service-integration-test" % "1.2.0-play-28"          % "test, it"
  )




  def apply(): Seq[ModuleID] = compile ++ test
}
