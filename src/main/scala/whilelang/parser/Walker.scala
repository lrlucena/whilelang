package whilelang.parser

import scala.util.Try
import org.antlr.v4.runtime.{BaseErrorListener, CharStream, CharStreams, CommonTokenStream, RecognitionException, Recognizer }
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.tree.ParseTreeWalker

object ThrowingErrorListener extends BaseErrorListener:
  override def syntaxError(r: Recognizer[_, _], off: Any, line: Int, col: Int, msg: String, e: RecognitionException) =
    throw new ParseCancellationException(s"line $line:$col $msg")

object Walker:
  def sourceCode(file: String) = Try { io.Source.fromFile(file).getLines().mkString("\n") }

  def walk(source: String)(implicit listener: WhilelangListener) = Try {
    def addListener(r: Recognizer[_, _]) =
      r.removeErrorListeners()
      r.addErrorListener(ThrowingErrorListener)
    val lexer = new WhilelangLexer(CharStreams.fromString(source)) { addListener(this) }
    val parser = new WhilelangParser(new CommonTokenStream(lexer)) { addListener(this) }

    new ParseTreeWalker().walk(listener, parser.program)
  }
