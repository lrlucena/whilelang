package whilelang.util

import java.io.FileNotFoundException
import scala.util.{ Failure, Success, Try }
import whilelang.parser.MyListener
import whilelang.parser.Statement.Program
import Walker.{sourceCode, walk}

object Runner:
  given MyListener = MyListener()

  def run(action: Program => Unit)(file: String) = sourceCode(file).flatMap(walk) match
    case Success(program)                  => action(program)
    case Failure(e: FileNotFoundException) => println("File not found")
    case Failure(e)                        => println("Error: " + e.printStackTrace())
