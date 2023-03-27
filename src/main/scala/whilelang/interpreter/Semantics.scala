package whilelang.interpreter

import scala.collection.mutable.Map
import whilelang.parser._
import whilelang.parser.Statement._
import whilelang.parser.Expression._
import whilelang.parser.Bool._

type Environment = Map[String, Int]
given Environment = Map[String, Int]()

extension (stmt: Statement)(using env: Environment)
  def execute: Unit = stmt match
    case If(cond, tSmt, eSmt) => (if cond.value then tSmt else eSmt).execute
    case Write(exp)           => println(exp.value)
    case While(cond, doSmt)   => while cond.value do doSmt.execute
    case Print(text)          => println(text)
    case SeqStatement(stmts)  => stmts.foreach(_.execute)
    case Attrib(id, exp)      => env += id -> exp.value
    case Program(seq)         => seq.execute
    case Skip | _             =>

extension (exp: Expression)(using env: Environment)
  def value: Int = exp match
    case Read                 => io.StdIn.readInt()
    case Id(id)               => env.getOrElseUpdate(id, 0)
    case Integer(value)       => value
    case ExpSum(lhs, rhs)     => lhs.value + rhs.value
    case ExpSub(lhs, rhs)     => lhs.value - rhs.value
    case ExpMult(lhs, rhs)    => lhs.value * rhs.value
    case null | _             => 0

extension (exp: Bool)(using env: Environment)
  def value: Boolean = exp match
    case Boole(b)             => b
    case ExpEq(lhs, rhs)      => lhs.value == rhs.value
    case ExpLe(lhs, rhs)      => lhs.value <= rhs.value
    case Not(b)               => !b.value
    case And(lhs, rhs)        => lhs.value && rhs.value
    case null | _             => true
