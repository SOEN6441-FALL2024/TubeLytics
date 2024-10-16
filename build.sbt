name := "TubeLytics"

version := "1.0"

scalaVersion := "2.13.8" // or your project's Scala version

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.8.15",
  "com.typesafe.play" %% "play-json" % "2.9.2",
  "com.typesafe.play" %% "play-test" % "2.8.15" % Test
)

enablePlugins(PlayScala)
