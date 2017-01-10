package whilelang

import scala.collection.mutable.Map

object Language {
  val memory = Map[String, Int]()

  class Bool(val value: () => Boolean)
  def Bool(value: => Boolean) = new Bool(() => value)

  class Statement(val execute: () => Unit)
  def Statement(execute: => Unit) = new Statement(() => execute)

  class Expression(val value: () => Int)
  def Expression(value: => Int) = new Expression(() => value)

  val Skip = Statement {}

  def If(condition: Bool, `then`: Statement, `else`: Statement) = Statement {
    if (condition.value()) `then`.execute() else `else`.execute()
  }

  def Write(exp: Expression) = Statement {
    println(exp.value())
  }

  def While(condition: Bool, `do`: Statement) = Statement {
    while (condition.value()) { `do`.execute() }
  }

  def Print(text: String) = Statement {
    println(text)
  }

  def Block(statements: List[Statement]) = Statement {
    statements.foreach { _.execute() }
  }

  def Attrib(id: String, exp: Expression) = Statement {
    memory += id -> exp.value()
  }

  def Program(statements: List[Statement]) = Statement {
    statements.foreach { _.execute() }
  }

  val Read = Expression {
    io.StdIn.readInt
  }

  def Id(id: String) = Expression {
    memory.getOrElseUpdate(id, 0)
  }

  def Integer(value: Int) = Expression {
    value
  }

  def ExpSum(lhs: Expression, rhs: Expression) = Expression {
    lhs.value() + rhs.value()
  }

  def ExpSub(lhs: Expression, rhs: Expression) = Expression {
    lhs.value() - rhs.value()
  }

  def ExpMult(lhs: Expression, rhs: Expression) = Expression {
    lhs.value() * rhs.value()
  }

  def Boole(value: Boolean) = Bool {
    value
  }

  def ExpEqual(lhs: Expression, rhs: Expression) = Bool {
    lhs.value() == rhs.value()
  }

  def ExpLessOrEqualThan(lhs: Expression, rhs: Expression) = Bool {
    lhs.value() <= rhs.value()
  }

  def Not(b: Bool) = Bool {
    !b.value()
  }

  def And(lhs: Bool, rhs: Bool) = Bool {
    lhs.value() && rhs.value()
  }
}
