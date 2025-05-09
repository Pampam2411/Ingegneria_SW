package com.kenken.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Cage {

    private final int targetValue;
    private final OperationType operationType;
    private List<Cell> cellsInCage;
    private final int cageId;
    private static int nextCageId = 1;

    public Cage(int targetValue, OperationType operationType) {
        if (operationType == null) throw new IllegalArgumentException("Operatore nullo");
        this.targetValue = targetValue;
        this.operationType = operationType;
        this.cellsInCage = new ArrayList<>();
        this.cageId = nextCageId++;
    }

    public int getTargetValue() {
        return this.targetValue;
    }

    public OperationType getOperationType() {
        return this.operationType;
    }

    public List<Cell> getCellsInCage() {
        return this.cellsInCage;
    }

    public int getCageId() {
        return this.cageId;
    }

    public void addCell(Cell cell) {
        if (cell == null)
            throw new IllegalArgumentException("La cella non può essere nulla");
        else if (this.cellsInCage.contains(cell))
            throw new IllegalArgumentException("La cella è già presente nella gabbia");
            else {
            this.cellsInCage.add(cell);
            cell.setParentCage(this);
        }
    }

    public boolean checkConstraint() {
        if (this.cellsInCage.isEmpty() || operationType == null)
            return false;

        for (Cell c : this.cellsInCage)
            if (c.isEmpty()) return false;

        //creo uno stream dalla lista di celle che ho, poi li mappo prendendo tutti i valori interi da ogni cella, infine li restituisco sotto forma di lista di Integer
        List<Integer> values = this.cellsInCage.stream().map(Cell::getValue).toList();

        switch (operationType) {
            case NONE:
                return cellsInCage.size() == 1 && values.get(0) == targetValue;

            case ADD: {
                int sum = 0;
                for (Integer val : values)
                    sum += val;
                return sum == targetValue;
            }

            case SUB: {
                if (values.size() != 2)
                    throw new IllegalArgumentException("La sottrazione non è definita per gabbie con più di due celle");
                return Math.abs(values.get(0) - values.get(1)) == targetValue;
            }

            case MUL: {
                long prod = 1;
                for (Integer val : values)
                    prod *= val;
                return prod == targetValue;
            }

            case DIV: {
                if (values.size() != 2)
                    throw new IllegalArgumentException("La divisione non è definita per gabbie con più di due celle");
                int v1 = values.get(0), v2 = values.get(1);
                if (v1 == 0 || v2 == 0) {
                    throw new IllegalArgumentException("Impossibile eseguire la divisione per zero");
                } else {
                    boolean check1 = (v1 > v2) && (v1 % v2 == 0) && (v1 / v2 == targetValue);
                    boolean check2 = (v2 > v1) && (v2 % v1 == 0) && (v2 / v1 == targetValue);
                    boolean check3 = (v1 == v2) && targetValue == 1;
                    return (check1 || check2 || check3);
                }
            }

            default:
                throw new IllegalArgumentException("Operazione non supportata");
        }
    }

    @Override
    public String toString() {
        return "Cage{" +
                "cageId=" + cageId +
                ", targetValue=" + targetValue +
                ", operationType=" + operationType.getSymbol() +
                ", cellsInCage=" + cellsInCage.size() +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof Cage))
            return false;
        Cage other = (Cage) obj;
        return this.cageId == other.cageId;
    }

    @Override
    public int hashCode() {
        return this.cageId;
    }
}
