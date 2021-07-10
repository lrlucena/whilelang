package whilelang.interpreter

type Memory = collection.mutable.Map[String, Int]
given Memory = collection.mutable.Map[String, Int]()

extension(stmt: Statement)(using memory: Memory)
  def execute: Unit = stmt match
    case Statement.If(cond, tSmt, eSmt) => (if cond.value then tSmt else eSmt).execute
    case Statement.Write(exp)           => println(exp.value)
    case Statement.While(cond, doSmt)   => while cond.value do doSmt.execute
    case Statement.Print(text)          => println(text)
    case Statement.SeqStatement(stmts)  => stmts.foreach { _.execute }
    case Statement.Attrib(id, exp)      => memory += id -> exp.value
    case Statement.Program(seq)         => seq.execute
    case Statement.Skip | _             =>

extension(exp: Expression)(using memory: Memory)
  def value: Int = exp match
    case Expression.Read              => io.StdIn.readInt()
    case Expression.Id(id)            => memory.getOrElseUpdate(id, 0)
    case Expression.Integer(value)    => value
    case Expression.ExpSum(lhs, rhs)  => lhs.value + rhs.value
    case Expression.ExpSub(lhs, rhs)  => lhs.value - rhs.value
    case Expression.ExpMult(lhs, rhs) => lhs.value * rhs.value
    case null | _                     => 0

extension(exp: Bool)(using memory: Memory)
  def value: Boolean = exp match
    case Bool.Boole(b)                     => b
    case Bool.ExpEqual(lhs, rhs)           => lhs.value == rhs.value
    case Bool.ExpLessOrEqualThan(lhs, rhs) => lhs.value <= rhs.value
    case Bool.Not(b)                       => !b.value
    case Bool.And(lhs, rhs)                => lhs.value && rhs.value
    case null | _                          => true
