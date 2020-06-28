package whilelang.compiler

import java.io.FileNotFoundException
import scala.util.{ Failure, Success, Try }
import whilelang.parser.Walker.walk

object Main extends App {
  implicit val listener = new Compiler()
  Try(io.Source.fromFile(args(0)).getLines().mkString("\n")).flatMap(walk) match {
    case Success(_)                        => println(listener.program)
    case Failure(e: FileNotFoundException) => println("File not found")
    case Failure(e)                        => println("Error: " + e.printStackTrace())
  }
}
