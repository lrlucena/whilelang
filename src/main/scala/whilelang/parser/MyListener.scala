package whilelang.parser

import scala.compiletime.uninitialized
import whilelang.parser.Bool.*
import whilelang.parser.Expression.*
import whilelang.parser.Statement.*
import whilelang.parser.WhilelangBaseListener as BaseListener
import whilelang.parser.WhilelangParser.*
import whilelang.util.ContextValue

class MyListener extends BaseListener with ContextValue:

  var program: Program = uninitialized

  override def exitProgram(ctx: ProgramContext) =
    program = Program(ctx.seqStatement.value)

  override def exitSeqStatement(ctx: SeqStatementContext) = ctx.value_= :
    SeqStatement(ctx.statement.map(_.value[Statement]))

  override def exitAttrib(ctx: AttribContext) = ctx.value_= :
    Attrib(ctx.ID.text, ctx.expression.value)

  override def exitSkip(ctx: SkipContext) = ctx.value_= :
    Skip

  override def exitIf(ctx: IfContext) = ctx.value_= :
    val Seq(thenStat, elseStat) = ctx.statement.map(_.value[Statement])
    If(ctx.bool.value, thenStat, elseStat)

  override def exitWhile(ctx: WhileContext) = ctx.value_= :
    While(ctx.bool.value, ctx.statement.value)

  override def exitPrint(ctx: PrintContext) = ctx.value_= :
    Print(ctx.Text.text.drop(1).dropRight(1))

  override def exitWrite(ctx: WriteContext) = ctx.value_= :
    Print(ctx.expression.value)

  override def exitBlock(ctx: BlockContext) = ctx.value_= :
    ctx.seqStatement.value

  override def exitRead(ctx: ReadContext) = ctx.value_= :
    Read

  override def exitId(ctx: IdContext) = ctx.value_= :
    Id(ctx.ID.text)

  override def exitExpParen(ctx: ExpParenContext) = ctx.value_= :
    ctx.expression.value

  override def exitInt(ctx: IntContext) = ctx.value_= :
    Integer(ctx.text.toInt)

  override def exitBinOp(ctx: BinOpContext) = ctx.value_= :
    val Seq(lhs, rhs) = ctx.expression.map(_.value[Expression])
    ctx(1).text match
      case "*" => ExpMult(lhs, rhs)
      case "-" => ExpSub(lhs, rhs)
      case _ => ExpSum(lhs, rhs)

  override def exitNot(ctx: NotContext) = ctx.value_= :
    Not(ctx.bool.value)

  override def exitBoolean(ctx: BooleanContext) = ctx.value_= :
    Boole(ctx.text == "true")

  override def exitAnd(ctx: AndContext) = ctx.value_= :
    And(ctx.bool(0).value, ctx.bool(1).value)

  override def exitBoolParen(ctx: BoolParenContext) = ctx.value_= :
    ctx.bool.value

  override def exitRelOp(ctx: RelOpContext) = ctx.value_= :
    val Seq(lhs, rhs) = ctx.expression.map(_.value[Expression])
    ctx(1).text match
      case "=" => ExpEq(lhs, rhs)
      case _   => ExpLe(lhs, rhs)