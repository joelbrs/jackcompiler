package br.ufma.ecp;

import br.ufma.ecp.interfaces.SyntacticElements;
import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;

public class Parser implements SyntacticElements {
    private static class ParseError extends RuntimeException {};
    private final Scanner scan;
    private Token currentToken;
    private Token peekToken;
    private final StringBuilder xmlOutput = new StringBuilder();
    private final VMWriter vmWriter = new VMWriter();

    public Parser (byte[] input) {
        scan = new Scanner(input);
        nextToken();
    }

    private void nextToken () {
        currentToken = peekToken;
        peekToken = scan.nextToken();
    }

    static public boolean isOperator(String op) {
        return "+=*/<>-~&|.".contains(op);
    }

    @Override
    public void parseTerm() {
        printNonTerminal("term");
        boolean currentIsUnaryOrRParen = currentToken != null && (currentTokenIs(TokenType.NOT) || currentTokenIs(TokenType.MINUS) || currentTokenIs(TokenType.RPAREN));

        if (TokenType.isLiteral(peekToken.getType())) {
            if (peekTokenIs(TokenType.NUMBER)) {
                vmWriter.writePush(VMWriter.Segment.CONST, Integer.parseInt(peekToken.getLexeme()));
            }

            if (peekTokenIs(TokenType.STRING)) {
                var strValue = peekToken.getLexeme();
                vmWriter.writePush(VMWriter.Segment.CONST, strValue.length());
                vmWriter.writeCall("String.new", 1);
                for (int i = 0; i < strValue.length(); i++) {
                    vmWriter.writePush(VMWriter.Segment.CONST, strValue.charAt(i));
                    vmWriter.writeCall("String.appendChar", 2);
                }
            }

            if (peekTokenIs(TokenType.THIS)) {
                vmWriter.writePush(VMWriter.Segment.POINTER, 0);
            }

            if (peekTokenIs(TokenType.IDENT)) {
                expectPeek(TokenType.IDENT);

                if (peekTokenIs(TokenType.LPAREN) || peekTokenIs(TokenType.DOT)) {
                    parseSubRoutineCall();
                }

                printNonTerminal("/term");

                if (currentIsUnaryOrRParen) {
                    printNonTerminal("/term");
                }
                return;
            }

            expectPeek(peekToken.getType());
            printNonTerminal("/term");
            return;
        }

        if (TokenType.isBoolean(peekToken.getType())) {
            expectPeek(TokenType.FALSE, TokenType.NULL, TokenType.TRUE);

            vmWriter.writePush(VMWriter.Segment.CONST, 0);
            if (currentToken.getType() == TokenType.TRUE)
                vmWriter.writeArithmetic(VMWriter.Command.NOT);

            printNonTerminal("/term");
            return;
        }

        if (peekTokenIs(TokenType.LPAREN)) {
            expectPeek(TokenType.LPAREN);
            parseExpression();
            expectPeek(TokenType.RPAREN);
            printNonTerminal("/term");

            if (currentIsUnaryOrRParen) {
                printNonTerminal("/term");
            }
            return;
        }

        if (peekTokenIs(TokenType.NOT) || peekTokenIs(TokenType.MINUS)) {
            expectPeek(peekToken.getType());
            parseTerm();
            return;
        }

        throw error(peekToken, "term expected");
    }

    @Override
    public void parseSubRoutineCall() {
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
    }

    @Override
    public void parseExpressionList() {
        printNonTerminal("expressionList");
        if (!peekTokenIs(TokenType.RPAREN)) {
            parseExpression();
        }
        while (peekTokenIs(TokenType.COMMA)) {
            expectPeek(TokenType.COMMA);
            parseExpression();
        }
        printNonTerminal("/expressionList");
    }

    @Override
    public void parseStatements() {
            printNonTerminal("statements");
            while (TokenType.isStatement(peekToken.getType())) {
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

                if (peekTokenIs(TokenType.DO)) {
                    parseDo();
                }
            }
            printNonTerminal("/statements");
    }

    @Override
    public void parseExpression() {
        printNonTerminal("expression");
        parseTerm();

        while (isOperator(peekToken.getLexeme())) {
            var ope = peekToken.getType();
            expectPeek(peekToken.getType());
            parseTerm();
            compileOperators(ope);
        }
        printNonTerminal("/expression");
    }

