package whilelang.interpreter
import Language._

object Semantics:
  val memory = scala.collection.mutable.Map[String, Int]()

  def execute(stmt: Statement): Unit = stmt match
    case If(cond, thenSmt, elseSmt) => if (cond.value) thenSmt.execute() else elseSmt.execute()
    case Write(exp)                 => println(exp.value)
    case While(cond, doSmt)         => while (cond.value) { doSmt.execute() }
    case Print(text)                => println(text)
    case SeqStatement(stmts)        => stmts.foreach { _.execute() }
    case Attrib(id, exp)            => memory += id -> exp.value
    case Program(seq)               => seq.execute()
    case Skip | _                   =>

  def value(exp: Expression): Int = exp match
    case Read              => io.StdIn.readInt()
    case Id(id)            => memory.getOrElseUpdate(id, 0)
    case Integer(value)    => value
    case ExpSum(lhs, rhs)  => lhs.value + rhs.value
    case ExpSub(lhs, rhs)  => lhs.value - rhs.value
    case ExpMult(lhs, rhs) => lhs.value * rhs.value
    case null | _          => 0

  def value(a: Bool): Boolean = a match
    case Boole(b)                     => b
    case ExpEqual(lhs, rhs)           => lhs.value == rhs.value
    case ExpLessOrEqualThan(lhs, rhs) => lhs.value <= rhs.value
    case Not(b)                       => !b.value
    case And(lhs, rhs)                => lhs.value && rhs.value
    case null | _                     => true
