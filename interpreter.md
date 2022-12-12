# Interpreter

````shell
$ sbt

# To run the interpreter
sbt> runMain whilelang.interpreter.main sum.while
````

221 lines of code:

 - Grammar (36 lines)
 - Parser Rules (73 lines)
 - Abstract Syntax (28 lines)
 - Semantics (33 lines)
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
package whilelang.parser

import scala.jdk.CollectionConverters._
import scala.language.implicitConversions
import whilelang.parser.WhilelangBaseListener
import whilelang.parser.WhilelangParser._
import whilelang.util.Antlr2Scala
import Statement._
import Expression._
import Bool._

class MyListener extends WhilelangBaseListener with Antlr2Scala[Any]:
  var program: Program = _

  override def exitProgram(ctx: ProgramContext) =
    ctx.value = Program(ctx.seqStatement.value)
    program = ctx.value

  override def exitSeqStatement(ctx: SeqStatementContext) =
    ctx.value = SeqStatement(ctx.statement.asScala.toList.map { _.value[Statement] })

  override def exitAttrib(ctx: AttribContext) =
    ctx.value = Attrib(ctx.ID.text, ctx.expression.value)

  override def exitSkip(ctx: SkipContext) =
    ctx.value = Skip

  override def exitIf(ctx: IfContext) =
    ctx.value = If(ctx.bool.value, ctx.statement(0).value, ctx.statement(1).value)

  override def exitWhile(ctx: WhileContext) =
    ctx.value = While(ctx.bool.value, ctx.statement.value)

  override def exitPrint(ctx: PrintContext) =
    ctx.value = Print(ctx.Text.text.drop(1).dropRight(1))

  override def exitWrite(ctx: WriteContext) =
    ctx.value = Write(ctx.expression.value)

  override def exitBlock(ctx: BlockContext) =
    ctx.value = ctx.seqStatement.value

  override def exitRead(ctx: ReadContext) =
    ctx.value = Read

  override def exitId(ctx: IdContext) =
    ctx.value = Id(ctx.ID.text)

  override def exitExpParen(ctx: ExpParenContext) =
    ctx.value = ctx.expression.value

  override def exitInt(ctx: IntContext) =
    ctx.value = Integer(ctx.text.toInt)

  override def exitBinOp(ctx: BinOpContext) =
    val lhs: Expression = ctx.expression(0).value
    val rhs: Expression = ctx.expression(1).value
    ctx.value = ctx(1).text match
      case "*"     => ExpMult(lhs, rhs)
      case "-"     => ExpSub(lhs, rhs)
      case "+" | _ => ExpSum(lhs, rhs)

  override def exitNot(ctx: NotContext) =
    ctx.value = Not(ctx.bool.value)

  override def exitBoolean(ctx: BooleanContext) =
    ctx.value = Boole(ctx.text == "true")

  override def exitAnd(ctx: AndContext) =
    ctx.value = And(ctx.bool(0).value, ctx.bool(1).value)

  override def exitBoolParen(ctx: BoolParenContext) =
    ctx.value = ctx.bool.value

  override def exitRelOp(ctx: RelOpContext) =
    val lhs: Expression = ctx.expression(0).value
    val rhs: Expression = ctx.expression(1).value
    ctx.value = ctx(1).text match
      case "="      => ExpEq(lhs, rhs)
      case "<=" | _ => ExpLe(lhs, rhs)
````

## Abstract Syntax
````scala
package whilelang.parser

enum Statement:
  case Skip
  case If(condition: Bool, thenSmt: Statement, elseSmt: Statement)
  case Write(exp: Expression)
  case While(condition: Bool, doSmt: Statement)
  case Print(text: String)
  case SeqStatement(statements: List[Statement])
  case Attrib(id: String, exp: Expression)
  case Program(statements: SeqStatement)

enum Expression:
  case Read
  case Id(id: String)
  case Integer(exp: Int)
  case ExpSum(lhs: Expression, rhs: Expression)
  case ExpSub(lhs: Expression, rhs: Expression)
  case ExpMult(lhs: Expression, rhs: Expression)

enum Bool:
  case Boole(b: Boolean)
  case ExpEq(lhs: Expression, rhs: Expression)
  case ExpLe(lhs: Expression, rhs: Expression)
  case Not(b: Bool)
  case And(lhs: Bool, rhs: Bool)
````

## Semantics
````scala
package whilelang.interpreter
import Language._

object Semantics {
  val memory = scala.collection.mutable.Map[String, Int]()

  def execute(stmt: Statement): Unit = stmt match
    case If(cond, thenSmt, elseSmt) => if (cond.value) thenSmt.execute() else elseSmt.execute()
    case Write(exp)                 => println(exp.value)
    case While(cond, doSmt)         => while (cond.value) { doSmt.execute() }
    case Print(text)                => println(text)
    case SeqStatement(stmts)        => stmts.foreach { _.execute() }
    case Attrib(id, exp)            => memory += id -> exp.value
    case Program(seq)               => seq.execute()
    case Skip | _                   =>

  def value(exp: Expression): Int = exp match
    case Read              => io.StdIn.readInt()
    case Id(id)            => memory.getOrElseUpdate(id, 0)
    case Integer(value)    => value
    case ExpSum(lhs, rhs)  => lhs.value + rhs.value
    case ExpSub(lhs, rhs)  => lhs.value - rhs.value
    case ExpMult(lhs, rhs) => lhs.value * rhs.value
    case null | _          => 0

  def value(a: Bool): Boolean = a match
    case Boole(b)                     => b
    case ExpEqual(lhs, rhs)           => lhs.value == rhs.value
    case ExpLessOrEqualThan(lhs, rhs) => lhs.value <= rhs.value
    case Not(b)                       => !b.value
    case And(lhs, rhs)                => lhs.value && rhs.value
    case null | _                     => true
}
````


## Auxiliary Classes

### Walker
````scala
package whilelang.util

import scala.util.Try
import org.antlr.v4.runtime.{BaseErrorListener, CharStream, CharStreams, CommonTokenStream, RecognitionException, Recognizer }
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.tree.ParseTreeWalker
import whilelang.parser._
import whilelang.interpreter._

object ThrowingErrorListener extends BaseErrorListener:
  override def syntaxError(r: Recognizer[_, _], off: Any, line: Int, col: Int, msg: String, e: RecognitionException) =
    throw ParseCancellationException(s"line $line:$col $msg")

object Walker:
  def sourceCode(file: String): Try[String] = Try {
    io.Source.fromFile(file).getLines().mkString("\n")
  }

  def addListener(r: Recognizer[_, _]*) = r.map( r =>
    r.removeErrorListeners()
    r.addErrorListener(ThrowingErrorListener))

  def walk(source: String)(implicit listener: MyListener): Try[Statement.Program] = Try {
    val lexer = WhilelangLexer(CharStreams.fromString(source))
    val parser = WhilelangParser(CommonTokenStream(lexer))
    addListener(lexer, parser)
    ParseTreeWalker().walk(listener, parser.program)
    listener.program
  }
````

### Antlr2Scala
````scala
package whilelang.util

import org.antlr.v4.runtime.tree.{ParseTree, ParseTreeProperty}

trait Antlr2Scala[T]:
  private val values = ParseTreeProperty[T]

  given Conversion[ParseTree, Tree2Scala] = Tree2Scala(_)
  private[Antlr2Scala] case class Tree2Scala(tree: ParseTree):
    def apply(i: Int) = tree.getChild(i)
    def text = tree.getText
    def value[E]: E = values.get(tree).asInstanceOf[E]
    def value_=(v: T) = values.put(tree, v)
````
