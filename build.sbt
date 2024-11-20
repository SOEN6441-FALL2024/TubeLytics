name := """TubeLytics"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.15"

val PekkoVersion = "1.1.2"

libraryDependencies ++= Seq(
  guice,
  ws,
  "org.playframework" %% "play-json" % "3.0.4",
  "org.junit.jupiter" % "junit-jupiter-api" % "5.10.2" % Test,
  "org.junit.jupiter" % "junit-jupiter-engine" % "5.10.2" % Test,
  "org.mockito" % "mockito-core" % "5.12.0" % Test,
  "org.apache.pekko" %% "pekko-actor" % PekkoVersion,
  "org.apache.pekko" %% "pekko-actor-typed" % PekkoVersion,
  "org.apache.pekko" %% "pekko-protobuf-v3" % PekkoVersion,
  "org.apache.pekko" %% "pekko-slf4j" % PekkoVersion,
  "org.apache.pekko" %% "pekko-serialization-jackson" % PekkoVersion,
  "org.apache.pekko" %% "pekko-stream" % PekkoVersion,
  "org.apache.pekko" %% "pekko-actor-testkit-typed" % PekkoVersion % Test
 // "com.typesafe.play" %% "play-test" % playVersion % Test
)

Test / testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

//Test / test := {
//  (Test / test).dependsOn(Test / jacocoReport).value
//}


logLevel := Level.Debug


jacocoReportSettings := JacocoReportSettings(
  "Jacoco Coverage Report",
  None,
  JacocoThresholds(),
  Seq(JacocoReportFormats.HTML, JacocoReportFormats.XML), // note XML formatter
  "utf-8")