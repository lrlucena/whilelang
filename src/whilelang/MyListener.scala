package whilelang

import whilelang.{ WhilelangParser => C } 
import whilelang.Language._
import scala.collection.JavaConverters._

class MyListener extends WhilelangBaseListener with Antlr2Scala {
  var _program: Program = _
  def program = _program

  override def exitProgram(ctx: C.ProgramContext) {
    ctx.value = Program(ctx.seqStatement.value[List[Statement]])
    _program = ctx.value
  }

  override def exitSeqStatement(ctx: C.SeqStatementContext) {
    val cmds = ctx.statement().asScala.toList.map { _.value[Statement] }
    ctx.value = cmds
  }

  override def exitAttrib(ctx: C.AttribContext) {
    ctx.value = Attrib(ctx.ID.text, ctx.expression.value)
  }

  override def exitSkip(ctx: C.SkipContext) {
    ctx.value = Skip
  }

  override def exitIf(ctx: C.IfContext) = {
    ctx.value = new If(ctx.bool.value, ctx.statement(0).value, ctx.statement(1).value)
  }

  override def exitWhile(ctx: C.WhileContext) {
    ctx.value = new While(ctx.bool.value, ctx.statement.value)
  }

  override def exitPrint(ctx: C.PrintContext) = {
    ctx.value = Print(ctx.Text.text)
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
