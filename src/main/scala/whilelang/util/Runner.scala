package whilelang.util

import whilelang.parser.MyListener as Listener
import whilelang.parser.Statement.Program

import java.io.FileNotFoundException
import scala.util.{Failure, Success, Try}

object Runner:
  def apply(action: Program => Unit)(file: String): Unit =
    for source <- sourceCode(file) yield
      Walker(Listener())(source) match
        case Success(program) => action(program)
        case Failure(_: FileNotFoundException) => println("File not found")
        case Failure(e) => println("Error: " + e.printStackTrace())

  def sourceCode(file: String): Try[String] = Try:
    io.Source.fromFile(file).getLines.mkString("\n")
