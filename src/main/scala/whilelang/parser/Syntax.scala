package whilelang.parser

trait Element

enum Statement extends Element:
  case Skip
  case If(condition: Bool, thenSmt: Statement, elseSmt: Statement)
  case Write(exp: Expression)
  case While(condition: Bool, doSmt: Statement)
  case Print(text: String)
  case SeqStatement(statements: Seq[Statement])
  case Attrib(id: String, exp: Expression)
  case Program(statements: SeqStatement)

enum Expression extends Element:
  case Read
  case Id(id: String)
  case Integer(exp: Int)
  case ExpSum(lhs: Expression, rhs: Expression)
  case ExpSub(lhs: Expression, rhs: Expression)
  case ExpMult(lhs: Expression, rhs: Expression)

enum Bool extends Element:
  case Boole(b: Boolean)
  case ExpEq(lhs: Expression, rhs: Expression)
  case ExpLe(lhs: Expression, rhs: Expression)
  case Not(b: Bool)
  case And(lhs: Bool, rhs: Bool)
