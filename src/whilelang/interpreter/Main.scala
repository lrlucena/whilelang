package whilelang.interpreter

import java.io.FileNotFoundException
import scala.util.{ Failure, Success, Try }
import whilelang.parser.Walker

object Main extends App {
  implicit val listener = new MyListener()
  Try(io.Source.fromFile(args(0)).getLines.mkString("\n")).flatMap(Walker.walk) match {
    case Success(_)                        => listener.program.execute
    case Failure(e: FileNotFoundException) => println("File not found")
    case Failure(e)                        => println("Error: " + e.getLocalizedMessage)
  }
}
