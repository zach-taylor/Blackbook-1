import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "Blackbook"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "postgresql" % "postgresql" %"9.1-901.jdbc4",
    filters
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    scalacOptions ++= Seq("-feature")
  )

}
