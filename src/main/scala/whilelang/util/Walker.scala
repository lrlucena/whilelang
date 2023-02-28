package whilelang.util

import scala.util.Try
import org.antlr.v4.runtime.{BaseErrorListener, CharStream, CharStreams, CommonTokenStream, RecognitionException, Recognizer }
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.tree.ParseTreeWalker
import whilelang.parser.{WhilelangParser, WhilelangLexer, MyListener}

object ThrowingErrorListener extends BaseErrorListener:
  override def syntaxError(r: Recognizer[?, ?], off: Any, line: Int, col: Int, msg: String, e: RecognitionException) =
    throw ParseCancellationException(s"line $line:$col $msg")

object Walker:
  def sourceCode(file: String): Try[String] = Try:
    io.Source.fromFile(file).getLines().mkString("\n")

  private def addListener(r: Recognizer[?, ?]*): Unit = r.map : r =>
    r.removeErrorListeners()
    r.addErrorListener(ThrowingErrorListener)

  def walk(source: String)(using listener: MyListener) = Try:
    val lexer = WhilelangLexer(CharStreams.fromString(source))
    val parser = WhilelangParser(CommonTokenStream(lexer))
    addListener(lexer, parser)
    ParseTreeWalker().walk(listener, parser.program)
    listener.program
