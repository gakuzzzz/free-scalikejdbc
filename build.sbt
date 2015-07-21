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
  resolvers += "bintray/non" at "http://dl.bintray.com/non/maven",
  resolvers += "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases",
  libraryDependencies ++=
    ("org.scalikejdbc"         %% "scalikejdbc"                      % scalikejdbcVersion) ::
    ("org.scalaz"              %% "scalaz-core"                      % "7.1.3") ::
    ("org.scalaz"              %% "scalaz-concurrent"                % "7.1.3") ::
    ("org.scalaz.stream"       %% "scalaz-stream"                    % "0.7.1a") ::
    ("com.h2database"           % "h2"                               % "1.4.+"              % "test") ::
    ("org.scalikejdbc"         %% "scalikejdbc-test"                 % scalikejdbcVersion   % "test") ::
    ("org.scalikejdbc"         %% "scalikejdbc-config"               % scalikejdbcVersion   % "test") ::
    ("org.scalikejdbc"         %% "scalikejdbc-syntax-support-macro" % scalikejdbcVersion   % "test") ::
    ("org.scalatest"           %% "scalatest"                        % "2.2.2"              % "test") ::
    ("org.scalacheck"          %% "scalacheck"                       % "1.12.1"             % "test") ::
    Nil,
  scalacOptions ++= scalacOpts,
  parallelExecution in Test := false,
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.5.2")
)

lazy val sample = (project in file("sample")).settings(
  name := """free-scalikejdbc-sample""",
  version := "1.0",
  scalaVersion := "2.11.6",
  resolvers ++= ("bintray/non" at "http://dl.bintray.com/non/maven") :: Resolver.sonatypeRepo("releases") :: Nil,
  libraryDependencies ++=
    ("org.scalikejdbc"         %% "scalikejdbc-config"               % scalikejdbcVersion) ::
    ("org.scalikejdbc"         %% "scalikejdbc-syntax-support-macro" % scalikejdbcVersion) ::
    ("com.h2database"           % "h2"                               % "1.4.+") ::
    ("org.scalatest"           %% "scalatest"                        % "2.2.2"              % "test") ::
    ("org.scalacheck"          %% "scalacheck"                       % "1.12.1"             % "test") ::
    Nil,
  scalacOptions ++= scalacOpts,
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.5.2"),
  addCompilerPlugin("org.scalamacros" %% "paradise" % "2.0.1" cross CrossVersion.full)
).dependsOn(core)

lazy val root = (project in file(".")).aggregate(core, sample)
