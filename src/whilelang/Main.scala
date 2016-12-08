package whilelang

import org.antlr.v4.runtime.{ ANTLRInputStream, CommonTokenStream }
import org.antlr.v4.runtime.tree.ParseTreeWalker
import scala.io.Source
import scala.util.{ Try, Success, Failure }

object Main extends App {
  def parse(source: String) = {
    val input = new ANTLRInputStream(source)
    val lexer = new WhilelangLexer(input)
    val tokens = new CommonTokenStream(lexer)
    val parser = new WhilelangParser(tokens)
    val tree = parser.program;
    val walker = new ParseTreeWalker()
    val listener = new MyListener()
    walker.walk(listener, tree)
    listener.program
  }

  if (args.length > 0) {
    val filename = args(0)
    val sourceCode = Try(Source.fromFile(filename).getLines.mkString("\n"))
    sourceCode match {
      case Success(code) => parse(code).execute
      case Failure(_)    => println("File not found")
    }
  } else {
    println("Please include the filename.")
  }
}
