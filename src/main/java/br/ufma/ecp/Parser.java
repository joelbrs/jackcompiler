package br.ufma.ecp;

import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;

public class Parser {
    private static class ParseError extends RuntimeException {};
    private final Scanner scan;
    private Token currentToken;
    private Token peekToken;
    private StringBuilder xmlOutput = new StringBuilder();

    public Parser (byte[] input) {
        scan = new Scanner(input);
        nextToken();
    }

    void number () {
        System.out.println(currentToken.getLexeme());
        match(TokenType.NUMBER);
    }

    private void nextToken () {
        currentToken = peekToken;
        peekToken = scan.nextToken();
    }

    private void match(TokenType t) {
        if (currentToken.getType() == t) {
            nextToken();
            return;
        }

        throw new Error("syntax error");
    }

    static public boolean isOperator(String op) {
        return "+=*/<>=~&|".contains(op);
    }

    void oper () {
        if (currentToken.getType() == TokenType.PLUS) {
            match(TokenType.PLUS);
            number();
            System.out.println("add");
            oper();
        } else if (currentToken.getType() == TokenType.MINUS) {
            match(TokenType.MINUS);
            number();
            System.out.println("sub");
            oper();
        } else {
            throw new Error("syntax error");
        }
    }

    void parseTerm() {
        printNonTerminal("term");
        switch (peekToken.getType()) {
            case NUMBER:
                expectPeek(TokenType.NUMBER);
                break;
            case STRING:
                expectPeek(TokenType.STRING);
                break;
            case FALSE:
            case NULL:
            case TRUE:
                expectPeek(TokenType.FALSE, TokenType.NULL, TokenType.TRUE);
                break;
            case THIS:
                expectPeek(TokenType.THIS);
                break;
            case IDENT:
                expectPeek(TokenType.IDENT);
                break;
            default:
                throw error(peekToken, "term expected");
        }
        printNonTerminal("/term");
    }

    void parseExpression() {
        printNonTerminal("expression");
        parseTerm();
        while (isOperator(peekToken.getLexeme())) {
            expectPeek(peekToken.getType());
            parseTerm();
        }
        printNonTerminal("/expression");
    }

    void parseLet() {
        printNonTerminal("letStatement");
        expectPeek(TokenType.LET);
        expectPeek(TokenType.IDENT);

        if (peekTokenIs(TokenType.LBRACKET)) {
            expectPeek(TokenType.LBRACKET);
            parseExpression();
            expectPeek(TokenType.RBRACKET);
        }

        expectPeek(TokenType.EQ);
        parseExpression();
        expectPeek(TokenType.SEMICOLON);
        printNonTerminal("/letStatement");
    }

    private void expectPeek(TokenType... types) {
        for (TokenType type: types) {
            if (peekToken.getType() == type) {
                expectPeek(type);
                return;
            }
        }
        throw error(peekToken, "Expected a statement");
    }

    private void expectPeek(TokenType type) {
        if (peekToken.getType() == type) {
            nextToken();
            xmlOutput.append(String.format("%s\r\n", currentToken.toString()));
            return;
        }
        throw error(peekToken, "Expected " + type.name());
    }

    public String XMLOutput() {
        return xmlOutput.toString();
    }

    private void printNonTerminal(String nterminal) {
        xmlOutput.append(String.format("<%s>\r\n", nterminal));
    }

    boolean peekTokenIs(TokenType type) {
        return peekToken.getType() == type;
    }

    boolean currentTokenIs(TokenType type) {
        return currentToken.getType() == type;
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + " ] Error" + where + ": " + message);
    }

    private ParseError error(Token token, String message) {
        if (token.getType() == TokenType.EOF) {
            report(token.getLine(), "at end", message);
        } else {
            report(token.getLine(), "at '" + token.getLexeme() + "'", message);
        }
        return new ParseError();
    }
}
