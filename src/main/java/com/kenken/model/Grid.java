package com.kenken.model;

public class Grid {
    private final Cell [][] cells;
    private final int N;

    public Grid(int N){
        if(N<3 || N>6)
            throw new IllegalArgumentException("Il numero di righe o colonne deve essere compreso tra 3 e 6");
        this.N=N;
        cells = new Cell[N][N];
        initializeCells();
    }

    private void initializeCells(){
        for(int i=0;i<N;i++){
            for(int j=0;j<N;j++){
                this.cells[i][j]=new Cell(i,j);
            }
        }
    }

    public int getSize(){
        return this.N;
    }

    public Cell getCell(int row, int col){
        if(row<0 || row>=N || col<0 || col>=N)
            throw new IndexOutOfBoundsException("La cella non esiste");
        return this.cells[row][col];
    }

    public void printToConsole(){
        for(int i=0;i<N;i++){
            for(int j=0;j<N;j++){
                System.out.print(cells[i][j].getValue()+"\t");
            }
            System.out.println();
        }
    }

    @Override
    public String toString() {
        return "Size Grid-> "+N+"x"+N+"\n";
    }
}
