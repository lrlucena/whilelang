package whilelang.util

import java.io.FileNotFoundException
import scala.util.{Failure, Success}
import whilelang.parser.MyListener as Listener
import whilelang.parser.Statement.Program
import Walker.{sourceCode, walk}

given Listener = Listener()

object Runner:
  def apply(action: Program => Unit)(file: String) = sourceCode(file).flatMap(walk) match
    case Success(program)                  => action(program)
    case Failure(e: FileNotFoundException) => println("File not found")
    case Failure(e)                        => println("Error: " + e.printStackTrace())
