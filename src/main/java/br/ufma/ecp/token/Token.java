package br.ufma.ecp.token;
public class Token {

    private final TokenType type;
    private final String lexeme;

    private final int line;

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public Integer getLine() {
        return line;
    }

    public Token (TokenType type, String lexeme, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }

    public String toString() {
        var type = this.type.toString();

        /**
        if (TokenType.isSymbol(lexeme.charAt(0)))
            type = "symbol";

        if (TokenType.isKeyword(this.type))
            type = "keyword";
        **/

        return "<"+ type +">" + lexeme + "</"+ type + ">";
    }
    
}
