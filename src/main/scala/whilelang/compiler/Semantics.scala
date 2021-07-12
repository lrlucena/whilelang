package whilelang.compiler

import whilelang.parser.Expression._
import whilelang.parser.Statement._
import whilelang.parser.Bool._

type Ids = collection.mutable.Set[String]
given Ids = collection.mutable.Set[String]()

def vars(ids: Ids) =
  if ids.nonEmpty then s"var ${ids.mkString(", ")} = 0;" else ""

extension(src: Any)(using ids: Ids)
  def translate: String = t
  private def t: String = src match
    case If(cond, tSmt, eSmt)         => s"if ${cond.t} then\n  ${tSmt.t}\nelse\n  ${eSmt.t}"
    case Write(exp)                   => s"println(${exp.t});"
    case Print(text)                  => s"print(\"$text\");"
    case While(cond, doSmt)           => s"while ${cond.t} do\n  ${doSmt.t}"
    case SeqStatement(stmts)          => stmts.map(_.t).mkString("\n").replaceAll("\n", "\n  ")
    case Attrib(id, exp)              => ids += id; s"$id = ${exp.t};"
    case Program(seq)                 => val smt = seq.t; s"@main def main() =\n  ${vars(ids)}\n  ${smt}"
    case Skip                         => "()"
    case Read                         => "readInt()"
    case Id(id)                       => id
    case Integer(value)               => "${value}"
    case ExpSum(lhs, rhs)             => s"(${lhs.t} + ${rhs.t})"
    case ExpSub(lhs, rhs)             => s"(${lhs.t} - ${rhs.t})"
    case ExpMult(lhs, rhs)            => s"(${lhs.t} * ${rhs.t})"
    case Boole(b)                     => s"${b}"
    case ExpEqual(lhs, rhs)           => s"(${lhs.t} == ${rhs.t})"
    case ExpLessOrEqualThan(lhs, rhs) => s"(${lhs.t} <= ${rhs.t})"
    case Not(b)                       => s"!(${b.t})"
    case And(lhs, rhs)                => s"(${lhs.t} && ${rhs.t})"
    case _                            => "~~~ Not Implemented ~~~"
