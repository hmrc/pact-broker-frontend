import sbt.*

object AppDependencies {
  private val bootstrapVer = "7.1.0"
  private val hmrcMongoVer = "1.3.0"

  private val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28" % bootstrapVer,
    "uk.gov.hmrc"       %% "simple-reactivemongo"      % "8.1.0-play-28",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"        % hmrcMongoVer,
    "uk.gov.hmrc"       %% "logback-json-logger"       % "5.2.0",
    "uk.gov.hmrc"       %% "mongo-lock"                % "7.1.0-play-28"
  )

  private val test = Seq(
    "com.vladsch.flexmark" % "flexmark-all"             % "0.36.8",
    "org.jsoup"            % "jsoup"                    % "1.15.3",
    "org.mockito"         %% "mockito-scala-scalatest"  % "1.17.12",
    "uk.gov.hmrc"         %% "service-integration-test" % "1.3.0-play-28"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
