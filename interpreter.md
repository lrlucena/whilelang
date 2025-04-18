# While Language Interpreter

This section details the implementation of the While language interpreter, which executes programs directly using Scala's evaluation capabilities. The interpreter follows a classic parse-evaluate pipeline with ANTLR for parsing and custom semantic rules for execution.

---

## Running the Interpreter

Execute While programs using sbt with the following commands:

````shell
$ sbt

# Run the interpreter on a While program
sbt> runMain whilelang.interpreter.main sum.while
````

---

## Language Grammar

The foundation of the interpreter is defined through this ANTLR grammar:

````antlr
grammar Whilelang;

/* Core program structure */
program : seqStatement;  // Entry point for programs

/* Statement sequencing */
seqStatement: statement (';' statement)* ;  // Semicolon-separated statements

/* Statement definitions */
statement: ID ':=' expression                          # attrib  // Variable assignment
         | 'skip'                                      # skip    // No-op
         | 'if' bool 'then' statement 'else' statement # if      // Conditional
         | 'while' bool 'do' statement                 # while   // Loop
         | 'print' Text                                # print   // String output
         | 'write' expression                          # write   // Expression output
         | '{' seqStatement '}'                        # block   // Statement grouping
         ;

/* Expression hierarchy */
expression: INT                                        # int     // Literal
          | 'read'                                     # read    // Input
          | ID                                         # id      // Variable
          | expression '*' expression                  # binOp   // Multiplication
          | expression ('+'|'-') expression            # binOp   // Add/Sub
          | '(' expression ')'                         # expParen// Precedence
          ;

/* Boolean expressions */
bool: ('true'|'false')                                 # boolean // Literals
    | expression '=' expression                        # relOp   // Equality
    | expression '<=' expression                       # relOp   // Comparison
    | 'not' bool                                       # not     // Negation
    | bool 'and' bool                                  # and     // Conjunction
    | '(' bool ')'                                     # boolParen
    ;

// Lexer rules
INT: ('0'..'9')+ ;         // Integer literals
ID: ('a'..'z')+;           // Identifiers
Text: '"' .*? '"';         // String literals
Space: [ \t\n\r] -> skip;  // Whitespace handling
````

Key features:
- Simple expression hierarchy with basic arithmetic operations
- Classic control structures (if/else, while)
- I/O operations through read/write statements
- Block scoping with curly braces

---

## Abstract Syntax Tree (AST)

The AST is defined using Scala 3 enums for type-safe tree construction:

````scala
package whilelang.parser

// Statement hierarchy
enum Statement:
  case Skip  // No operation
  case If(condition: Bool, thenSmt: Statement, elseSmt: Statement)
  case Write(exp: Expression)          // Output expression value
  case While(condition: Bool, doSmt: Statement)
  case Print(text: String)             // Output literal string
  case SeqStatement(statements: List[Statement])  // Statement sequence
  case Attrib(id: String, exp: Expression)  // Variable assignment
  case Program(statements: SeqStatement)    // Root node

// Expression types
enum Expression:
  case Read                            // Input operation
  case Id(id: String)                  // Variable reference
  case Integer(exp: Int)               // Integer literal
  case ExpSum(lhs: Expression, rhs: Expression)
  case ExpSub(lhs: Expression, rhs: Expression)
  case ExpMult(lhs: Expression, rhs: Expression)

// Boolean expressions
enum Bool:
  case Boole(b: Boolean)              // Boolean literal
  case ExpEq(lhs: Expression, rhs: Expression)
  case ExpLe(lhs: Expression, rhs: Expression)
  case Not(b: Bool)                   // Logical negation
  case And(lhs: Bool, rhs: Bool)      // Logical conjunction
````

The AST provides:
- Type-safe representation of program structure
- Pattern-matchable cases for semantic analysis
- Clear separation of statements, expressions, and boolean logic

---

## Execution Semantics

The interpreter implements operational semantics through extension methods:

````scala
package whilelang.interpreter

import scala.collection.mutable.Map

// Execution environment (mutable state)
type Environment = Map[String, Int]
given Environment = Map[String, Int]()

// Statement execution logic
extension (stmt: Statement)(using env: Environment)
  def execute: Unit = stmt match
    case If(cond, tSmt, eSmt) => (if cond.value then tSmt else eSmt).execute
    case Write(exp)           => println(exp.value)      // Output expression
    case While(cond, doSmt)   => while cond.value do doSmt.execute  // Loop
    case Print(text)          => println(text)           // Output text
    case SeqStatement(stmts)  => stmts.foreach(_.execute)// Sequence
    case Attrib(id, exp)      => env += id -> exp.value  // Assignment
    case Program(seq)         => seq.execute             // Program entry
    case Skip | _             =>                        // No-op

