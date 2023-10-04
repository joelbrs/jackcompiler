package br.ufma.ecp.interfaces;

public interface Statements {
    void parseStatements();
    void parseLet();
    void parseIf();
    void parseWhile();
    void parseDo();
    void parseReturn();
}
