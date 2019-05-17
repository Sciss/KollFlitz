lazy val baseName  = "KollFlitz"
lazy val baseNameL = baseName.toLowerCase

lazy val projectVersion = "0.2.3"
lazy val mimaVersion    = "0.2.1"

name               := baseName
version            := projectVersion
organization       := "de.sciss"
scalaVersion       := "2.12.8"
crossScalaVersions := Seq("2.12.8", "2.11.12", "2.13.0-RC2")
description        := "Useful methods for Scala collections which I tend to copy and paste once a week"
homepage           := Some(url(s"https://git.iem.at/sciss/${name.value}"))
licenses           := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt"))

mimaPreviousArtifacts := Set("de.sciss" %% baseNameL % mimaVersion)

libraryDependencies += {
  val v = "3.0.8-RC2"
  if (scalaVersion.value == "2.13.0-RC2") {
    "org.scalatest" % "scalatest_2.13.0-RC1" % v % Test
  } else {
    "org.scalatest" %% "scalatest" % v % Test
  }
}

initialCommands in console :=
  """import de.sciss.kollflitz._
    |import Ops._
    |import RandomOps._""".stripMargin

scalacOptions in (Compile, compile) ++= Seq("-deprecation", "-unchecked", "-feature", "-Xfuture", "-encoding", "utf8", "-Xlint")

// ---- publishing ----

publishMavenStyle := true

publishTo :=
  Some(if (isSnapshot.value)
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  )

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := { val n = name.value
<scm>
  <url>git@git.iem.at:sciss/{n}.git</url>
  <connection>scm:git:git@git.iem.at:sciss/{n}.git</connection>
</scm>
<developers>
  <developer>
    <id>sciss</id>
    <name>Hanns Holger Rutz</name>
    <url>http://www.sciss.de</url>
  </developer>
</developers>
}
