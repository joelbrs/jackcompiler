package br.ufma.ecp;

import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;

public class Parser {
    private static class ParseError extends RuntimeException {};
    private final StringBuilder xmlOutput = new StringBuilder();
    private final VMWriter vmWriter = new VMWriter();
    private final SymbolTable symTable = new SymbolTable();
    private final Scanner scan;
    private Token currentToken;
    private Token peekToken;
    private String className;
    private int ifLabelNum = 0 ;
    private int whileLabelNum = 0;

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

                SymbolTable.Symbol sym = symTable.resolve(currentToken.getLexeme());

                if (peekTokenIs(TokenType.LPAREN) || peekTokenIs(TokenType.DOT)) {
                    parseSubRoutineCall();
                } else {
                    if (peekTokenIs(TokenType.LBRACKET)) {
                        expectPeek(TokenType.LBRACKET);
                        parseExpression();

                        if (sym != null && kind2Segment(sym.kind())!= null) {
                            vmWriter.writePush(kind2Segment(sym.kind()), sym.index());
                        }
                        vmWriter.writeArithmetic(VMWriter.Command.ADD);
                        expectPeek(TokenType.RBRACKET);

                        vmWriter.writePop(VMWriter.Segment.POINTER, 1); // pop address pointer into pointer 1
                        vmWriter.writePush(VMWriter.Segment.THAT, 0);
                    } else {
                        if (sym != null && kind2Segment(sym.kind()) != null) {
                            vmWriter.writePush(kind2Segment(sym.kind()), sym.index());
                        }
                    }
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

            var op = currentToken.getType();
            parseTerm();
            if (op == TokenType.MINUS) {
                vmWriter.writeArithmetic(VMWriter.Command.NEG);
                return;
            }
            vmWriter.writeArithmetic(VMWriter.Command.NOT);
            return;
        }

