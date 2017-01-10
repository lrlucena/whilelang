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
