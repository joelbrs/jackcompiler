package br.ufma.ecp;

import static br.ufma.ecp.token.TokenType.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;

public class Scanner {
    private final byte[] input;
    private int current;
    private int start;
    private int line = 1;
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

    private char peek () {
        if (current < input.length) {
            return (char)input[current];
        }
        return 0;
    }

    private char peekNext() {
        int next = current + 1;

        if (next < input.length) {
            return (char) input[next];
        }
        return 0;
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

    private Token identifier() {
        while (isAlphaNumeric(peek())) advance();

        String id = new String(input, start, current-start, StandardCharsets.UTF_8)  ;
        TokenType type = keywords.get(id);
        if (type == null) type = IDENT;
        return new Token(type, id, line);
    }

    private Token number() {
        while (Character.isDigit(peek())) {
            advance();
        }

        String num = new String(input, start, current-start, StandardCharsets.UTF_8)  ;
        return new Token(NUMBER, num, line);
    }

    private Token string () {
        advance();
        start = current;
        while (peek() != '"' && peek() != 0) {
            advance();
        }
        String s = new String(input, start, current-start, StandardCharsets.UTF_8);
        Token token = new Token (TokenType.STRING,s, line);
        advance();
        return token;
    }


    private void skipWhitespace() {
        char ch = peek();
        while (ch == ' ' || ch == '\r' || ch == '\t' || ch == '\n') {
            if (ch == '\n') {
                line++;
            }

            advance();
            ch = peek();
        }
    }

    private void skipLineComments() {
        while (peek() != '\n' && peek() != 0) {
            advance();
        }
        if (peek() == '\n') {
            line++;
            advance();
        }
    }

    private void skikBlockComments() {
        boolean endComment = Boolean.FALSE;
        advance();

        while (!endComment) {
            char ch = peek();

            if (ch == '\n') {
                line++;
            }

            if (ch == 0) {
                System.exit(1);
            }

            advance();
            if (ch == '*') {
                while (peek() == '*') {
                    advance();
                }

                if (peek() == '/') {
                    endComment = Boolean.TRUE;
                    advance();
                }
            }
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
            return new Token(EOF, "EOF", line);
        }

        for (TokenType token : TokenType.values()) {
            if (token.getType() != null && ch == (char) token.getType()) {
                if ((char) token.getType() == '/') {
                    if (peekNext() == '/') {
                        skipLineComments();
                        return nextToken();
                    }

                    if (peekNext() == '*') {
                        skikBlockComments();
                        return nextToken();
                    }
                }

                advance();
                return new Token(token, token.getType().toString(), line);
            }
        }
        return new Token(ILLEGAL, Character.toString(ch), line);
    }
}
