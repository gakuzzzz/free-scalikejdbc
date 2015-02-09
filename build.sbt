name := """free-scalikejdbc"""
version := "1.0"
scalaVersion := "2.11.5"

scalacOptions ++= (
  "-deprecation" ::
  "-unchecked" ::
  "-language:existentials" ::
  "-language:higherKinds" ::
  "-language:implicitConversions" ::
  Nil
)

libraryDependencies ++=
  ("org.scalikejdbc"         %% "scalikejdbc"                      % "2.2.+") ::
  ("org.scalikejdbc"         %% "scalikejdbc-config"               % "2.2.+") ::
  ("org.scalikejdbc"         %% "scalikejdbc-syntax-support-macro" % "2.2.+") ::
  ("org.scalaz"              %% "scalaz-core"                      % "7.1.1") ::
  ("com.h2database"           % "h2"                               % "1.4.+") ::
  ("org.scalatest"           %% "scalatest"                        % "2.1.6"        % "test") ::
  Nil


