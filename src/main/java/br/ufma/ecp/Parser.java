package br.ufma.ecp;

import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;

public class Parser {
    private final Scanner scan;
    private Token currentToken;
    
    public Parser (byte[] input) {
        scan = new Scanner(input);
        currentToken = scan.nextToken();
    }

    public void parse () {
        expr();
    }

    void expr() {
        number();
        oper();
    }

    void number () {
        System.out.println(currentToken.getLexeme());
        match(TokenType.NUMBER);
    }

    private void nextToken () {
        currentToken = scan.nextToken();
    }

   private void match(TokenType t) {
        if (currentToken.getType() == t) {
            nextToken();
        }else {
            throw new Error("syntax error");
        }
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

    public String VMOutput() {
        return "";
    }

}
