import sbt.*
import sbt.Keys.parallelExecution
import scoverage.ScoverageKeys

object ScoverageSettings {
  def apply(): Seq[Def.Setting[?]] = Seq( // Semicolon-separated list of regexes matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*(config|views.*);.*(AuthService|BuildInfo|Routes).*",
    ScoverageKeys.coverageExcludedFiles := Seq(
      "<empty>",
      "Reverse.*",
      ".*connectors.*",
      ".*BuildInfo.*",
      ".*javascript.*",
      ".*repositories.*",
      ".*Routes.*",
      ".*GuiceInjector",
      ".*DateTimeQueryStringBinder.*", // better covered via wiremock/E2E integration tests
      ".*Test.*"
    ).mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}
