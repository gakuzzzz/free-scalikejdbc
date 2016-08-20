val scalacOpts =
  "-deprecation" ::
  "-unchecked" ::
  "-language:existentials" ::
  "-language:higherKinds" ::
  "-language:implicitConversions" ::
  Nil


val scalaVersion_ = "2.11.8"
val scalikejdbcVersion = "2.3.+"
val scalazVersion = "7.1.7"
val scalatestVersion = "2.2.6"
val scalacheckVersion = "1.12.1"
val h2Version = "1.4.+"
val kindProjectorVersion = "0.8.0"

lazy val core = (project in file("core")).settings(
  name := """free-scalikejdbc""",
  version := "1.0",
  scalaVersion := scalaVersion_,
  resolvers += "bintray/non" at "http://dl.bintray.com/non/maven",
  resolvers += "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases",
  libraryDependencies ++=
    ("org.scalikejdbc"         %% "scalikejdbc"                      % scalikejdbcVersion) ::
    ("org.scalaz"              %% "scalaz-core"                      % scalazVersion) ::
    ("org.scalaz"              %% "scalaz-concurrent"                % scalazVersion) ::
    ("org.scalaz.stream"       %% "scalaz-stream"                    % "0.7.1a") ::
    ("com.h2database"           % "h2"                               % h2Version            % "test") ::
    ("org.scalikejdbc"         %% "scalikejdbc-test"                 % scalikejdbcVersion   % "test") ::
    ("org.scalikejdbc"         %% "scalikejdbc-config"               % scalikejdbcVersion   % "test") ::
    ("org.scalikejdbc"         %% "scalikejdbc-syntax-support-macro" % scalikejdbcVersion   % "test") ::
    ("org.scalatest"           %% "scalatest"                        % scalatestVersion     % "test") ::
    ("org.scalacheck"          %% "scalacheck"                       % scalacheckVersion    % "test") ::
    Nil,
  scalacOptions ++= scalacOpts,
  parallelExecution in Test := false,
  addCompilerPlugin("org.spire-math" %% "kind-projector" % kindProjectorVersion)
)

lazy val sample = (project in file("sample")).settings(
  name := """free-scalikejdbc-sample""",
  version := "1.0",
  scalaVersion := scalaVersion_,
  resolvers ++= ("bintray/non" at "http://dl.bintray.com/non/maven") :: Resolver.sonatypeRepo("releases") :: Nil,
  libraryDependencies ++=
    ("org.scalikejdbc"         %% "scalikejdbc-config"               % scalikejdbcVersion) ::
    ("org.scalikejdbc"         %% "scalikejdbc-syntax-support-macro" % scalikejdbcVersion) ::
    ("com.h2database"           % "h2"                               % h2Version) ::
    ("org.scalatest"           %% "scalatest"                        % scalatestVersion              % "test") ::
    ("org.scalacheck"          %% "scalacheck"                       % scalacheckVersion             % "test") ::
    Nil,
  scalacOptions ++= scalacOpts,
  addCompilerPlugin("org.spire-math" %% "kind-projector" % kindProjectorVersion),
  addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)
).dependsOn(core)

lazy val root = (project in file(".")).aggregate(core, sample)
