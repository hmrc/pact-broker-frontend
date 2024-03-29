resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always // it should not be needed but the build still fails without it

addSbtPlugin("uk.gov.hmrc"       % "sbt-auto-build"     % "3.19.0")
addSbtPlugin("uk.gov.hmrc"       % "sbt-distributables" % "2.4.0")
addSbtPlugin("com.typesafe.play" % "sbt-plugin"         % "2.8.21")
addSbtPlugin("org.scoverage"     % "sbt-scoverage"      % "2.0.9")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"       % "2.4.6")
