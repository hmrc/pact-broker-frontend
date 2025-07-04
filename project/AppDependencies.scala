import sbt.*

object AppDependencies {
  private val bootstrapVer = "9.13.0"
  private val hmrcMongoVer = "2.6.0"

  private val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVer,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVer
  )

  private val test = Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-30" % bootstrapVer,
    "org.scalatestplus"   %% "scalacheck-1-17"        % "3.2.18.0",
    "com.vladsch.flexmark" % "flexmark-all"           % "0.64.8",
    "org.scalatestplus"   %% "mockito-4-11"           % "3.2.18.0"
    ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
