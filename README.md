While language
=====

  A small programming language created with ANTLR and Scala

Only 236 lines of code:

  - [Grammar](#grammar) (36 lines)
  - [Listener](#listener) (95 lines)
  - [Language](#language) (60 lines) or [Language1](src/whilelang/Language1.scala) (90 lines) or [Language2](src/whilelang/Language.scala) (88 lines)
  - [Main](#ain) (22 lines)
  - [Antlr2Scala](#antlr2scala) (13 lines)

Grammar
====

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
```

Listener
====

````scala
package whilelang

import whilelang.{ WhilelangParser => C } 
import whilelang.Language._
import scala.collection.JavaConverters._

class MyListener extends WhilelangBaseListener with Antlr2Scala {
  var _program: Program = _
  def program = _program

  override def exitProgram(ctx: C.ProgramContext) {
    _program = Program(ctx.seqStatement.value[List[Statement]])
  }

  override def exitSeqStatement(ctx: C.SeqStatementContext) {
    ctx.value = ctx.statement().asScala.toList.map { _.value[Statement] }
  }

  override def exitAttrib(ctx: C.AttribContext) {
    ctx.value = Attrib(ctx.ID.text, ctx.expression.value)
  }

  override def exitSkip(ctx: C.SkipContext) {
    ctx.value = Skip
  }

  override def exitIf(ctx: C.IfContext) = {
    ctx.value = If(ctx.bool.value, ctx.statement(0).value, ctx.statement(1).value)
  }

  override def exitWhile(ctx: C.WhileContext) {
    ctx.value = While(ctx.bool.value, ctx.statement.value)
  }

  override def exitPrint(ctx: C.PrintContext) = {
    ctx.value = Print(ctx.Text.text.drop(1).dropRight(1))
  } 

  override def exitWrite(ctx: C.WriteContext) = {
    ctx.value = Write(ctx.expression.value)
  }

  override def exitBlock(ctx: C.BlockContext) = {
    ctx.value = Block(ctx.seqStatement.value)
  }

  override def exitRead(ctx: C.ReadContext) = {
    ctx.value = Read
  }

  override def exitId(ctx: C.IdContext) {
    ctx.value = Id(ctx.ID.text)
  }

  override def exitExpParen(ctx: C.ExpParenContext) {
    ctx.value = ctx.expression.value
  }

  override def exitInt(ctx: C.IntContext) {
    ctx.value = Integer(ctx.text.toInt)
  }

  override def exitBinOp(ctx: C.BinOpContext) {
    val exp = ctx(1).text match {
      case "*" => ExpMult
      case "-" => ExpSub
      case _   => ExpSum
    }
    ctx.value = exp(ctx.expression(0).value, ctx.expression(1).value)
  }

  override def exitNot(ctx: C.NotContext) {
    ctx.value = Not(ctx.bool.value)
  }

  override def exitBoolean(ctx: C.BooleanContext) = {
    ctx.value = Boole(ctx.text == "true")
  }

  override def exitAnd(ctx: C.AndContext) {
    ctx.value = And(ctx.bool(0).value, ctx.bool(1).value)
  }

  override def exitBoolParen(ctx: C.BoolParenContext) {
    ctx.value = ctx.bool.value
  }

  override def exitRelOp(ctx: C.RelOpContext) {
    val exp = ctx(1).text match {
      case "="  => ExpEqual
      case _    => ExpLessOrEqualThan
    }
    ctx.value = exp(ctx.expression(0).value, ctx.expression(1).value)
  }
}
````

Language
====

````scala
package whilelang

object Language {
  sealed trait Statement { def execute() = Exec.execute(this) }
  case object Skip extends Statement
  case class If(condition: Bool, `then`: Statement, `else`: Statement) extends Statement
  case class Write(exp: Expression) extends Statement
  case class While(condition: Bool, `do`: Statement) extends Statement
  case class Print(text: String) extends Statement
  case class Block(statements: List[Statement]) extends Statement
  case class Attrib(id: String, exp: Expression) extends Statement
  case class Program(statements: List[Statement]) extends Statement

  sealed trait Expression { def value() = Exec.value(this) }
  case object Read extends Expression
  case class Id(id: String) extends Expression
  case class Integer(exp: Int) extends Expression
  case class ExpSum(lhs: Expression, rhs: Expression) extends Expression
  case class ExpSub(lhs: Expression, rhs: Expression) extends Expression
  case class ExpMult(lhs: Expression, rhs: Expression) extends Expression

  sealed trait Bool { def value() = Exec.value(this) }
  case class Boole(b: Boolean) extends Bool
  case class ExpEqual(lhs: Expression, rhs: Expression) extends Bool
  case class ExpLessOrEqualThan(lhs: Expression, rhs: Expression) extends Bool
  case class Not(b: Bool) extends Bool
  case class And(lhs: Bool, rhs: Bool) extends Bool
}

private[this] object Exec {
  import Language._
  val memory = scala.collection.mutable.Map[String, Int]()
  def execute(a: Statement): Unit = a match {
    case If(cond, thn, els) => if (cond.value) thn.execute else els.execute
    case Write(exp)         => println(exp.value)
    case While(cond, d)     => while (cond.value) { d.execute }
    case Print(text)        => println(text)
    case Block(stmts)       => stmts.foreach { _.execute }
    case Attrib(id, exp)    => memory += id -> exp.value
    case Program(stmts)     => stmts.foreach { _.execute }
    case Skip | _           =>
  }
  def value(a: Expression): Int = a match {
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
````

Main
====
````scala
package whilelang

import org.antlr.v4.runtime.{ ANTLRInputStream, CommonTokenStream }
import org.antlr.v4.runtime.tree.ParseTreeWalker
import scala.io.Source
import scala.util.{ Try, Success, Failure }

object Main extends App {
  def parse(source: String) = {
    val parser   = new WhilelangParser(new CommonTokenStream(new WhilelangLexer(new ANTLRInputStream(source))))
    val walker   = new ParseTreeWalker()
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
````


Antrl2Scala
====
````scala
package whilelang

import org.antlr.v4.runtime.tree.{ParseTree, ParseTreeProperty}

trait Antlr2Scala {
  protected val values = new ParseTreeProperty[Any]
  protected implicit class tree2scala(tree: ParseTree) {
    def apply(i: Int) = tree.getChild(i)
    def text = tree.getText
    def value[T]: T = values.get(tree).asInstanceOf[T]
    def value_=(v: Any) = values.put(tree, v)
  }
}
````
