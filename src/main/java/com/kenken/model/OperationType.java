package com.kenken.model;

public enum OperationType {
    ADD("+"), SUB("-"), MUL("x"), DIV("/"), NONE("");

    private final String symbol;

    private OperationType(String symbol){
        this.symbol = symbol;
    }

    public String getSymbol(){
        return this.symbol;
    }

    @Override
    public String toString() {
        return name();
    }
}
