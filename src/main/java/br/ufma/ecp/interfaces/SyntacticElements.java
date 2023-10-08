package br.ufma.ecp.interfaces;

public interface SyntacticElements {
    void parseExpression();
    void parseTerm();
    void parseSubRoutineCall();
    void parseExpressionList();
    void parseStatements();
    void parseLet();
    void parseIf();
    void parseWhile();
    void parseDo();
    void parseReturn();
    void parseClassVarDec();
    void parseVarDec();
    void parseSubRoutineBody();
    void parseSubRoutineDec();
    void parseParameterList();
    void parseClass();
}
