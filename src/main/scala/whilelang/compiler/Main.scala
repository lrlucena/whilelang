package whilelang.compiler

import java.io.FileNotFoundException
import scala.util.{ Failure, Success, Try }
import whilelang.parser.Walker.{sourceCode, walk}

implicit val listener: Compiler = new Compiler()

@main def main(file: String) = sourceCode(file).flatMap(walk) match
  case Success(_)                        => println(listener.program)
  case Failure(e: FileNotFoundException) => println("File not found")
  case Failure(e)                        => println("Error: " + e.printStackTrace())
