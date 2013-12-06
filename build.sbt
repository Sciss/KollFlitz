name          := "KollFlitz"

version       := "0.1.0"

organization  := "de.sciss"

scalaVersion  := "2.10.3"

description   := "Useful methods for Scala collections which I tend to copy and paste once a week"

homepage      := Some(url("https://github.com/Sciss/" + name.value))

licenses      := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt"))

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0" % "test"

initialCommands in console :=
  """import de.sciss.kollflitz._
    |import RandomOps._""".stripMargin

retrieveManaged := true

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

// ---- publishing ----

publishMavenStyle := true

publishTo :=
  Some(if (version.value endsWith "-SNAPSHOT")
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  )

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := { val n = name.value
<scm>
  <url>git@github.com:Sciss/{n}.git</url>
  <connection>scm:git:git@github.com:Sciss/{n}.git</connection>
</scm>
<developers>
  <developer>
    <id>sciss</id>
    <name>Hanns Holger Rutz</name>
    <url>http://www.sciss.de</url>
  </developer>
</developers>
}

// ---- ls.implicit.ly ----

// seq(lsSettings :_*)

// (LsKeys.tags   in LsKeys.lsync) := Seq("swing", "gui")

// (LsKeys.ghUser in LsKeys.lsync) := Some("Sciss")

// (LsKeys.ghRepo in LsKeys.lsync) := Some(name.value)

