grammar Dost;

start : stmts EOF;

stmts_block : LBRACE stmts RBRACE ;
stmts
    : stmt (stmt_end+ stmts)?   # compoundStmt
    | stmt_end+ stmts           # blankStmt
    | /* € */                   # noStmt
    ;

stmt_end : (NEWLINE | SEMI) ;

stmt
    : VAR IDENT ASSIGN expr                 # variableDecl
    | lvalue ASSIGN expr                    # assignment
    | if_stmt                               # ifStmt
    | WHILE LPAREN expr RPAREN stmts_block  # whileLoop
    | for_loop                              # forLoop
    | PRINT expr                            # printStmt
    ;

lvalue
    : IDENT
    | lvalue LBRACK index=expr RBRACK
    ;

if_stmt : IF LPAREN expr RPAREN stmts_block if_end? ;
if_end : NEWLINE* ELSE (if_stmt | stmts_block);

for_loop : FOR LPAREN IDENT IN init=expr range end=expr RPAREN stmts_block ;
range : UPTO | UPTOINC | DOWNTO | DOWNTOINC ;

expr
    : ident=IDENT                   # identExpr
    | value=literal                 # literalExpr
    | LPAREN value=expr RPAREN      # parenExpr
    | target=expr LBRACK index=expr RBRACK  # indexAccessExpr
    | BANG value=expr               # notExpr
    | SUB value=expr                # negationExpr
    | left=expr MOD right=expr      # moduloExpr
    | left=expr DIV right=expr      # divisionExpr
    | left=expr MUL right=expr      # multiplictionExpr
    | left=expr SUB right=expr      # substractionExpr
    | left=expr ADD right=expr      # additionExpr
    | left=expr LEQ right=expr      # lessOrEqExpr
    | left=expr LT right=expr       # lessThanExpr
    | left=expr GEQ right=expr      # greaterOrEqExpr
    | left=expr GT right=expr       # greaterThanExpr
    | left=expr EQ right=expr       # equalityExpr
    | left=expr NEQ right=expr      # inequalityExpr
    | left=expr AND right=expr      # andExpr
    | left=expr OR right=expr       # orExpr
    ;

fun_call : IDENT LPAREN (expr (COMMA expr)* )? RPAREN ;

literal
    : array_lit
    | INUM
    | FNUM
    | TRUE
    | FALSE
    | STRING
    ;

array_lit : LBRACK expr type RBRACK ;

type
    : IDENT                     # simpleType
    | subtype=type '[]'         # arrayType
    ;


VAR : 'var' ;
IF : 'if' ;
ELSE : 'else' ;
FUN : 'fun' ;
RETURN : 'return' ;
FOR : 'for' ;
IN : 'in' ;
WHILE : 'while' ;
PRINT : 'print' ;
TRUE : 'true' ;
FALSE : 'false' ;
ARRAY : 'array' ;

IDENT : [a-zA-Z]([a-zA-Z0-9]+)? ;

LPAREN : '(' ;
RPAREN : ')' ;
LBRACE : '{' ;
RBRACE : '}' ;
LBRACK : '[' ;
RBRACK : ']' ;
SEMI : ';' ;
COMMA : ',' ;
DOT : '.' ;
COLON : ':' ;

ASSIGN : '=' ;
GT : '>' ;
LT : '<' ;
EQ : '==' ;
LEQ : '<=' ;
GEQ : '>=' ;
NEQ : '!=' ;
BANG : '!' ;
AND : '&&' ;
OR : '||' ;

ADD : '+' ;
SUB : '-' ;
MUL : '*' ;
DIV : '/' ;
MOD : '%' ;

INC : '++' ;
DEC : '--' ;

UPTO : '..' ;
UPTOINC : '..=' ;
DOWNTO : '``' ;
DOWNTOINC : '``=' ;

WHITESPACE : [ \t\u000C]+ -> skip ;
COMMENT : '/*' .*? '*/' -> channel(HIDDEN) ;
LINE_COMMENT : '//' ~[\r\n]* -> channel(HIDDEN) ;

INUM : [1-9][0-9]* | [0] ;
FNUM : INUM '.' [0-9]+ ;
STRING : '"' STRING_CHAR* '"' ;
STRING_CHAR : ~["\\\n] | '\\' [tn"\\] ; // All characters except newlines and backslaces, unless they are written as '\n' and '\\'. '\t' and '\"'are also an accepted characters.

NEWLINE : ('\r'? '\n' | '\r') ;


