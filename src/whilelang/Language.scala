package whilelang

object Language {
  val memory = scala.collection.mutable.Map[String, Int]()
  trait Bool {
    def value: Boolean
  } 

  trait Statement {
    def execute: Unit
  }

  trait Expression {
    def value: Int
  }

  case class If(condition: Bool, `then`: Statement, `else`: Statement) extends Statement {
    def execute = if (condition.value) `then`.execute else `else`.execute
  }

  case object Skip {
    def execute = ()
  }

  case class Write(exp: Expression) extends Statement {
    def execute = println(exp.value)
  }

  case class While(condition: Bool, `do`: Statement) extends Statement {
    def execute = while (condition.value) `do`.execute
  }

  case class Print(text: String) extends Statement {
    def execute = println(text)
  }

  case class Block(statements: List[Statement]) extends Statement {
    def execute = statements.foreach { _.execute }
  }

  case class Attrib(id: String, exp: Expression) extends Statement {
    def execute = memory += id -> exp.value
  }

  case class Program(statements: List[Statement]) extends Statement {
    def execute = statements.foreach { _.execute }
  }

  case object Read extends Expression {
    def value = io.StdIn.readInt
  }

  case class Id(id: String) extends Expression {
    def value = memory.getOrElseUpdate(id, 0)
  }

  case class Integer(value: Int) extends Expression

  case class ExpSum(lhs: Expression, rhs: Expression) extends Expression {
    def value = lhs.value + rhs.value
  }

  case class ExpSub(lhs: Expression, rhs: Expression) extends Expression {
    def value = lhs.value - rhs.value
  }

  case class ExpMult(lhs: Expression, rhs: Expression) extends Expression {
    def value = lhs.value * rhs.value
  }

  case class Boole(value: Boolean) extends Bool

  case class ExpEqual(lhs: Expression, rhs: Expression) extends Bool {
    def value = lhs.value == rhs.value
  }
  
  case class ExpLessOrEqualThan(lhs: Expression, rhs: Expression) extends Bool {
    def value = lhs.value <= rhs.value
  }

  case class Not(b: Bool) extends Bool {
    def value = !b.value
  }
  
  case class And(lhs: Bool, rhs: Bool) extends Bool {
    def value = lhs.value && rhs.value
  }
}
