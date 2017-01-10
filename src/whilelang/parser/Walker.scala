package whilelang.parser

import scala.util.Try
import org.antlr.v4.runtime.{ ANTLRInputStream, BaseErrorListener, CommonTokenStream, RecognitionException, Recognizer }
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.tree.ParseTreeWalker

object ThrowingErrorListener extends BaseErrorListener {
  override def syntaxError(r: Recognizer[_, _], off: Any, line: Int, col: Int, msg: String, e: RecognitionException) =
    throw new ParseCancellationException(s"line $line:$col $msg")
}

object Walker {
  def walk(source: String)(implicit listener: WhilelangListener) = Try {
    val lexer = new WhilelangLexer(new ANTLRInputStream(source)) {
      removeErrorListeners()
      addErrorListener(ThrowingErrorListener)
    }
    val parser = new WhilelangParser(new CommonTokenStream(lexer)) {
      removeErrorListeners()
      addErrorListener(ThrowingErrorListener)
    }
    new ParseTreeWalker().walk(listener, parser.program)
  }
}
