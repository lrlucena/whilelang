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
