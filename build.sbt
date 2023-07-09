import com.typesafe.config._

val conf = ConfigFactory.parseFile(new File("project/local.properties")).resolve()

ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.8"

// Need to have local.properties with setting for local.repo, which is a repo that includes Mulligan exported with PublishLocal
val localRepo = conf.getString("local.repo")
resolvers += Resolver.file("localtrix", file(localRepo))(Resolver.ivyStylePatterns)
libraryDependencies += "com.github.rs17" %% "mulligan" % "0.1"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.9"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"

lazy val root = (project in file("."))
  .settings(
    name := "IBKRReportHelper"
  )