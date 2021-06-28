package whilelang.interpreter

object Language:
  sealed trait Statement:
    def execute() = Semantics.execute(this)
  case object Skip extends Statement
  case class If(condition: Bool, thenSmt: Statement, elseSmt: Statement) extends Statement
  case class Write(exp: Expression) extends Statement
  case class While(condition: Bool, doSmt: Statement) extends Statement
  case class Print(text: String) extends Statement
  case class SeqStatement(statements: List[Statement]) extends Statement
  case class Attrib(id: String, exp: Expression) extends Statement
  case class Program(statements: SeqStatement) extends Statement

  sealed trait Expression:
    def value = Semantics.value(this)
  case object Read extends Expression
  case class Id(id: String) extends Expression
  case class Integer(exp: Int) extends Expression
  case class ExpSum(lhs: Expression, rhs: Expression) extends Expression
  case class ExpSub(lhs: Expression, rhs: Expression) extends Expression
  case class ExpMult(lhs: Expression, rhs: Expression) extends Expression

  sealed trait Bool:
    def value = Semantics.value(this)
  case class Boole(b: Boolean) extends Bool
  case class ExpEqual(lhs: Expression, rhs: Expression) extends Bool
  case class ExpLessOrEqualThan(lhs: Expression, rhs: Expression) extends Bool
  case class Not(b: Bool) extends Bool
  case class And(lhs: Bool, rhs: Bool) extends Bool
