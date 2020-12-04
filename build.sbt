lazy val baseName  = "KollFlitz"
lazy val baseNameL = baseName.toLowerCase

lazy val projectVersion = "0.2.4"
lazy val mimaVersion    = "0.2.1"

lazy val commonJvmSettings = Seq(
  crossScalaVersions := Seq("3.0.0-M2", "2.13.4", "2.12.12"),
)

// sonatype plugin requires that these are in global
ThisBuild / version      := projectVersion
ThisBuild / organization := "de.sciss"

lazy val root = crossProject(JVMPlatform, JSPlatform).in(file("."))
  .jvmSettings(commonJvmSettings)
  .settings(
    name               := baseName,
//    version            := projectVersion,
//    organization       := "de.sciss",
    scalaVersion       := "2.13.4",
    description        := "Useful methods for Scala collections which I tend to copy and paste once a week",
    homepage           := Some(url(s"https://git.iem.at/sciss/${name.value}")),
    licenses           := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt")),
    mimaPreviousArtifacts := Set("de.sciss" %% baseNameL % mimaVersion),
    libraryDependencies += {
      "org.scalatest" %%% "scalatest" % "3.2.3" % Test
    },
    initialCommands in console := {
      """import de.sciss.kollflitz._
        |import Ops._
        |import RandomOps._""".stripMargin
    },
    scalacOptions in (Compile, compile) ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xlint", "-Xsource:2.13"),
    unmanagedSourceDirectories in Compile ++= {
      val sourceDirPl = (sourceDirectory in Compile).value
      val sourceDirSh = file(
        sourceDirPl.getPath.replace("/jvm/" , "/shared/").replace("/js/", "/shared/")
      )
      val sv = CrossVersion.partialVersion(scalaVersion.value)
      val sub = sv match {
        case Some((2, n)) if n >= 13 => "scala-2.13+"
        case Some((3, _))            => "scala-2.13+"
        case _                       => "scala-2.13-"
      }
      Seq(sourceDirPl / sub, sourceDirSh / sub)
    },
  )
  .settings(publishSettings)

// ---- publishing ----
lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  developers := List(
    Developer(
      id    = "sciss",
      name  = "Hanns Holger Rutz",
      email = "contact@sciss.de",
      url   = url("https://www.sciss.de")
    )
  ),
  scmInfo := {
    val h = "git.iem.at"
    val a = s"sciss/${name.value}"
    Some(ScmInfo(url(s"https://$h/$a"), s"scm:git@$h:$a.git"))
  },
)

