package br.ufma.ecp.interfaces;

public interface Statements {
    void parseLet();
    void parseIf();
    void parseWhile();
    void parseDo();
    void parseReturn();
}
