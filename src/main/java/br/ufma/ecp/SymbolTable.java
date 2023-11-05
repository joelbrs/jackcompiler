package br.ufma.ecp;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    public enum Kind {
        STATIC, FIELD, ARG, VAR
    };

    public static record Symbol(String name, String type, Kind kind, int index) {
    }

    private final Map<String, Symbol> classScope = new HashMap<>();
    private final Map<String, Symbol> subroutineScope = new HashMap<>();
    private final Map<Kind, Integer> countVars = new HashMap<>();

    public SymbolTable() {
        countVars.put(Kind.ARG, 0);
        countVars.put(Kind.VAR, 0);
        countVars.put(Kind.STATIC, 0);
        countVars.put(Kind.FIELD, 0);
    }

    public void startSubroutine() {
        subroutineScope.clear();
        countVars.put(Kind.ARG, 0);
        countVars.put(Kind.VAR, 0);
    }

    private Map<String,Symbol> scope (Kind kind) {
        if (kind == Kind.STATIC || kind == Kind.FIELD){
            return classScope;
        }
        return subroutineScope;
    }

    void define(String name, String type, Kind kind) {
        Map<String,Symbol> scopeTable = scope(kind);
        if (scopeTable.get(name) != null) return;

        Symbol s = new Symbol(name, type, kind, varCount(kind));
        scopeTable.put(name, s);

        countVars.put(kind, countVars.get(kind) + 1);
    }

    public Symbol resolve (String name) {
        return subroutineScope.get(name) != null ? subroutineScope.get(name) : classScope.get(name);
    }

    int varCount(Kind kind) {
        return countVars.get(kind);
    }
}
