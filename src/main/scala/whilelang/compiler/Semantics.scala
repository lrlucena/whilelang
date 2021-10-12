package whilelang.compiler

import whilelang.parser._
import whilelang.parser.Expression._
import whilelang.parser.Statement._
import whilelang.parser.Bool._

type Ids = collection.mutable.Set[String]
given Ids = collection.mutable.Set[String]()

def vars(ids: Ids) =
  if ids.nonEmpty then s"var ${ids.mkString(", ")} = 0" else ""

extension(src: Any)(using ids: Ids)
  def meaning: String = m
  private def m: String = src match
    case If(cond, tSmt, eSmt) => s"if ${cond.m} then\n  ${tSmt.m}\nelse\n  ${eSmt.m}"
    case Write(exp)           => s"println(${exp.m})"
    case Print(text)          => s"println(\"$text\")"
    case While(cond, doSmt)   => s"while ${cond.m} do\n  ${doSmt.m}"
    case SeqStatement(stmts)  => stmts.map(_.m).mkString("\n").replaceAll("\n", "\n  ")
    case Attrib(id, exp)      => ids += id; s"$id = ${exp.m}"  //;
    case Program(seq)         => s"@main def main() =\n  ${vars(ids)}\n  ${seq.m}"
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
