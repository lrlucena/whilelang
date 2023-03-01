# Interpreter

````shell
$ sbt

# To run the interpreter
sbt> runMain whilelang.interpreter.main sum.while
````

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

import scala.collection.mutable.Map
import whilelang.parser._
import whilelang.parser.Statement._
import whilelang.parser.Expression._
import whilelang.parser.Bool._

type Environment = Map[String, Int]
given Environment = Map[String, Int]()

extension (stmt: Statement)(using env: Environment)
  def execute: Unit = stmt match
    case If(cond, tSmt, eSmt) => (if cond.value then tSmt else eSmt).execute
    case Write(exp)           => println(exp.value)
    case While(cond, doSmt)   => while cond.value do doSmt.execute
    case Print(text)          => println(text)
    case SeqStatement(stmts)  => stmts.foreach(_.execute)
    case Attrib(id, exp)      => env += id -> exp.value
    case Program(seq)         => seq.execute
    case Skip | _             =>

extension(exp: Expression)(using env: Environment)
  def value: Int = exp match
    case Read                 => io.StdIn.readInt()
    case Id(id)               => env.getOrElseUpdate(id, 0)
    case Integer(value)       => value
    case ExpSum(lhs, rhs)     => lhs.value + rhs.value
    case ExpSub(lhs, rhs)     => lhs.value - rhs.value
    case ExpMult(lhs, rhs)    => lhs.value * rhs.value
    case null | _             => 0

extension(exp: Bool)(using env: Environment)
  def value: Boolean = exp match
    case Boole(b)             => b
    case ExpEq(lhs, rhs)      => lhs.value == rhs.value
    case ExpLe(lhs, rhs)      => lhs.value <= rhs.value
    case Not(b)               => !b.value
    case And(lhs, rhs)        => lhs.value && rhs.value
    case null | _             => true
````


## Parser Rules
````scala
package whilelang.parser

import scala.jdk.CollectionConverters._
import whilelang.parser.WhilelangBaseListener
import whilelang.parser.WhilelangParser.*
import Statement.*
import Expression.*
import Bool.*

class MyListener extends WhilelangBaseListener with ContextValue:
  var program: Program = _

  override def exitProgram(ctx: ProgramContext) =
    ctx.value = Program(ctx.seqStatement.value)
    program = ctx.value

  override def exitSeqStatement(ctx: SeqStatementContext) =
    ctx.value = SeqStatement(ctx.statement.asScala.toList.map(_.value[Statement]))

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

## Main
````scala
package whilelang.interpreter

import whilelang.util.Runner

def action = Runner(program => program.execute)

@main def main(file: String) = action(file)
````

## Auxiliary Classes

### Runner
````scala
package whilelang.util

import java.io.FileNotFoundException
import scala.util.{Failure, Success}
import whilelang.parser.MyListener
import whilelang.parser.Statement.Program
import Walker.{sourceCode, walk}

given MyListener = MyListener()

object Runner:
  def apply(action: Program => Unit)(file: String) = sourceCode(file).flatMap(walk) match
    case Success(program)                  => action(program)
    case Failure(e: FileNotFoundException) => println("File not found")
    case Failure(e)                        => println("Error: " + e.printStackTrace())
````

### Walker
````scala
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
````

### ContectValue

````scala
package whilelang.util

import org.antlr.v4.runtime.tree.{ParseTree, ParseTreeProperty}

trait ContextValue:
  given ParseTreeProperty[Any] = ParseTreeProperty[Any]()

  extension (tree: ParseTree)(using values: ParseTreeProperty[Any])
    def apply(i: Int) = tree.getChild(i)
    def text = tree.getText
    def value[E]: E = values.get(tree).asInstanceOf[E]
    def value_=(v: Any) = values.put(tree, v)
````