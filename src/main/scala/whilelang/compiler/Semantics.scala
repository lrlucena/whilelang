package whilelang.compiler

import whilelang.parser.*
import whilelang.parser.Bool.*
import whilelang.parser.Expression.*
import whilelang.parser.Statement.*

import scala.collection.mutable.Set as MSet

type Ids = MSet[String]
given Ids = MSet[String]()

def vars(ids: Ids) =
  if ids.nonEmpty then s"var ${ids.mkString(", ")} = 0" else ""

extension (src: Element)(using ids: Ids)
  def meaning: String = m

  private def op(s: String)(lhs: Element, rhs: Element) = s"(${lhs.m} $s ${rhs.m})"

  private def m: String = src match
    case If(cond, tSmt, eSmt)   => s"if ${cond.m} then\n  ${tSmt.m}\nelse\n  ${eSmt.m}"
    case Print(exp: Expression) => s"println(${exp.m})"
    case Print(text: String)    => s"println(\"$text\")"
    case While(cond, doSmt)     => s"while ${cond.m} do\n  ${doSmt.m}"
    case SeqStatement(stmts)    => stmts.map(_.m).mkString("\n").replaceAll("\n", "\n  ")
    case Attrib(id, exp)        => ids.add(id); s"$id = ${exp.m}"
    case Program(seq)           => s"@main def main() =\n  ${vars(ids)}\n  ${seq.m}"
    case Skip                   => "()"
    case Read                   => "readInt()"
    case Id(id)                 => id
    case Integer(value)         => s"$value"
    case Boole(b)               => s"$b"
    case Not(b)                 => s"(!${b.m})"
    case ExpSum(lhs, rhs)       => op("+")(lhs, rhs)
    case ExpSub(lhs, rhs)       => op("-")(lhs, rhs)
    case ExpMult(lhs, rhs)      => op("*")(lhs, rhs)
    case ExpEq(lhs, rhs)        => op("==")(lhs, rhs)
    case ExpLe(lhs, rhs)        => op("<=")(lhs, rhs)
    case And(lhs, rhs)          => op("&&")(lhs, rhs)
    case _                      => "~~~ Not Implemented ~~~"
