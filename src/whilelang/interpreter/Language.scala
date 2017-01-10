package whilelang

object Language2 {
  sealed trait Statement { def execute() = Exec.execute(this) }
  case object Skip extends Statement
  case class If(condition: Bool, `then`: Statement, `else`: Statement) extends Statement
  case class Write(exp: Expression) extends Statement
  case class While(condition: Bool, `do`: Statement) extends Statement
  case class Print(text: String) extends Statement
  case class Block(statements: List[Statement]) extends Statement
  case class Attrib(id: String, exp: Expression) extends Statement
  case class Program(statements: List[Statement]) extends Statement

  sealed trait Expression { def value() = Exec.value(this) }
  case object Read extends Expression
  case class Id(id: String) extends Expression
  case class Integer(exp: Int) extends Expression
  case class ExpSum(lhs: Expression, rhs: Expression) extends Expression
  case class ExpSub(lhs: Expression, rhs: Expression) extends Expression
  case class ExpMult(lhs: Expression, rhs: Expression) extends Expression

  sealed trait Bool { def value() = Exec.value(this) }
  case class Boole(b: Boolean) extends Bool
  case class ExpEqual(lhs: Expression, rhs: Expression) extends Bool
  case class ExpLessOrEqualThan(lhs: Expression, rhs: Expression) extends Bool
  case class Not(b: Bool) extends Bool
  case class And(lhs: Bool, rhs: Bool) extends Bool
}

private[this] object Exec {
  import Language2._
  val memory = scala.collection.mutable.Map[String, Int]()
  def execute(a: Statement): Unit = a match {
    case If(cond, thn, els) => if (cond.value) thn.execute else els.execute
    case Write(exp)         => println(exp.value)
    case While(cond, d)     => while (cond.value) { d.execute }
    case Print(text)        => println(text)
    case Block(stmts)       => stmts.foreach { _.execute }
    case Attrib(id, exp)    => memory += id -> exp.value
    case Program(stmts)     => stmts.foreach { _.execute }
    case Skip | _           =>
  }
  def value(a: Expression): Int = a match {
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
