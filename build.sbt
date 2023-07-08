ThisBuild / version := "0.1"

ThisBuild / scalaVersion := "2.13.8"
resolvers += Resolver.file("localtrix", file("/home/ravi/.ivy2/local"))(Resolver.ivyStylePatterns)
libraryDependencies += "com.github.rs17" %% "mulligan" % "0.1"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.9"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"

lazy val root = (project in file("."))
  .settings(
    name := "IBKRReportHelper"
  )
