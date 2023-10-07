package whilelang.util

import whilelang.parser.MyListener as Listener
import whilelang.parser.Statement.Program
import whilelang.util.Walker.{sourceCode, walk}

import java.io.FileNotFoundException
import scala.util.{Failure, Success}

given Listener = Listener()

object Runner:
  def apply(action: Program => Unit)(file: String): Unit = sourceCode(file).flatMap(walk) match
    case Success(program) => action(program)
    case Failure(_: FileNotFoundException) => println("File not found")
    case Failure(e) => println("Error: " + e.printStackTrace())
