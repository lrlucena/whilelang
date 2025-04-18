# Core Language Components

These components are shared between both the interpreter and transpiler implementations.

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

## Parser Implementation

Shared ANTLR listener that builds the AST:

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

## Shared Utilities

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
