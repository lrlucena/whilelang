package whilelang.compiler

import scala.jdk.CollectionConverters._
import scala.collection.immutable.StringOps
import scala.language.implicitConversions
import whilelang.parser.{ Antlr2Scala, WhilelangBaseListener}
import whilelang.parser.WhilelangParser._

class Compiler extends WhilelangBaseListener with Antlr2Scala[String]:
  var program: String = _
  val ids = collection.mutable.Set[String]()

  override def exitProgram(ctx: ProgramContext) =
    program = s"""@main def main() =
                 |  ${if ids.nonEmpty then s"var ${ids.mkString(", ")} = 0;" else ""}
                 |  ${ctx.seqStatement.value}
                 |""".stripMargin

  override def exitSeqStatement(ctx: SeqStatementContext) =
    ctx.value = ctx.statement().asScala
      .map(b => b.value[String])
      .mkString("\n").replaceAll("\n", "\n  ")

  override def exitAttrib(ctx: AttribContext) =
    val id = ctx.ID.text
    ids += id
    ctx.value = s"$id = ${ctx.expression.value};"

  override def exitSkip(ctx: SkipContext) =
    ctx.value = "()"

  override def exitIf(ctx: IfContext) =
    ctx.value = s"""if ${ctx.bool.value} then
                   |  ${ctx.statement(0).value}
                   |else
                   |  ${ctx.statement(1).value}
                   |""".stripMargin

  override def exitWhile(ctx: WhileContext) =
    ctx.value = s"""while ${ctx.bool.value} do
                   |  ${ctx.statement.value}
                   |""".stripMargin

  override def exitPrint(ctx: PrintContext) =
    ctx.value = s"println(${ctx.Text.text});"

  override def exitWrite(ctx: WriteContext) =
    ctx.value = s"println(${ctx.expression.value});"

  override def exitBlock(ctx: BlockContext) =
    ctx.value = ctx.seqStatement.value

  override def exitRead(ctx: ReadContext) =
    ctx.value = "readInt()"

  override def exitId(ctx: IdContext) =
    ctx.value = s"${ctx.ID.text}"

  override def exitExpParen(ctx: ExpParenContext) =
    ctx.value = ctx.expression.value

  override def exitInt(ctx: IntContext) =
    ctx.value = s"${ctx.text.toInt}"

  override def exitBinOp(ctx: BinOpContext) =
    ctx.value = s"${ctx.expression(0).value} ${ctx(1).text} ${ctx.expression(1).value}"

  override def exitNot(ctx: NotContext) =
    ctx.value = s"!(${ctx.bool.value})"

  override def exitBoolean(ctx: BooleanContext) =
    ctx.value = s"""${ctx.text == "true"}"""

  override def exitAnd(ctx: AndContext) =
    ctx.value = s"(${ctx.bool(0).value} && ${ctx.bool(1).value})"

  override def exitBoolParen(ctx: BoolParenContext) =
    ctx.value = s"(${ctx.bool.value})"

  override def exitRelOp(ctx: RelOpContext) =
    ctx.value = s"${ctx.expression(0).value} ${
      ctx(1).text match
        case "=" => "=="
        case op  => op
    } ${ctx.expression(1).value}"
