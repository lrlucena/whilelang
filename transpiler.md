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
package whilelang.compiler

import collection.mutable.Set
import whilelang.parser._
import whilelang.parser.Expression._
import whilelang.parser.Statement._
import whilelang.parser.Bool._

type Ids = Set[String]
given Ids = Set[String]()

def vars(ids: Ids) =
  if ids.nonEmpty then s"var ${ids.mkString(", ")} = 0" else ""

extension (src: Any)(using ids: Ids)
  def meaning: String = m
  private def m: String = src match
    case If(cond, tSmt, eSmt) => s"if ${cond.m} then\n  ${tSmt.m}\nelse\n  ${eSmt.m}"
    case Write(exp)           => s"println(${exp.m})"
    case Print(text)          => s"println(\"$text\")"
    case While(cond, doSmt)   => s"while ${cond.m} do\n  ${doSmt.m}"
    case SeqStatement(stmts)  => stmts.map(_.m).mkString("\n").replaceAll("\n", "\n  ")
    case Attrib(id, exp)      => ids.add(id); s"$id = ${exp.m}"
    case Program(seq)         => val main = seq.m; s"@main def main() =\n  ${vars(ids)}\n  ${main}"
    case Skip                 => "()"
    case Read                 => "readInt()"
    case Id(id)               => id
    case Integer(value)       => s"${value}"
    case ExpSum(lhs, rhs)     => s"(${lhs.m} + ${rhs.m})"
    case ExpSub(lhs, rhs)     => s"(${lhs.m} - ${rhs.m})"
    case ExpMult(lhs, rhs)    => s"(${lhs.m} * ${rhs.m})"
    case Boole(b)             => s"${b}"
    case ExpEq(lhs, rhs)      => s"(${lhs.m} == ${rhs.m})"
    case ExpLe(lhs, rhs)      => s"(${lhs.m} <= ${rhs.m})"
    case Not(b)               => s"!(${b.m})"
    case And(lhs, rhs)        => s"(${lhs.m} && ${rhs.m})"
    case _                    => "~~~ Not Implemented ~~~"
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
package whilelang.compiler

import whilelang.util.Runner

def action = Runner(program => println(program.meaning))

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