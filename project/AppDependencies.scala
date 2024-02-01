import sbt.*

object AppDependencies {
  private val bootstrapVer = "8.4.0"
  private val hmrcMongoVer = "1.7.0"

  private val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28" % bootstrapVer,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"        % hmrcMongoVer
  )

  private val test = Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-28"  % bootstrapVer,
    "org.scalatestplus"   %% "scalacheck-1-17"         % "3.2.17.0",
    "com.vladsch.flexmark" % "flexmark-all"            % "0.64.8",
    "org.mockito"         %% "mockito-scala-scalatest" % "1.17.30"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
