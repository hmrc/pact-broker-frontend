import play.core.PlayVersion
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "simple-reactivemongo"           % "8.1.0-play-28",
    "uk.gov.hmrc"       %% "logback-json-logger"            % "5.2.0",
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28"      % "7.1.0",
    "uk.gov.hmrc"       %% "mongo-lock"                     % "7.1.0-play-28",
  )

  val test: Seq[ModuleID] = Seq(
    "com.typesafe.play"           %% "play-test"                % PlayVersion.current       % "test, it",
    "org.scalatest"               %% "scalatest"                % "3.1.4"                   % "test, it",
    "org.scalatestplus.play"      %% "scalatestplus-play"       % "5.1.0"                   % "test, it",
    "com.vladsch.flexmark"        % "flexmark-all"              % "0.36.8"                  % "test, it",
    "org.jsoup"                   %  "jsoup"                    % "1.15.3"                  % "test, it",
    "org.mockito"                 %% "mockito-scala-scalatest"  % "1.17.12"                % "test, it",
    "uk.gov.hmrc"                 %% "service-integration-test" % "1.3.0-play-28"          % "test, it"
  )




  def apply(): Seq[ModuleID] = compile ++ test
}
