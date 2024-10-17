grammar simpleC;

@header {
    import java.io.FileWriter;
    import java.io.IOException;
    import java.util.ArrayList;
    import java.util.List;
}

@members {
    FileWriter writer;
    int block_count_successors = 0;
    int block_count_predecessors = 0;
    List<List<String>> stringList_predecessors = new ArrayList<>(); // String 리스트 선언
    List<List<String>> stringList_successors = new ArrayList<>(); 
    
    void openFile() {
        try {
            writer = new FileWriter("cfg.out");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void closeFile() {
        try {
            if (writer != null) writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void writeToFile(String content) {
        try {
            if (writer != null) writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Lexer 규칙
INT     : 'int';
IF      : 'if';
ELSE    : 'else';
ELIF    : 'else if';
WHILE   : 'while';
RETURN  : 'return';
LT      : '<';
EQ      : '=';
PLUS    : '+';
SEMICOLON : ';';
COMMA   : ',';
LPAREN  : '(';
RPAREN  : ')';
LBRACE  : '{';
RBRACE  : '}';
ID      : [a-zA-Z_][a-zA-Z_0-9]*;
NUMBER  : [0-9]+;
COMMENT : '//' ~[\r\n]* -> skip; // 한 줄 주석 무시
WS      : [ \t\r\n]+ -> skip; // 공백 무시

// Parser 규칙
program : func;

func
    : INT ID LPAREN param_list RPAREN inblock
    ;

param_list: param (',' param)* | ;
param: INT ID ;

stmt
    : decl+
    | decl_with_if
    | assign+
    | assign_with_if
    | whileStmt
    | returnStmt
    | (ELSE LBRACE decl+ RBRACE)
    | else_assign
    | (ELSE LBRACE decl* assign* RBRACE)
    | decl_with_brace
    | assign_with_else
    ;

assign_with_else
    : (LBRACE assign+ RBRACE ELSE assign_with_brace_in_else)
    ;

assign_with_brace_in_else
    : (LBRACE assign+ RBRACE)
    ;

assign_with_brace_in_while
    : (LBRACE assign+ RBRACE)
    ;

decl_with_brace
    : (LBRACE decl+ RBRACE)
    ;

decl_with_if
    : (decl+ ifStmt)
    ;

assign_with_if
    : (assign+ ifStmt)
    ;

else_assign
    : ELSE LBRACE assign+ RBRACE
    ;

whileStmt
    : (WHILE LPAREN expr RPAREN assign_with_brace_in_while)
    ;


inblock 
    : LBRACE
      stmt*
      RBRACE
    ;

block 
    : LBRACE
      stmt*
      RBRACE
    ;
    


decl
    : INT varDeclList SEMICOLON
    ;

varDeclList
    : fisrtID=ID (COMMA nextIDs+=ID)* (EQ expr)?
    ;

assign
    : ID EQ expr SEMICOLON
    ;

ifStmt
    : IF LPAREN expr RPAREN 
    ;


returnStmt
    : RETURN expr SEMICOLON
    ;

expr
    : ID
    | NUMBER
    | expr PLUS expr
    | expr LT expr 
    ;