    @Override
    public void parseLet() {
        printNonTerminal("letStatement");
        expectPeek(TokenType.LET);
        expectPeek(TokenType.IDENT);
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
        parseStatements();
        expectPeek(TokenType.RBRACE);
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
        expectPeek(TokenType.IDENT);
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
        while (peekTokenIs(TokenType.STATIC) || peekTokenIs(TokenType.FIELD)) {
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
    }

    @Override
    public void parseVarDec() {
        while (peekTokenIs(TokenType.VAR)) {
            printNonTerminal("varDec");
            expectPeek(TokenType.VAR);
            expectPeek(TokenType.INT, TokenType.CHAR, TokenType.BOOLEAN, TokenType.IDENT);
            expectPeek(TokenType.IDENT);
            expectPeek(TokenType.SEMICOLON);
            printNonTerminal("/varDec");
        }
    }

    @Override
    public void parseSubRoutineBody() {
        printNonTerminal("subroutineBody");
        expectPeek(TokenType.LBRACE);
        parseVarDec();
        parseStatements();
        expectPeek(TokenType.RBRACE);
        printNonTerminal("/subroutineBody");
    }

    @Override
    public void parseSubRoutineDec() {
        while (peekTokenIs(TokenType.CONSTRUCTOR) || peekTokenIs(TokenType.FUNCTION) || peekTokenIs(TokenType.METHOD)) {
            printNonTerminal("subroutineDec");
            expectPeek(TokenType.CONSTRUCTOR, TokenType.FUNCTION, TokenType.METHOD);
            expectPeek(TokenType.VOID, TokenType.INT, TokenType.CHAR, TokenType.BOOLEAN, TokenType.IDENT);
            expectPeek(TokenType.IDENT);
            expectPeek(TokenType.LPAREN);
            parseParameterList();
            expectPeek(TokenType.RPAREN);
            parseSubRoutineBody();
            printNonTerminal("/subroutineDec");
        }
    }

    @Override
    public void parseParameterList() {
        printNonTerminal("parameterList");
        if (peekTokenIs(TokenType.INT) || peekTokenIs(TokenType.CHAR) || peekTokenIs(TokenType.BOOLEAN) || peekTokenIs(TokenType.IDENT)) {
            expectPeek(TokenType.INT, TokenType.CHAR, TokenType.BOOLEAN, TokenType.IDENT);
            expectPeek(TokenType.IDENT);

            while (peekTokenIs(TokenType.COMMA)) {
                expectPeek(TokenType.COMMA);
                expectPeek(TokenType.INT, TokenType.CHAR, TokenType.BOOLEAN, TokenType.IDENT);
                expectPeek(TokenType.IDENT);
            }
        }

        printNonTerminal("/parameterList");
    }

    @Override
    public void parseClass() {
        printNonTerminal("class");
        expectPeek(TokenType.CLASS);
        expectPeek(TokenType.IDENT);
        expectPeek(TokenType.LBRACE);
        parseClassVarDec();
        parseSubRoutineDec();
        expectPeek(TokenType.RBRACE);
        printNonTerminal("/class");
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
            report(token.getLine(), " at end", message);
        }

        if (token.getType() != TokenType.EOF) {
            report(token.getLine(), " at '" + token.getLexeme() + "'", message);
        }
        return new ParseError();
    }

    public String VMOutput() {
        return vmWriter.vmOutput();
    }

    public void compileOperators(TokenType type) {

        if (type == TokenType.ASTERISK) {
            vmWriter.writeCall("Math.multiply", 2);
        } else if (type == TokenType.SLASH) {
            vmWriter.writeCall("Math.divide", 2);
        } else {
            vmWriter.writeArithmetic(typeOperator(type));
        }
    }

    private VMWriter.Command typeOperator(TokenType type) {
        if (type == TokenType.PLUS)
            return VMWriter.Command.ADD;
        if (type == TokenType.MINUS)
            return VMWriter.Command.SUB;
        if (type == TokenType.LT)
            return VMWriter.Command.LT;
        if (type == TokenType.GT)
            return VMWriter.Command.GT;
        if (type == TokenType.EQ)
            return VMWriter.Command.EQ;
        if (type == TokenType.AND)
            return VMWriter.Command.AND;
        if (type == TokenType.OR)
            return VMWriter.Command.OR;
        return null;
    }
}
