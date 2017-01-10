# While language

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/b1705795c5f74b9289b6f4c942dd5911)](https://www.codacy.com/app/leonardo-lucena/whilelang?utm_source=github.com&utm_medium=referral&utm_content=lrlucena/whilelang&utm_campaign=badger)

> A small programming language created with ANTLR and Scala. 

Two implementations:

  - Interpreter
  - Translator to Scala

<table>
  <thead>
    <tr>
      <td></td>
      <th >Interpreter</th>
      <th >Translator</th>
    </tr>
    </thead>
    <tbody>
    <tr>
      <th>Grammar</th>
      <td colspan="2" align="center"><a href="#grammar">Grammar</a> (32 lines)</td>
    </tr>
    <tr>
      <th>Parser Rules</th>
      <td><a href="#grammar">Listener</a> (74 lines)<br><a href="#grammar">Abstract Syntax ans Semantics</a> (63 lines)</td>
      <td><a href="#grammar">Compiler</a> (87 lines)</td>
    </tr>
    <tr>
      <th>Main</th>
      <td><a href="#grammar">Main</a> (14 lines)</td>
      <td><a href="#grammar">Main</a> (14 lines)</td>
    </tr>
    <tr>
      <th>Utility Classes</th>
      <td colspan="2" align="center"><a href="#antlr2scala">Antr2Scala</a> (13 lines)<br> <a href="#walker">Walker</a> (25 lines)</td>
    </tr>
    <tr>
      <th>Total</th>
      <td>221 lines</td>
      <td>171 lines</td>
    </tr>
  </tbody>
</table>

## Grammar

```antlr
grammar Whilelang;

program : seqStatement;

seqStatement: statement (';' statement)* ;

statement: ID ':=' expression                          # attrib
         | 'skip'                                      # skip
         | 'if' bool 'then' statement 'else' statement # if
         | 'while' bool 'do' statement                 # while
         | 'print' Text                                # print
         | 'write' expression                          # write
         | '{' seqStatement '}'                        # block ;

expression: INT                                        # int
          | 'read'                                     # read
          | ID                                         # id
          | expression '*' expression                  # binOp
          | expression ('+'|'-') expression            # binOp
          | '(' expression ')'                         # expParen ;

bool: ('true'|'false')                                 # boolean
    | expression '=' expression                        # relOp
    | expression '<=' expression                       # relOp
    | 'not' bool                                       # not
    | bool 'and' bool                                  # and
    | '(' bool ')'                                     # boolParen ;

INT: ('0'..'9')+ ;
ID: ('a'..'z')+;
Text: '"' .*? '"';
Space: [ \t\n\r] -> skip;
```
---

## Interpreter

### Listener

````scala
package whilelang

import whilelang.{ WhilelangParser => C }
import whilelang.Language2._
import scala.collection.JavaConverters._

class MyListener extends WhilelangBaseListener with Antlr2Scala {
  var _program: Statement = _
  def program = _program

  override def exitProgram(ctx: C.ProgramContext) =
    _program = Program(ctx.seqStatement.value[List[Statement]])

  override def exitSeqStatement(ctx: C.SeqStatementContext) =
    ctx.value = ctx.statement().asScala.toList.map { _.value[Statement] }

  override def exitAttrib(ctx: C.AttribContext) =
    ctx.value = Attrib(ctx.ID.text, ctx.expression.value)

  override def exitSkip(ctx: C.SkipContext) =
    ctx.value = Skip

  override def exitIf(ctx: C.IfContext) =
    ctx.value = If(ctx.bool.value, ctx.statement(0).value, ctx.statement(1).value)

  override def exitWhile(ctx: C.WhileContext) =
    ctx.value = While(ctx.bool.value, ctx.statement.value)

  override def exitPrint(ctx: C.PrintContext) =
    ctx.value = Print(ctx.Text.text.drop(1).dropRight(1))

  override def exitWrite(ctx: C.WriteContext) =
package whilelang.interpreter

import scala.collection.JavaConverters.asScalaBufferConverter
import whilelang.interpreter.Language._
import whilelang.parser.{ Antlr2Scala, WhilelangBaseListener, WhilelangParser => C }

class MyListener extends WhilelangBaseListener with Antlr2Scala[Any] {
  var _program: Program = _
  def program = _program

  override def exitProgram(ctx: C.ProgramContext) =
    _program = Program(ctx.seqStatement.value)

  override def exitSeqStatement(ctx: C.SeqStatementContext) =
    ctx.value = SeqStatement(ctx.statement().asScala.toList.map { _.value[Statement] })

  override def exitAttrib(ctx: C.AttribContext) =
    ctx.value = Attrib(ctx.ID.text, ctx.expression.value)

  override def exitSkip(ctx: C.SkipContext) =
    ctx.value = Skip

  override def exitIf(ctx: C.IfContext) =
    ctx.value = If(ctx.bool.value, ctx.statement(0).value, ctx.statement(1).value)

  override def exitWhile(ctx: C.WhileContext) =
    ctx.value = While(ctx.bool.value, ctx.statement.value)

  override def exitPrint(ctx: C.PrintContext) =
    ctx.value = Print(ctx.Text.text.drop(1).dropRight(1))

  override def exitWrite(ctx: C.WriteContext) =
    ctx.value = Write(ctx.expression.value[Expression])

  override def exitBlock(ctx: C.BlockContext) =
    ctx.value = ctx.seqStatement.value

  override def exitRead(ctx: C.ReadContext) =
    ctx.value = Read

  override def exitId(ctx: C.IdContext) =
    ctx.value = Id(ctx.ID.text)

  override def exitExpParen(ctx: C.ExpParenContext) =
    ctx.value = ctx.expression.value

  override def exitInt(ctx: C.IntContext) =
    ctx.value = Integer(ctx.text.toInt)

  override def exitBinOp(ctx: C.BinOpContext) =
    ctx.value = (ctx(1).text match {
      case "*"     => ExpMult
      case "-"     => ExpSub
      case "+" | _ => ExpSum
    })(ctx.expression(0).value, ctx.expression(1).value)

  override def exitNot(ctx: C.NotContext) =
    ctx.value = Not(ctx.bool.value)

  override def exitBoolean(ctx: C.BooleanContext) =
    ctx.value = Boole(ctx.text == "true")

  override def exitAnd(ctx: C.AndContext) =
    ctx.value = And(ctx.bool(0).value, ctx.bool(1).value)

  override def exitBoolParen(ctx: C.BoolParenContext) =
    ctx.value = ctx.bool.value

  override def exitRelOp(ctx: C.RelOpContext) =
    ctx.value = (ctx(1).text match {
      case "="      => ExpEqual
      case "<=" | _ => ExpLessOrEqualThan
    })(ctx.expression(0).value, ctx.expression(1).value)
}
````

### Language

````scala
package whilelang.interpreter

object Language {
  sealed trait Statement { def execute() = Semantics.execute(this) }
  case object Skip extends Statement
  case class If(condition: Bool, `then`: Statement, `else`: Statement) extends Statement
  case class Write(exp: Expression) extends Statement
  case class While(condition: Bool, `do`: Statement) extends Statement
  case class Print(text: String) extends Statement
  case class SeqStatement(statements: List[Statement]) extends Statement
  case class Attrib(id: String, exp: Expression) extends Statement
  case class Program(statements: SeqStatement) extends Statement

  sealed trait Expression { def value() = Semantics.value(this) }
  case object Read extends Expression
  case class Id(id: String) extends Expression
  case class Integer(exp: Int) extends Expression
  case class ExpSum(lhs: Expression, rhs: Expression) extends Expression
  case class ExpSub(lhs: Expression, rhs: Expression) extends Expression
  case class ExpMult(lhs: Expression, rhs: Expression) extends Expression

  sealed trait Bool { def value() = Semantics.value(this) }
  case class Boole(b: Boolean) extends Bool
  case class ExpEqual(lhs: Expression, rhs: Expression) extends Bool
  case class ExpLessOrEqualThan(lhs: Expression, rhs: Expression) extends Bool
  case class Not(b: Bool) extends Bool
  case class And(lhs: Bool, rhs: Bool) extends Bool

  object Semantics {
    val memory = scala.collection.mutable.Map[String, Int]()

    def execute(stmt: Statement): Unit = stmt match {
      case If(cond, thn, els)  => if (cond.value) thn.execute else els.execute
      case Write(exp)          => println(exp.value)
      case While(cond, d)      => while (cond.value) { d.execute }
      case Print(text)         => println(text)
      case SeqStatement(stmts) => stmts.foreach { _.execute }
      case Attrib(id, exp)     => memory += id -> exp.value
      case Program(seq)        => seq.execute
      case Skip | _            =>
    }

    def value(exp: Expression): Int = exp match {
      case Read              => io.StdIn.readInt
      case Id(id)            => memory.getOrElseUpdate(id, 0)
      case Integer(value)    => value
      case ExpSum(lhs, rhs)  => lhs.value + rhs.value
      case ExpSub(lhs, rhs)  => lhs.value - rhs.value
      case ExpMult(lhs, rhs) => lhs.value * rhs.value
      case _                 => 0
    }

    def value(a: Bool): Boolean = a match {
      case Boole(b)                     => b
      case ExpEqual(lhs, rhs)           => lhs.value == rhs.value
      case ExpLessOrEqualThan(lhs, rhs) => lhs.value <= rhs.value
      case Not(b)                       => !b.value
      case And(lhs, rhs)                => lhs.value && rhs.value
      case _                            => true
    }
  }
}
````

## Main

````scala
package whilelang.interpreter

import java.io.FileNotFoundException
import scala.util.{ Failure, Success, Try }
import whilelang.parser.Walker

object Main extends App {
  implicit val listener = new MyListener()  // new Compiler()
  Try(io.Source.fromFile(args(0)).getLines.mkString("\n")).flatMap(Walker.walk) match {
    case Success(_)                        => listener.program.execute  // println(listener.program)
    case Failure(e: FileNotFoundException) => println("File not found")
    case Failure(e)                        => println("Error: " + e.getLocalizedMessage)
  }
}
````

---

## Utility Classes

### Walker

````scala
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
````

### Antlr2Scala

````scala
package whilelang.parser

import org.antlr.v4.runtime.tree.{ ParseTree, ParseTreeProperty }

trait Antlr2Scala[T] {
  protected val values = new ParseTreeProperty[T]
  protected implicit class tree2scala(tree: ParseTree) {
    def apply(i: Int) = tree.getChild(i)
    def text = tree.getText
    def value[E]: E = values.get(tree).asInstanceOf[E]
    def value_=(v: T) = values.put(tree, v)
  }
}
````

---

## Compiler

````scala
package whilelang.compiler

import scala.collection.JavaConverters.asScalaBufferConverter
import whilelang.parser.{ Antlr2Scala, WhilelangBaseListener, WhilelangParser => C }
import scala.collection.immutable.StringOps

class Compiler extends WhilelangBaseListener with Antlr2Scala[String] {
  var _program: String = _
  def program = _program
  val ids = collection.mutable.Set("i")

  override def exitProgram(ctx: C.ProgramContext) =
    _program = s"""object Main extends App {
                  |  var ${ids.mkString("", ", ", "")} = 0;
                  |  ${ctx.seqStatement.value}
                  |}""".stripMargin

  override def exitSeqStatement(ctx: C.SeqStatementContext) =
    ctx.value = ctx.statement().asScala
      .map(b => b.value[String]).mkString("\n")
      .replaceAll("\n", "\n  ")

  override def exitAttrib(ctx: C.AttribContext) = {
    val id = ctx.ID.text
    ids += id
    ctx.value = s"$id = ${ctx.expression.value};"
  }

  override def exitSkip(ctx: C.SkipContext) =
    ctx.value = "()"

  override def exitIf(ctx: C.IfContext) =
    ctx.value = s"""if (${ctx.bool.value}) {
                   |  ${ctx.statement(0).value}
                   |} else {
                   |  ${ctx.statement(1).value}
                   |}""".stripMargin

  override def exitWhile(ctx: C.WhileContext) =
    ctx.value = s"""while(${ctx.bool.value}) {
                   |  ${ctx.statement.value}
                   |}""".stripMargin

  override def exitPrint(ctx: C.PrintContext) =
    ctx.value = s"println(${ctx.Text.text});"

  override def exitWrite(ctx: C.WriteContext) =
    ctx.value = s"println(${ctx.expression.value});"

  override def exitBlock(ctx: C.BlockContext) =
    ctx.value = ctx.seqStatement.value

  override def exitRead(ctx: C.ReadContext) =
    ctx.value = "readInt()"

  override def exitId(ctx: C.IdContext) =
    ctx.value = s"${ctx.ID.text}"

  override def exitExpParen(ctx: C.ExpParenContext) =
    ctx.value = ctx.expression.value

  override def exitInt(ctx: C.IntContext) =
    ctx.value = s"${ctx.text.toInt}"

  override def exitBinOp(ctx: C.BinOpContext) =
    ctx.value = s"${ctx.expression(0).value} ${ctx(1).text} ${ctx.expression(1).value}"

  override def exitNot(ctx: C.NotContext) =
    ctx.value = s"!(${ctx.bool.value})"

  override def exitBoolean(ctx: C.BooleanContext) =
    ctx.value = s"""${ctx.text == "true"}"""

  override def exitAnd(ctx: C.AndContext) =
    ctx.value = s"(${ctx.bool(0).value} && ${ctx.bool(1).value})"

  override def exitBoolParen(ctx: C.BoolParenContext) =
    ctx.value = s"(${ctx.bool.value})"

  override def exitRelOp(ctx: C.RelOpContext) =
    ctx.value = s"${ctx.expression(0).value} ${
      ctx(1).text match {
        case "=" => "=="
        case op  => op
      }
    } ${ctx.expression(1).value}"
}
````
