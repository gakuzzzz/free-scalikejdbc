val scalacOpts = (
  "-deprecation" ::
  "-unchecked" ::
  "-language:existentials" ::
  "-language:higherKinds" ::
  "-language:implicitConversions" ::
  Nil
)

val scalikejdbcVersion = "2.2.+"

lazy val core = (project in file("core")).settings(
  name := """free-scalikejdbc""",
  version := "1.0",
  scalaVersion := "2.11.5",
  libraryDependencies ++= (
    ("org.scalikejdbc"         %% "scalikejdbc"                      % scalikejdbcVersion) ::
    ("org.scalaz"              %% "scalaz-core"                      % "7.1.1") ::
    ("org.scalatest"           %% "scalatest"                        % "2.1.6"              % "test") ::
    Nil
  ),
  scalacOptions ++= scalacOpts
)

lazy val sample = (project in file("sample")).settings(
  name := """free-scalikejdbc-sample""",
  version := "1.0",
  scalaVersion := "2.11.5",
  libraryDependencies ++= (
    ("org.scalikejdbc"         %% "scalikejdbc-config"               % scalikejdbcVersion) ::
    ("org.scalikejdbc"         %% "scalikejdbc-syntax-support-macro" % scalikejdbcVersion) ::
    ("com.h2database"           % "h2"                               % "1.4.+") ::
    ("org.scalatest"           %% "scalatest"                        % "2.1.6"               % "test") ::
    Nil
  ),
  scalacOptions ++= scalacOpts
).dependsOn(core)

lazy val root = (project in file(".")).aggregate(core, sample)
