package com.kenken.model;

public class Cell {
    private final int row,col;
    private int value;
    private boolean isEditable;
    private Cage parentCage;

    public Cell(int row, int col){
        this.row = row;
        this.col = col;
        this.value = 0;
        this.isEditable = true;
        parentCage = null;
    }

    public Cell(int row, int col, int initialValue, boolean isEditable){
        this.row=row;
        this.col=col;
        this.value = initialValue;
        this.isEditable = isEditable;
        this.parentCage = null;
    }

    public int getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }

    public int getValue() {
        return this.value;
    }

    public boolean isEmpty(){
        return this.value==0;
    }

    public boolean isEditable() {
        return this.isEditable;
    }

    public Cage getParentCage() {
        return this.parentCage;
    }

    public void setValue(int value) {
        if(!this.isEditable)
            throw new IllegalStateException("Impossibile modificare il valore di una cella non editabile");
        else
            this.value = value;
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

   public void setParentCage(Cage parentCage) {
        this.parentCage = parentCage;
    }

    public void clearValue(){
        if(this.isEditable)
            this.value=0;
    }

    @Override
    public String toString(){
        String editStr= isEditable ? " Y ":" N ";
        String cageStr= parentCage==null ? " - ":" "+parentCage.hashCode()+" ";
        return "["+row+","+col+"]"+editStr+cageStr+value;
    }

    @Override
    public boolean equals(Object obj){
        if(obj==null)
            return false;
        if(obj==this)
            return true;
        if(!(obj instanceof Cell))
            return false;
        Cell other = (Cell)obj;
        return this.row==other.row && this.col==other.col;
    }

    @Override
    public int hashCode(){
        return 31*this.row+this.col;
    }

}