        throw error(peekToken, "term expected");
    }

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

    public void parseLet() {
        var isArray = false;

        printNonTerminal("letStatement");
        expectPeek(TokenType.LET);
        expectPeek(TokenType.IDENT);

        var symbol = symTable.resolve(currentToken.getLexeme());
        if (peekTokenIs(TokenType.LBRACKET)) {
            expectPeek(TokenType.LBRACKET);
            parseExpression();

            vmWriter.writePush(kind2Segment(symbol.kind()), symbol.index());
            vmWriter.writeArithmetic(VMWriter.Command.ADD);
            expectPeek(TokenType.RBRACKET);

            isArray = true;
        }
        expectPeek(TokenType.EQ);
        parseExpression();

        if (isArray) {
            vmWriter.writePop(VMWriter.Segment.TEMP, 0);    // push result back onto stack
            vmWriter.writePop(VMWriter.Segment.POINTER, 1); // pop address pointer into pointer 1
            vmWriter.writePush(VMWriter.Segment.TEMP, 0);   // push result back onto stack
            vmWriter.writePop(VMWriter.Segment.THAT, 0);
        } else {
            if (symbol != null && kind2Segment(symbol.kind()) != null) {
                vmWriter.writePop(kind2Segment(symbol.kind()), symbol.index());
            }
        }
        expectPeek(TokenType.SEMICOLON);
        printNonTerminal("/letStatement");
    }

    public void parseIf() {
        printNonTerminal("ifStatement");

        var labelTrue = "IF_TRUE" + ifLabelNum;
        var labelFalse = "IF_FALSE" + ifLabelNum;
        var labelEnd = "IF_END" + ifLabelNum;

        ifLabelNum++;

        expectPeek(TokenType.IF);
        expectPeek(TokenType.LPAREN);
        parseExpression();
        expectPeek(TokenType.RPAREN);

        vmWriter.writeIf(labelTrue);
        vmWriter.writeGoto(labelFalse);
        vmWriter.writeLabel(labelTrue);

        expectPeek(TokenType.LBRACE);
        parseStatements();
        expectPeek(TokenType.RBRACE);
        if (peekTokenIs(TokenType.ELSE)) {
            vmWriter.writeGoto(labelEnd);
        }

        vmWriter.writeLabel(labelFalse);

        if (peekTokenIs(TokenType.ELSE)) {
            expectPeek(TokenType.ELSE);
            expectPeek(TokenType.LBRACE);
            parseStatements();
            expectPeek(TokenType.RBRACE);
            vmWriter.writeLabel(labelEnd);
        }

        printNonTerminal("/ifStatement");
    }

    public void parseWhile() {
        printNonTerminal("whileStatement");

        var labelTrue = "WHILE_EXP" + whileLabelNum;
        var labelFalse = "WHILE_END" + whileLabelNum;
        whileLabelNum++;

        vmWriter.writeLabel(labelTrue);

        expectPeek(TokenType.WHILE);
        expectPeek(TokenType.LPAREN);
        parseExpression();

        vmWriter.writeArithmetic(VMWriter.Command.NOT);
        vmWriter.writeIf(labelFalse);

        expectPeek(TokenType.RPAREN);
        expectPeek(TokenType.LBRACE);
        parseStatements();

        vmWriter.writeGoto(labelTrue);
        vmWriter.writeLabel(labelFalse);

        expectPeek(TokenType.RBRACE);
        printNonTerminal("/whileStatement");
    }

    public void parseDo() {
        printNonTerminal("doStatement");
        expectPeek(TokenType.DO);
        expectPeek(TokenType.IDENT);
        parseSubRoutineCall();
        expectPeek(TokenType.SEMICOLON);
        printNonTerminal("/doStatement");
    }

    public void parseReturn() {
        printNonTerminal("returnStatement");
        expectPeek(TokenType.RETURN);
        if (peekTokenIs(TokenType.SEMICOLON)) {
            expectPeek(TokenType.SEMICOLON);
            vmWriter.writePush(VMWriter.Segment.CONST, 0);
            vmWriter.writeReturn();
            printNonTerminal("/returnStatement");
            return;
        }
        parseExpression();
        expectPeek(TokenType.SEMICOLON);
        vmWriter.writeReturn();
        printNonTerminal("/returnStatement");
    }

    public void parseClassVarDec() {
        while (peekTokenIs(TokenType.STATIC) || peekTokenIs(TokenType.FIELD)) {
            printNonTerminal("classVarDec");
            expectPeek(TokenType.STATIC, TokenType.FIELD);

            SymbolTable.Kind kind = SymbolTable.Kind.STATIC;
            if (currentTokenIs(TokenType.FIELD)) {
                kind = SymbolTable.Kind.FIELD;
            }
            expectPeek(TokenType.INT, TokenType.BOOLEAN, TokenType.CHAR, TokenType.IDENT);

            String type = currentToken.getLexeme();
            expectPeek(TokenType.IDENT);
            String name = currentToken.getLexeme();
            symTable.define(name, type, kind);

            while (peekTokenIs(TokenType.COMMA)) {
                expectPeek(TokenType.COMMA);
                expectPeek(TokenType.IDENT);

                name = currentToken.getLexeme();
                symTable.define(name, type, kind);
            }

            expectPeek(TokenType.SEMICOLON);
            printNonTerminal("/classVarDec");
        }
    }

    public void parseVarDec() {
        while (peekTokenIs(TokenType.VAR)) {
            printNonTerminal("varDec");
            expectPeek(TokenType.VAR);

            SymbolTable.Kind kind = SymbolTable.Kind.VAR;

            expectPeek(TokenType.INT, TokenType.CHAR, TokenType.BOOLEAN, TokenType.IDENT);
            String type = currentToken.getLexeme();

            expectPeek(TokenType.IDENT);

            //TODO: verificar
            String name = currentToken.getLexeme();

            symTable.define(name, type, kind);

            while (peekTokenIs(TokenType.COMMA)) {
                expectPeek(TokenType.COMMA);
                expectPeek(TokenType.IDENT);

                name = currentToken.getLexeme();
                symTable.define(name, type, kind);
            }

            expectPeek(TokenType.SEMICOLON);
            printNonTerminal("/varDec");
        }
    }

    public void parseSubRoutineBody(String functionName, TokenType subroutineType) {
        printNonTerminal("subroutineBody");
        expectPeek(TokenType.LBRACE);
        parseVarDec();

        var nlocals = symTable.varCount(SymbolTable.Kind.VAR);
        vmWriter.writeFunction(functionName, nlocals);

        parseStatements();
        expectPeek(TokenType.RBRACE);
        printNonTerminal("/subroutineBody");
    }

    public void parseSubRoutineDec() {
        while (peekTokenIs(TokenType.CONSTRUCTOR) || peekTokenIs(TokenType.FUNCTION) || peekTokenIs(TokenType.METHOD)) {
            printNonTerminal("subroutineDec");

            ifLabelNum = 0;
            whileLabelNum = 0;

            symTable.startSubroutine();

            expectPeek(TokenType.CONSTRUCTOR, TokenType.FUNCTION, TokenType.METHOD);

            var subroutineType = currentToken.getType();
            if (currentToken.getType() == TokenType.METHOD) {
                symTable.define("this", className, SymbolTable.Kind.ARG);
            }
            expectPeek(TokenType.VOID, TokenType.INT, TokenType.CHAR, TokenType.BOOLEAN, TokenType.IDENT);
            expectPeek(TokenType.IDENT);

            var functionName = className + "." + currentToken.getLexeme();
            expectPeek(TokenType.LPAREN);
            parseParameterList();
            expectPeek(TokenType.RPAREN);
            parseSubRoutineBody(functionName, subroutineType);
            printNonTerminal("/subroutineDec");
        }
    }

    public void parseParameterList() {
        printNonTerminal("parameterList");

        if (!peekTokenIs(TokenType.RPAREN)) {
            SymbolTable.Kind kind = SymbolTable.Kind.ARG;

            expectPeek(TokenType.INT, TokenType.CHAR, TokenType.BOOLEAN, TokenType.IDENT);
            String type = currentToken.getLexeme();

            expectPeek(TokenType.IDENT);
            String name = currentToken.getLexeme();
            symTable.define(name, type, kind);

            while (peekTokenIs(TokenType.COMMA)) {
                expectPeek(TokenType.COMMA);
                expectPeek(TokenType.INT, TokenType.CHAR, TokenType.BOOLEAN, TokenType.IDENT);
                type = currentToken.getLexeme();

                expectPeek(TokenType.IDENT);

                name = currentToken.getLexeme();
                symTable.define(name, type, kind);

            }
        }

        printNonTerminal("/parameterList");
    }

    public void parseClass() {
        printNonTerminal("class");
        expectPeek(TokenType.CLASS);
        expectPeek(TokenType.IDENT);
        className = currentToken.getLexeme();
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

    private VMWriter.Segment kind2Segment(SymbolTable.Kind kind) {
        if (kind == SymbolTable.Kind.STATIC)
            return VMWriter.Segment.STATIC;
        if (kind == SymbolTable.Kind.FIELD)
            return VMWriter.Segment.THIS;
        if (kind == SymbolTable.Kind.VAR)
            return VMWriter.Segment.LOCAL;
        if (kind == SymbolTable.Kind.ARG)
            return VMWriter.Segment.ARG;
        return null;
    }
}
