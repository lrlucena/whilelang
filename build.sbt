ThisBuild / scalaVersion := "2.13.1"
ThisBuild / organization := "com.github.lrlucena"
ThisBuild / version := "1.0"

enablePlugins(Antlr4Plugin)

mainClass in (Compile, packageBin) := Some("whilelang.interpreter.Main")

scalacOptions in Compile ++= Seq("-deprecation")

lazy val hello = (project in file(".")).settings(name := "WhileLang" )

libraryDependencies ++= Seq(
     "org.antlr" % "antlr4" % "4.8-1",
     "org.scala-lang" % "scala-library" % "2.13.1" ,
     "org.scala-lang" % "scala-reflect" % "2.13.1"
)

antlr4Version in Antlr4 := "4.8-1"
antlr4PackageName in Antlr4 := Some("whilelang.parser")
antlr4GenListener in Antlr4 := true
antlr4GenVisitor in Antlr4 := false
