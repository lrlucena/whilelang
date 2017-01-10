package whilelang.interpreter

object Language {
  sealed trait Statement { def execute() = Semantics.execute(this) }
  case object Skip extends Statement
  case class If(condition: Bool, `then`: Statement, `else`: Statement) extends Statement
  case class Write(exp: Expression) extends Statement
  case class While(condition: Bool, `do`: Statement) extends Statement
  case class Print(text: String) extends Statement
  case class SeqStatement(statements: List[Statement]) extends Statement
  case class Attrib(id: String, exp: Expression) extends Statement
  case class Program(statements: SeqStatement) extends Statement

  sealed trait Expression { def value() = Semantics.value(this) }
  case object Read extends Expression
  case class Id(id: String) extends Expression
  case class Integer(exp: Int) extends Expression
  case class ExpSum(lhs: Expression, rhs: Expression) extends Expression
  case class ExpSub(lhs: Expression, rhs: Expression) extends Expression
  case class ExpMult(lhs: Expression, rhs: Expression) extends Expression

  sealed trait Bool { def value() = Semantics.value(this) }
  case class Boole(b: Boolean) extends Bool
  case class ExpEqual(lhs: Expression, rhs: Expression) extends Bool
  case class ExpLessOrEqualThan(lhs: Expression, rhs: Expression) extends Bool
  case class Not(b: Bool) extends Bool
  case class And(lhs: Bool, rhs: Bool) extends Bool

  object Semantics {
    val memory = scala.collection.mutable.Map[String, Int]()

    def execute(stmt: Statement): Unit = stmt match {
      case If(cond, thn, els)  => if (cond.value) thn.execute else els.execute
      case Write(exp)          => println(exp.value)
      case While(cond, d)      => while (cond.value) { d.execute }
      case Print(text)         => println(text)
      case SeqStatement(stmts) => stmts.foreach { _.execute }
      case Attrib(id, exp)     => memory += id -> exp.value
      case Program(seq)        => seq.execute
      case Skip | _            =>
    }

    def value(exp: Expression): Int = exp match {
      case Read              => io.StdIn.readInt
      case Id(id)            => memory.getOrElseUpdate(id, 0)
      case Integer(value)    => value
      case ExpSum(lhs, rhs)  => lhs.value + rhs.value
      case ExpSub(lhs, rhs)  => lhs.value - rhs.value
      case ExpMult(lhs, rhs) => lhs.value * rhs.value
      case _                 => 0
    }

    def value(a: Bool): Boolean = a match {
      case Boole(b)                     => b
      case ExpEqual(lhs, rhs)           => lhs.value == rhs.value
      case ExpLessOrEqualThan(lhs, rhs) => lhs.value <= rhs.value
      case Not(b)                       => !b.value
      case And(lhs, rhs)                => lhs.value && rhs.value
      case _                            => true
    }
  }
}
