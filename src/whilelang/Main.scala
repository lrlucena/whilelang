package whilelang

import org.antlr.v4.runtime.{ ANTLRInputStream, CommonTokenStream }
import org.antlr.v4.runtime.tree.ParseTreeWalker
import scala.io.Source
import scala.util.{ Try, Success, Failure }

object Main extends App {
  def parse(source: String) = {
    val parser = new WhilelangParser(new CommonTokenStream(new WhilelangLexer(new ANTLRInputStream(source))))
    val walker = new ParseTreeWalker()
    val listener = new MyListener()
    walker.walk(listener, parser.program)
    listener.program
  }

  val sourceCode = Try(Source.fromFile(args(0)).getLines.mkString("\n"))
  sourceCode match {
    case Success(code) => parse(code).execute
    case Failure(_)    => println("File not found")
  }
}
