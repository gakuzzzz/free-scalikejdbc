val scalacOpts =
  "-deprecation" ::
  "-unchecked" ::
  "-language:existentials" ::
  "-language:higherKinds" ::
  "-language:implicitConversions" ::
  Nil


val scalikejdbcVersion = "2.2.+"

lazy val core = (project in file("core")).settings(
  name := """free-scalikejdbc""",
  version := "1.0",
  scalaVersion := "2.11.6",
  libraryDependencies ++=
    ("org.scalikejdbc"         %% "scalikejdbc"                      % scalikejdbcVersion) ::
    ("org.scalaz"              %% "scalaz-core"                      % "7.1.1") ::
    ("com.h2database"           % "h2"                               % "1.4.+"              % "test") ::
    ("org.scalikejdbc"         %% "scalikejdbc-test"                 % scalikejdbcVersion   % "test") ::
    ("org.scalikejdbc"         %% "scalikejdbc-config"               % scalikejdbcVersion   % "test") ::
    ("org.scalikejdbc"         %% "scalikejdbc-syntax-support-macro" % scalikejdbcVersion   % "test") ::
    ("org.scalatest"           %% "scalatest"                        % "2.2.2"              % "test") ::
    ("org.scalacheck"          %% "scalacheck"                       % "1.12.1"             % "test") ::
    Nil,
  scalacOptions ++= scalacOpts,
  parallelExecution in Test := false
)

lazy val sample = (project in file("sample")).settings(
  name := """free-scalikejdbc-sample""",
  version := "1.0",
  scalaVersion := "2.11.5",
  libraryDependencies ++=
    ("org.scalikejdbc"         %% "scalikejdbc-config"               % scalikejdbcVersion) ::
    ("org.scalikejdbc"         %% "scalikejdbc-syntax-support-macro" % scalikejdbcVersion) ::
    ("com.h2database"           % "h2"                               % "1.4.+") ::
    ("org.scalatest"           %% "scalatest"                        % "2.2.2"              % "test") ::
    ("org.scalacheck"          %% "scalacheck"                       % "1.12.1"             % "test") ::
    Nil,
  scalacOptions ++= scalacOpts
).dependsOn(core)

lazy val root = (project in file(".")).aggregate(core, sample)
