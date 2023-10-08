package br.ufma.ecp.token;

import java.util.List;

public enum TokenType {
     // literals
     NUMBER("integerConstant"),
     STRING("stringConstant"),
     IDENT("identifier"),

     // symbols
     LPAREN("("), RPAREN(")"),
     LBRACE("{"), RBRACE("}"),
     LBRACKET("["), RBRACKET("]"),
     COMMA(","), SEMICOLON(";"), DOT("."),
     PLUS("+"), MINUS("-"), ASTERISK("*"), SLASH("/"),
     AND("&"), OR("|"), NOT("~"),
     LT("<"), GT(">"), EQ("="),

     // keywords
     METHOD, WHILE, IF, CLASS, CONSTRUCTOR, FUNCTION, FIELD, STATIC, RETURN,
     VAR, INT, CHAR, BOOLEAN, VOID, TRUE, FALSE, NULL, THIS, LET, DO, ELSE,

     EOF,

     ILLEGAL;

     String type;

     TokenType() {}

     TokenType(String c) {
          type = c;
     }

     private static final List<TokenType> booleanTokens = List.of(
             TRUE,
             FALSE,
             NULL
     );

     private static final List<TokenType> literalsTokens = List.of(
             NUMBER,
             STRING,
             IDENT,
             THIS
     );

     private static final List<TokenType> keywordsTokens = List.of(
             METHOD,
             WHILE,
             IF,
             CLASS,
             CONSTRUCTOR,
             FUNCTION,
             FIELD,
             STATIC,
             VAR,
             INT,
             CHAR,
             BOOLEAN,
             VOID,
             TRUE,
             FALSE,
             NULL,
             THIS,
             LET,
             DO,
             ELSE,
             RETURN
     );

     private static final List<TokenType> statementTokens = List.of(
          LET,
          IF,
          WHILE,
          RETURN,
          DO
     );

     public Object getType() {
          if (type != null) {
               return type;
          }
          return null;
     }

     static public boolean isSymbol(char c) {
        String symbols = "{}()[].,;+-*/&|<>=~";
        return symbols.indexOf(c) > -1;
     }

     static public boolean isKeyword(TokenType type) {
        return keywordsTokens.contains(type);
     }

     static public boolean isLiteral(TokenType type) { return literalsTokens.contains(type); }

     static public boolean isBoolean(TokenType type) {
         return booleanTokens.contains(type);
    }

     static public boolean isStatement(TokenType type) { return statementTokens.contains(type); }
}


