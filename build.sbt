ThisBuild / scalaVersion := "3.3.0-RC3"
ThisBuild / organization := "com.github.lrlucena"
ThisBuild / version := "1.3.0"

enablePlugins(Antlr4Plugin)

Compile / packageBin / mainClass := Some("whilelang.interpreter.main")

Compile / scalacOptions ++= Seq("-deprecation","-explain")

assembly / mainClass :=  Some("whilelang.interpreter.main")

lazy val hello = (project in file(".")).settings(name := "WhileLang" )

libraryDependencies ++= Seq(
     "org.antlr" % "antlr4" % "4.12.0"
)

Antlr4 / antlr4Version:= "4.12.0"
Antlr4 / antlr4PackageName := Some("whilelang.parser")
Antlr4 / antlr4GenListener := true
Antlr4 / antlr4GenVisitor := false
