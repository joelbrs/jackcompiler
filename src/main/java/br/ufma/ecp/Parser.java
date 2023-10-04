package br.ufma.ecp;

import br.ufma.ecp.interfaces.Expressions;
import br.ufma.ecp.interfaces.Statements;
import br.ufma.ecp.interfaces.SyntacticElements;
import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;

public class Parser implements Expressions, Statements, SyntacticElements {
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

    private void match(TokenType type) {
        if (currentToken.getType() == type) {
            nextToken();
            return;
        }

        throw new Error("syntax error");
    }

    static public boolean isOperator(String op) {
        return "+=*/<>=~&|".contains(op);
    }

    @Override
    public void parseTerm() {
        printNonTerminal("term");
        if (TokenType.isLiteral(peekToken.getType())) {
            expectPeek(peekToken.getType());
            printNonTerminal("/term");
            return;
        }

        if (TokenType.isBoolean(peekToken.getType())) {
            expectPeek(TokenType.FALSE, TokenType.NUMBER, TokenType.TRUE);
            printNonTerminal("/term");
            return;
        }

        throw error(peekToken, "term expected");
    }

    @Override
    public void parseSubRoutineCall() {
        printNonTerminal("subRoutineCall");
        expectPeek(TokenType.IDENT);

        if(peekTokenIs(TokenType.LPAREN)) {
            expectPeek(TokenType.LPAREN);
            parseExpressionList();
            expectPeek(TokenType.RPAREN);
            return;
        }

        expectPeek(TokenType.DOT);
        expectPeek(TokenType.IDENT);
        expectPeek(TokenType.LPAREN);
        parseExpressionList();
        expectPeek(TokenType.RPAREN);
        printNonTerminal("/subRoutineCall");
    }

    @Override
    public void parseExpressionList() {
        printNonTerminal("expressionList");
        parseExpression();
        if (peekTokenIs(TokenType.COMMA)) {
            expectPeek(TokenType.COMMA);
            parseExpression();
        }
        printNonTerminal("/expressionList");
    }

    @Override
    public void parseStatements() {
            if (peekTokenIs(TokenType.LET)) {
                parseLet();
            }

            if (peekTokenIs(TokenType.IF)) {
                parseIf();
            }

            if (peekTokenIs(TokenType.WHILE)) {
                parseWhile();
            }

            if (peekTokenIs(TokenType.RETURN)) {
                parseReturn();
            }
           //TODO: completar quando os outros métodos estiverem prontos
    }

    @Override
    public void parseExpression() {
        printNonTerminal("expression");
        parseTerm();

        while (isOperator(peekToken.getLexeme())) {
            expectPeek(peekToken.getType());
            parseTerm();
        }
        printNonTerminal("/expression");
    }

    @Override
    public void parseLet() {
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

    @Override
    public void parseIf() {
        printNonTerminal("ifStatement");
        expectPeek(TokenType.IF);
        expectPeek(TokenType.LPAREN);
        parseExpression();
        expectPeek(TokenType.RPAREN);
        expectPeek(TokenType.LBRACE);
        if (!peekTokenIs(TokenType.RBRACE)) {
            parseStatements();
        }
        expectPeek(TokenType.RBRACE);

        if(peekTokenIs(TokenType.ELSE)) {
            expectPeek(TokenType.ELSE);
            expectPeek(TokenType.LBRACE);
            if (!peekTokenIs(TokenType.RBRACE)) {
                parseStatements();
            }
            expectPeek(TokenType.RBRACE);
        }
        printNonTerminal("/ifStatement");
    }

    @Override
    public void parseWhile() {
        printNonTerminal("whileStatement");
        expectPeek(TokenType.WHILE);
        expectPeek(TokenType.LPAREN);
        parseExpression();
        expectPeek(TokenType.RPAREN);
        expectPeek(TokenType.LBRACE);
        parseStatements();
        expectPeek(TokenType.RBRACE);
        printNonTerminal("/whileStatement");
    }

    @Override
    public void parseDo() {
        printNonTerminal("doStatement");
        expectPeek(TokenType.DO);
        parseSubRoutineCall();
        expectPeek(TokenType.SEMICOLON);
        printNonTerminal("/doStatement");
    }

    @Override
    public void parseReturn() {
        printNonTerminal("returnStatement");
        expectPeek(TokenType.RETURN);
        if (peekTokenIs(TokenType.SEMICOLON)) {
            expectPeek(TokenType.SEMICOLON);
            printNonTerminal("/returnStatement");
            return;
        }
        parseExpression();
        expectPeek(TokenType.SEMICOLON);
        printNonTerminal("/returnStatement");
    }

    @Override
    public void parseClassVarDec() {
        printNonTerminal("classVarDec");

        expectPeek(TokenType.STATIC, TokenType.FIELD);
        expectPeek(TokenType.INT, TokenType.BOOLEAN, TokenType.CHAR, TokenType.IDENT);
        expectPeek(TokenType.IDENT);

        while (peekTokenIs(TokenType.COMMA)) {
            expectPeek(TokenType.COMMA);
            expectPeek(TokenType.IDENT);
        }

        expectPeek(TokenType.SEMICOLON);
        printNonTerminal("/classVarDec");
    }

    @Override
    public void parseVarDec() {
        printNonTerminal("varDec");
        expectPeek(TokenType.VAR);
        expectPeek(TokenType.INT, TokenType.CHAR, TokenType.BOOLEAN, TokenType.IDENT);
        expectPeek(TokenType.IDENT);

        while (peekTokenIs(TokenType.COMMA)) {
            expectPeek(TokenType.COMMA);
            expectPeek(TokenType.IDENT);
        }
        expectPeek(TokenType.SEMICOLON);
        printNonTerminal("/varDec");
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
        }

        if (token.getType() != TokenType.EOF) {
            report(token.getLine(), " at '" + token.getLexeme() + "'", message);
        }
        return new ParseError();
    }
}