// Expression evaluation
extension(exp: Expression)(using env: Environment)
  def value: Int = exp match
    case Read                 => io.StdIn.readInt()     // Input
    case Id(id)               => env.getOrElseUpdate(id, 0)  // Variable lookup
    case Integer(value)       => value                  // Literal
    case ExpSum(lhs, rhs)     => lhs.value + rhs.value  // Addition
    case ExpSub(lhs, rhs)     => lhs.value - rhs.value  // Subtraction
    case ExpMult(lhs, rhs)    => lhs.value * rhs.value  // Multiplication

// Boolean evaluation
extension(exp: Bool)(using env: Environment)
  def value: Boolean = exp match
    case Boole(b)             => b                      // Literal
    case ExpEq(lhs, rhs)      => lhs.value == rhs.value // Equality
    case ExpLe(lhs, rhs)      => lhs.value <= rhs.value // Comparison
    case Not(b)               => !b.value               // Negation
    case And(lhs, rhs)        => lhs.value && rhs.value // Conjunction
````

Key characteristics:
- Mutable environment for variable storage
- Strict evaluation semantics
- Direct mapping from AST nodes to execution logic
- Recursive tree walking through pattern matching

---

## Parser Implementation

The ANTLR listener converts parse trees to AST nodes:

````scala
package whilelang.parser

class MyListener extends WhilelangBaseListener with ContextValue:
  var program: Program = _  // Root AST node

  // Build AST nodes from parse tree events
  override def exitProgram(ctx: ProgramContext) =
    ctx.value = Program(ctx.seqStatement.value)
    program = ctx.value

  override def exitSeqStatement(ctx: SeqStatementContext) =
    ctx.value = SeqStatement(ctx.statement.asScala.toList.map(_.value[Statement]))

  override def exitAttrib(ctx: AttribContext) =
    ctx.value = Attrib(ctx.ID.text, ctx.expression.value)

  // Additional listener methods handle other node types...
````

Parser features:
- Inherits from ANTLR-generated WhilelangBaseListener
- Uses ContextValue trait for tree property management
- Constructs typed AST nodes during parse tree walk
- Handles operator precedence through nested listener calls

---

## Main Entry Point

The interpreter's entry point coordinates execution:

````scala
package whilelang.interpreter

import whilelang.util.Runner

// Execution entry point
def action = Runner(program => program.execute)

@main def main(file: String) = action(file)
````

Flow:
1. Accepts filename argument
2. Uses Runner utility to handle file processing
3. Triggers execution pipeline

---

## Core Utilities

### Runner Class

Handles file processing and error management:

````scala
package whilelang.util

object Runner:
  def apply(action: Program => Unit)(file: String) = 
    sourceCode(file).flatMap(walk) match
      case Success(program) => action(program)  // Execute valid program
      case Failure(e: FileNotFoundException) => println("File not found")
      case Failure(e) => println("Error: " + e.printStackTrace())
````

Responsibilities:
- File I/O operations
- Error handling
- Pipeline coordination

### Walker Component

Manages parsing workflow:

````scala
package whilelang.util

object Walker:
  // ANTLR configuration with error listener
  private def addListener(r: Recognizer[?, ?]*): Unit = 
    r.map: r =>
      r.removeErrorListeners()
      r.addErrorListener(ThrowingErrorListener)

  // Parse tree construction
  def walk(source: String)(using listener: MyListener) = Try:
    val lexer = WhilelangLexer(CharStreams.fromString(source))
    val parser = WhilelangParser(CommonTokenStream(lexer))
    addListener(lexer, parser)
    ParseTreeWalker().walk(listener, parser.program)
    listener.program
````

Features:
- ANTLR stream management
- Error listener integration
- Parse tree walking

### ContextValue Trait

Provides tree property management:

````scala
package whilelang.util

trait ContextValue:
  given ParseTreeProperty[Any] = ParseTreeProperty[Any]()

  extension (tree: ParseTree)(using values: ParseTreeProperty[Any])
    def value[E]: E = values.get(tree).asInstanceOf[E]
    def value_=(v: Any) = values.put(tree, v)
````

Purpose:
- Stores intermediate values during parse tree construction
- Enables type-safe value retrieval
- Facilitates AST node creation

---

This implementation demonstrates a classic interpreter architecture with:
1. Lexer/Parser (ANTLR)
2. Abstract Syntax Tree (Scala enums)
3. Semantic Rules (Extension methods)
4. Execution Environment (Mutable state)
5. Utilities for file handling and error management

The interpreter provides immediate feedback for While programs while maintaining a clear separation between syntax and semantics.
