name := """TubeLytics"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.15"

libraryDependencies ++= Seq(
  guice,
  ws,
  "org.playframework" %% "play-json" % "3.0.4",
  "org.junit.jupiter" % "junit-jupiter-api" % "5.10.2" % Test,
  "org.junit.jupiter" % "junit-jupiter-engine" % "5.10.2" % Test,
  "org.mockito" % "mockito-core" % "5.12.0" % Test,
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