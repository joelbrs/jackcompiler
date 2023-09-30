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
        var category = type.getType() != null ? type.getType().toString() : type.toString().toLowerCase();

        var value = lexeme;

        if (TokenType.isKeyword(type)) {
            category = "keyword";
        }

        if (TokenType.isSymbol(lexeme.charAt(0))) {
            category = "symbol";

            if (value.equals(">")) {
                value = "&gt;";
            }

            if (value.equals("<")) {
                value = "&lt;";
            }

            if (value.equals("\"")) {
                value = "&quote";
            }

            if (value.equals("&")) {
                value = "&amp;";
            }
        }

        return "<" + category + "> " + value + " </" + category + ">";
    }
}
