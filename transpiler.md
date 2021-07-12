# Transpiler

Transpiler from Whilelang to Scala.

````shell
$ sbt

# To run the transpiler
sbt> runMain whilelang.compiler.Main sum.while
````

<table>
 <thead><tr><th>Program</th><th>Whilelang</th><th>Scala</th></tr></thead>
<tbody>
<tr>
<td>Hello World</td>
<td>
<pre lang="ruby">
print "Hello World"
</pre>
</td>
<td>
<pre lang="scala">
@main def main() = 
  println("Hello World");
</pre>
</td>
</tr>

<tr>
<td>Sum of numbers</td>
<td>
<pre lang="ruby">
print "Enter the first number:";
a := read;
print "Enter the second number:";
b := read;
sum := a + b;
print "The sum is:";
write sum
</pre>
</td>
<td>
<pre lang="scala">
@main def main() = 
  var a, b, sum = 0;
  println("Enter the first number:");
  a = readInt();
  println("Enter the second number:");
  b = readInt();
  sum = a + b;
  println("The sum is:");
  println(sum);
</pre>
</td>
</tr>

<tr>
<td>Fibonacci Sequence</td>
<td>
<pre lang="ruby">
print "Fibonacci Sequence";
a := 0;
b := 1;
while b <= 1000000 do {
  write b;
  b := a + b;
  a := b - a
}
</pre>
</td>
<td>
<pre lang="scala">
@main def main() = 
  var a, b = 0;
  println("Fibonacci Sequence");
  a = 0;
  b = 1;
  while b <= 1000000 do
    println(b);
    b = a + b;
    a = b - a;
</pre>
</td>
</tr>

</tbody></table>


173 lines of code:

 - Grammar (36 lines)
 - Parser Rules (86 lines)
 - Main (14 lines)


 - Antlr2Scala (14 lines)
 - Walker (23 lines)

 ## Grammar

 ````antlr
 grammar Whilelang;

 program : seqStatement;

 seqStatement: statement (';' statement)* ;

 statement: ID ':=' expression                          # attrib
          | 'skip'                                      # skip
          | 'if' bool 'then' statement 'else' statement # if
          | 'while' bool 'do' statement                 # while
          | 'print' Text                                # print
          | 'write' expression                          # write
          | '{' seqStatement '}'                        # block
          ;

 expression: INT                                        # int
           | 'read'                                     # read
           | ID                                         # id
           | expression '*' expression                  # binOp
           | expression ('+'|'-') expression            # binOp
           | '(' expression ')'                         # expParen
           ;

 bool: ('true'|'false')                                 # boolean
     | expression '=' expression                        # relOp
     | expression '<=' expression                       # relOp
     | 'not' bool                                       # not
     | bool 'and' bool                                  # and
     | '(' bool ')'                                     # boolParen
     ;

 INT: ('0'..'9')+ ;
 ID: ('a'..'z')+;
 Text: '"' .*? '"';

 Space: [ \t\n\r] -> skip;
 ````

## Parser Rules
````scala
package whilelang.compiler

import scala.jdk.CollectionConverters._
import scala.collection.immutable.StringOps
import scala.language.implicitConversions
import whilelang.parser.{ Antlr2Scala, WhilelangBaseListener}
import whilelang.parser.WhilelangParser._

class Compiler extends WhilelangBaseListener with Antlr2Scala[String] {
  var program: String = _
  val ids = collection.mutable.Set[String]()

  override def exitProgram(ctx: ProgramContext) =
    program = s"""@main def main() = {
                 |  ${if (ids.nonEmpty) s"var ${ids.mkString(", ")} = 0" else ""}
                 |  ${ctx.seqStatement.value}
                 |}""".stripMargin

  override def exitSeqStatement(ctx: SeqStatementContext) =
    ctx.value = ctx.statement().asScala
      .map(b => b.value[String]).mkString("\n")
      .replaceAll("\n", "\n  ")

  override def exitAttrib(ctx: AttribContext) =
    val id = ctx.ID.text
    ids += id
    ctx.value = s"$id = ${ctx.expression.value};"

  override def exitSkip(ctx: SkipContext) =
    ctx.value = "()"

