name := """TubeLytics"""

version := "1.0-SNAPSHOT"

lazy val jacoco = Seq(jacocoReportSettings in Test := JacocoReportSettings().withTitle("Jacoco Coverage Report").withFormats(JacocoReportFormats.XML))


lazy val root = (project in file(".")).enablePlugins(PlayJava).settings(jacoco *)
enablePlugins(JacocoPlugin)


scalaVersion := "2.13.15"

libraryDependencies ++= Seq(
  guice,
  ws,
  "org.playframework" %% "play-json" % "3.0.4",
  "org.junit.jupiter" % "junit-jupiter-api" % "5.10.2" % Test,  // اضافه کردن junit-jupiter-api فقط برای تست
  "org.junit.jupiter" % "junit-jupiter-engine" % "5.10.2" % Test,  // اضافه کردن junit-jupiter-engine فقط برای تست
  "org.mockito" % "mockito-core" % "5.12.0" % Test  // اضافه کردن mockito-core فقط برای تست
)


Test / testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

Test / test := {
  (Test / test).dependsOn(Test / jacocoReport).value
}

logLevel := Level.Debug

//jacocoReportSettings := JacocoReportSettings().withFormats(JacocoReportFormats.XML, JacocoReportFormats.HTML)

