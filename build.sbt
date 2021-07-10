ThisBuild / scalaVersion := "3.0.0"
ThisBuild / organization := "com.github.lrlucena"
ThisBuild / version := "1.0"

enablePlugins(Antlr4Plugin)

Compile / packageBin / mainClass := Some("whilelang.interpreter.main")

Compile / scalacOptions ++= Seq("-deprecation")

assembly / mainClass :=  Some("whilelang.interpreter.main")

lazy val hello = (project in file(".")).settings(name := "WhileLang" )

libraryDependencies ++= Seq(
     "org.antlr" % "antlr4" % "4.9.2"
)

Antlr4 / antlr4Version:= "4.9.2"
Antlr4 / antlr4PackageName := Some("whilelang.parser")
Antlr4 / antlr4GenListener := true
Antlr4 / antlr4GenVisitor := false