  override def exitIf(ctx: IfContext) =
    ctx.value = s"""if (${ctx.bool.value}) {
                   |  ${ctx.statement(0).value}
                   |} else {
                   |  ${ctx.statement(1).value}
                   |}""".stripMargin

  override def exitWhile(ctx: WhileContext) =
    ctx.value = s"""while(${ctx.bool.value}) {
                   |  ${ctx.statement.value}
                   |}""".stripMargin

  override def exitPrint(ctx: PrintContext) =
    ctx.value = s"println(${ctx.Text.text});"

  override def exitWrite(ctx: WriteContext) =
    ctx.value = s"println(${ctx.expression.value});"

  override def exitBlock(ctx: BlockContext) =
    ctx.value = ctx.seqStatement.value

  override def exitRead(ctx: ReadContext) =
    ctx.value = "readInt()"

  override def exitId(ctx: IdContext) =
    ctx.value = s"${ctx.ID.text}"

  override def exitExpParen(ctx: ExpParenContext) =
    ctx.value = ctx.expression.value

  override def exitInt(ctx: IntContext) =
    ctx.value = s"${ctx.text.toInt}"

  override def exitBinOp(ctx: BinOpContext) =
    ctx.value = s"${ctx.expression(0).value} ${ctx(1).text} ${ctx.expression(1).value}"

  override def exitNot(ctx: NotContext) =
    ctx.value = s"!(${ctx.bool.value})"

  override def exitBoolean(ctx: BooleanContext) =
    ctx.value = s"""${ctx.text == "true"}"""

  override def exitAnd(ctx: AndContext) =
    ctx.value = s"(${ctx.bool(0).value} && ${ctx.bool(1).value})"

  override def exitBoolParen(ctx: BoolParenContext) =
    ctx.value = s"(${ctx.bool.value})"

  override def exitRelOp(ctx: RelOpContext) =
    ctx.value = s"${ctx.expression(0).value} ${
      ctx(1).text match
        case "=" => "=="
        case op  => op
    } ${ctx.expression(1).value}"
}
````

## Main

````scala
package whilelang.compiler

import java.io.FileNotFoundException
import scala.util.{ Failure, Success, Try }
import whilelang.parser.Walker

implicit val listener: Compiler = new Compiler()

@main def main(file: String) =
  val sourceCode = Try { io.Source.fromFile(file).getLines().mkString("\n")}
  sourceCode.flatMap(Walker.walk) match
    case Success(_)                        => println(listener.program)
    case Failure(e: FileNotFoundException) => println("File not found")
    case Failure(e)                        => println("Error: " + e.printStackTrace())
````

## Auxiliary Classes

### Walker
````scala
package whilelang.parser

import scala.util.Try
import org.antlr.v4.runtime.{BaseErrorListener, CharStream, CharStreams, CommonTokenStream, RecognitionException, Recognizer }
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.tree.ParseTreeWalker

object ThrowingErrorListener extends BaseErrorListener {
  override def syntaxError(r: Recognizer[_, _], off: Any, line: Int, col: Int, msg: String, e: RecognitionException) =
    throw new ParseCancellationException(s"line $line:$col $msg")
}

object Walker {
  def walk(source: String)(implicit listener: WhilelangListener) = Try {
    val addListener = (r: Recognizer[_, _]) =>
      r.removeErrorListeners()
      r.addErrorListener(ThrowingErrorListener)
    val lexer = new WhilelangLexer(CharStreams.fromString(source)) { addListener(this) }
    val parser = new WhilelangParser(new CommonTokenStream(lexer)) { addListener(this) }

    new ParseTreeWalker().walk(listener, parser.program)
  }
}
````

### Antlr2Scala
````scala
package whilelang.parser

import org.antlr.v4.runtime.tree.{ ParseTree, ParseTreeProperty }

trait Antlr2Scala[T] {
  private[Antlr2Scala] val values = new ParseTreeProperty[T]
  given Conversion[ParseTree, Tree2Scala] = Tree2Scala(_)
  private[Antlr2Scala] case class Tree2Scala(tree: ParseTree) {
    def apply(i: Int) = tree.getChild(i)
    def text = tree.getText
    def value[E]: E = values.get(tree).asInstanceOf[E]
    def value_=(v: T) = values.put(tree, v)
  }
}
````
