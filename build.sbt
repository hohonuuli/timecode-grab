lazy val buildSettings = Seq(
  organization := "org.mbari.videolab",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.1",
  crossScalaVersions := Seq("2.12.1"),
  test in assembly := {}
)

lazy val consoleSettings = Seq(
  shellPrompt := { state =>
    val user = System.getProperty("user.name")
    user + "@" + Project.extract(state).currentRef.project + ":sbt> "
  },
  initialCommands in console :=
    """
      |import java.time.Instant
      |import java.util.UUID
    """.stripMargin
)

lazy val dependencySettings = Seq(
  libraryDependencies ++= {
    val slf4jVersion = "1.7.22"
    val logbackVersion = "1.1.8"
    Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "ch.qos.logback" % "logback-core" % logbackVersion,
      "com.typesafe" % "config" % "1.3.1",
      "junit" % "junit" % "4.12" % "test",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.slf4j" % "log4j-over-slf4j" % slf4jVersion,
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "scilube" %% "scilube-core" % "2.0.4")
  },
  resolvers ++= Seq(
    Resolver.mavenLocal,
    Resolver.sonatypeRepo("releases"),
    "hohonuuli-bintray" at "http://dl.bintray.com/hohonuuli/maven")
)

lazy val gitHeadCommitSha =
  SettingKey[String]("git-head", "Determines the current git commit SHA")

lazy val makeVersionProperties =
  TaskKey[Seq[File]]("make-version-props", "Makes a version.properties file")

lazy val makeVersionSettings = Seq(
  gitHeadCommitSha := scala.util.Try(Process("git rev-parse HEAD").lines.head).getOrElse(""),
  makeVersionProperties := {
    val propFile = (resourceManaged in Compile).value / "version.properties"
    val content = "version=%s" format (gitHeadCommitSha.value)
    IO.write(propFile, content)
    Seq(propFile)
  },
  resourceGenerators in Compile <+= makeVersionProperties
)

lazy val optionSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8", // yes, this is 2 args
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture"),
  javacOptions ++= Seq("-target", "1.8", "-source", "1.8"),
  incOptions := incOptions.value.withNameHashing(true),
  updateOptions := updateOptions.value.withCachedResolution(true)
)

// --- Aliases
addCommandAlias("cleanall", ";clean;clean-files")

// --- Modules
lazy val appSettings = buildSettings ++ consoleSettings ++ dependencySettings ++
    optionSettings

lazy val root = (project in file("."))
  .settings(appSettings)
  .settings(
    name := "timecode-grab",
    todosTags := Set("TODO", "FIXME", "WTF"),
    fork := true,
    libraryDependencies ++= {
      Seq(
        "org.mbari.vcr4j" % "vcr4j-jssc" % "3.0.1",
        "com.offbytwo" % "docopt" % "0.6.0.20150202"
      )
    },
    mainClass in assembly := Some("Main")
  )

