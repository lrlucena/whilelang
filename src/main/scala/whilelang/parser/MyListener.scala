package whilelang.parser

import whilelang.parser.Bool.*
import whilelang.parser.Expression.*
import whilelang.parser.Statement.*
import whilelang.parser.WhilelangBaseListener as BaseListener
import whilelang.parser.WhilelangParser.*
import whilelang.util.ContextValue

class MyListener extends BaseListener with ContextValue:

  var program: Program = _

  override def exitProgram(ctx: ProgramContext): Unit = program = Program:
    ctx.seqStatement.value

  override def exitSeqStatement(ctx: SeqStatementContext): Unit = ctx.value_= :
    SeqStatement(ctx.statement.map(_.value[Statement]))

  override def exitAttrib(ctx: AttribContext): Unit = ctx.value_= :
    Attrib(ctx.ID.text, ctx.expression.value)

  override def exitSkip(ctx: SkipContext): Unit = ctx.value_= :
    Skip

  override def exitIf(ctx: IfContext): Unit = ctx.value_= :
    If(ctx.bool.value, ctx.statement(0).value, ctx.statement(1).value)

  override def exitWhile(ctx: WhileContext): Unit = ctx.value_= :
    While(ctx.bool.value, ctx.statement.value)

  override def exitPrint(ctx: PrintContext): Unit = ctx.value_= :
    Print(ctx.Text.text.drop(1).dropRight(1))

  override def exitWrite(ctx: WriteContext): Unit = ctx.value_= :
    Write(ctx.expression.value)

  override def exitBlock(ctx: BlockContext): Unit = ctx.value_= :
    ctx.seqStatement.value

  override def exitRead(ctx: ReadContext): Unit = ctx.value_= :
    Read

  override def exitId(ctx: IdContext): Unit = ctx.value_= :
    Id(ctx.ID.text)

  override def exitExpParen(ctx: ExpParenContext): Unit = ctx.value_= :
    ctx.expression.value

  override def exitInt(ctx: IntContext): Unit = ctx.value_= :
    Integer(ctx.text.toInt)

  override def exitBinOp(ctx: BinOpContext): Unit = ctx.value_= :
    val Seq(lhs, rhs): Seq[Expression] = ctx.expression.map(_.value)
    ctx(1).text match
      case "*" => ExpMult(lhs, rhs)
      case "-" => ExpSub(lhs, rhs)
      case "+" | _ => ExpSum(lhs, rhs)

  override def exitNot(ctx: NotContext): Unit = ctx.value_= :
    Not(ctx.bool.value)

  override def exitBoolean(ctx: BooleanContext): Unit = ctx.value_= :
    Boole(ctx.text == "true")

  override def exitAnd(ctx: AndContext): Unit = ctx.value_= :
    And(ctx.bool(0).value, ctx.bool(1).value)

  override def exitBoolParen(ctx: BoolParenContext): Unit = ctx.value_= :
    ctx.bool.value

  override def exitRelOp(ctx: RelOpContext): Unit = ctx.value_= :
    val Seq(lhs, rhs): Seq[Expression] = ctx.expression.map(_.value)
    ctx(1).text match
      case "="      => ExpEq(lhs, rhs)
      case "<=" | _ => ExpLe(lhs, rhs)
