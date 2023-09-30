package br.ufma.ecp;

import static br.ufma.ecp.token.TokenType.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;

public class Scanner {

    private final byte[] input;
    private int current;
    private int start;

    private static final Map<String, TokenType> keywords;
 

    static {
        keywords = new HashMap<>();

        for (TokenType token : TokenType.values()) {
            if (TokenType.isKeyword(token)) {
                keywords.put(token.toString().toLowerCase(), token);
            }
        }
    }

    
    public Scanner (byte[] input) {
        this.input = input;
        current = 0;
        start = 0;
    }

    private void skipWhitespace() {
        char ch = peek();
        while (ch == ' ' || ch == '\r' || ch == '\t' || ch == '\n') {
            advance();
            ch = peek();
        }
    }
    

    public Token nextToken () {
        skipWhitespace();

        start = current;
        char ch = peek();

        if (Character.isDigit(ch)) {
            return number();
        }

        if (isAlpha(ch)) {
            return identifier();
        }

        if (ch == '"') {
            return string();
        }

        if (ch == 0) {
            return new Token(EOF, "EOF");
        }

        for (TokenType token : TokenType.values()) {
            if (token.getType() != null && ch == (char) token.getType()) {
                advance();
                return new Token(token, token.getType().toString());
            }
        }
        return new Token(ILLEGAL, Character.toString(ch));
    }

    private Token identifier() {
        while (isAlphaNumeric(peek())) advance();

        String id = new String(input, start, current-start, StandardCharsets.UTF_8)  ;
        TokenType type = keywords.get(id);
        if (type == null) type = IDENT;
        return new Token(type, id);
    }

    private Token number() {
        while (Character.isDigit(peek())) {
            advance();
        }
        
        String num = new String(input, start, current-start, StandardCharsets.UTF_8)  ;
        return new Token(NUMBER, num);
    }

    private Token string () {
        advance();
        start = current;
        while (peek() != '"' && peek() != 0) {
            advance();
        }
        String s = new String(input, start, current-start, StandardCharsets.UTF_8);
        Token token = new Token (TokenType.STRING,s);
        advance();
        return token;
    }

    private void advance()  {
        char ch = peek();
        if (ch != 0) {
            current++;
        }
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
      }
    
      private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || Character.isDigit((c));
      }
    

    private char peek () {
        if (current < input.length)
           return (char)input[current];
       return 0;
    }
}
