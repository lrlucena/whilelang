While language
=====

  A small programming language created with ANTLR and Scala

Only 258 lines of code:

  - [Grammar](src/whilelang/Whilelang.g4) (36 lines)
  - [Listener](src/whilelang/MyListener.scala) (95 lines)
  - [Language](src/whilelang/Language.scala) (88 lines) or [Language1](src/whilelang/Language.scala) (90 lines) or [Language2](src/whilelang/Language2.scala) (60 lines)
  - [Main](src/whilelang/Main.scala) (23 lines)
  - [Antlr2Scala](src/whilelang/Antlr2Scala.scala) (16 lines)

Grammar
====

```ANTLR
grammar Whilelang;

program : seqStatement;

seqStatement: statement (';' statement)* ;

statement: ID ':=' expression                          # attrib
         | 'skip'                                      # skip
         | 'if' bool 'then' statement 'else' statement # if
         | 'while' bool 'do' statement                 # while
         | 'print' Text                                # print
         | 'write' expression                          # write
         | '{' seqStatement '}'                        # block
         ;

expression: INT                                        # int
          | 'read'                                     # read
          | ID                                         # id
          | expression '*' expression                  # binOp
          | expression ('+'|'-') expression            # binOp
          | '(' expression ')'                         # expParen
          ;

bool: ('true'|'false')                                 # boolean
    | expression '=' expression                        # relOp
    | expression '<=' expression                       # relOp
    | 'not' bool                                       # not
    | bool 'and' bool                                  # and
    | '(' bool ')'                                     # boolParen
    ;

INT: ('0'..'9')+ ;
ID: ('a'..'z')+;
Text: '"' .*? '"';

Space: [ \t\n\r] -> skip;
```
