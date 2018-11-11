lazy val junitInterface = "com.novocode" % "junit-interface" % "0.11" % "test"
lazy val jFreeChart = "org.jfree" % "jfreechart" % "1.5.0"

lazy val driftPlots = project
  .in(file("."))
  .settings(name    := "drift-plots",
            version := "0.0.0",
            organization := "ru.ifmo",
            libraryDependencies ++= Seq(junitInterface, jFreeChart),
            autoScalaLibrary := false,
            crossPaths := false)
