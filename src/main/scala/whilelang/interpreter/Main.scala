package whilelang.interpreter

import java.io.FileNotFoundException
import scala.util.{ Failure, Success, Try }
import whilelang.parser.Walker.{sourceCode, walk}

implicit val listener: MyListener = new MyListener()

@main def main(file: String) = sourceCode(file).flatMap(walk) match
  case Success(_)                        => listener.program.execute()
  case Failure(e: FileNotFoundException) => println("File not found")
  case Failure(e)                        => println("Error: " + e.getLocalizedMessage)
