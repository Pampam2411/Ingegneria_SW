package com.kenken.solver;

import com.kenken.model.Cage;
import com.kenken.model.Cell;
import com.kenken.model.Grid;

import java.util.ArrayList;
import java.util.List;

public class KenKenSolver {
    private Grid workingGrid;
    private List<Cage> originalCages;
    private int N;
    private List<Grid> solutions; //lista delle soluzioni trovate
    private int maxSolutionsToFind; //numero massimo di soluzioni da trovare

    public KenKenSolver(Grid grid, List<Cage> cages, int N){
        this.N=N;
        this.workingGrid=createWorkingGrid(grid,N);
        this.originalCages =cages;
    }

    //Questo metodo serve a creare una griglia copia per non andare a modificare la griglia originale
    private Grid createWorkingGrid(Grid grid, int N){
        Grid copy= new Grid(N);
        for(int i=0;i<N;i++){
            for(int j=0;j<N;j++){
                Cell originalCell=grid.getCell(i,j); //cella griglia originale
                Cell copyCell=copy.getCell(i,j); //cella griglia copia
                copyCell.setValue(originalCell.getValue()); // imposto valore cella copia con valore cella originale
                copyCell.setEditable(originalCell.isEditable()); //imposto editabilità cella copia con editabilità cella originale

                copyCell.setParentCage(originalCell.getParentCage());// imposto gabbia cella copia con gabbia cella originale
            }
        }
        return copy;
    }

    public List<Grid> solve(int maxSolutionsToFind){
        this.solutions=new ArrayList<>();
        this.maxSolutionsToFind=maxSolutionsToFind;
        if(this.maxSolutionsToFind==0)return this.solutions;

        findSolutionRecursive(0,0);
        return this.solutions;
    }

    private boolean findSolutionRecursive(int row, int col){
        if(this.solutions.size()>=this.maxSolutionsToFind && this.maxSolutionsToFind>0) return true;

        Cell nextEmptyCell=findNextEditableEmptyCell(row,col);

        //Se non ci sono più celle vuote allora la griglia sarà completa, e devo verificare che sia valida.
        //Se è valida la return è true solo se ho raggiunto le soluzioni da trovare, altrimenti se non è valida return false.
        if(nextEmptyCell==null){
            if(isCurrentGridSolutionValid()){
                Grid solutionToAdd=new Grid(this.N);
                for(int i=0;i<this.N;i++){
                    for(int j=0;j<this.N;j++){
                        solutionToAdd.getCell(i,j).setValue((this.workingGrid.getCell(i,j).getValue()));
                    }
                }
                this.solutions.add(solutionToAdd);
                return this.solutions.size()>=this.maxSolutionsToFind && this.maxSolutionsToFind>0; //se abbiamo raggiunto le soluzione return true.
            }
            return false;
        }

        //trovo possibile numero che può essere inserito nella cella vuota e verifico se è valido.
        for(int num=1; num<=this.N; num++){
            //se valido inserisco il numero nella cella vuota
            if(isValidPlacement(nextEmptyCell, num)){
                nextEmptyCell.setValue(num);
                int nextRow=nextEmptyCell.getRow();
                int nextCol=nextEmptyCell.getCol()+1;
                if(nextCol==this.N){
                    nextRow++;
                    nextCol=0;
                }

                //chiamata ricorsiva che verifica il resto della griglia
                if(findSolutionRecursive(nextRow,nextCol)){
                    if(this.solutions.size()>=this.maxSolutionsToFind) {
                        return true;
                    }
                }

                //BACKTRACK: se la chiamata ricorsiva non ha trovato una soluzione significa che devo rimuovere il valore inserito nella cella.
                nextEmptyCell.clearValue();
            }
        }
        //se sono qua significa che nessun valore da 1 a N porta a soluzioni valide
        return false;
    }

    //trova la prossima cella vuota che è editabile.
    private Cell findNextEditableEmptyCell(int startRow, int startCol) {
        for(int r=startRow;r<this.N;r++){
            //se sono in una riga diversa da quella iniziale devo inziare dalla colonna 0, altrimenti potrei saltare delle colonne
            int currentCol= (r==startRow)? startCol : 0;
            for(int c=currentCol;c<this.N;c++){
                Cell cell=this.workingGrid.getCell(r,c);
                if(cell.isEditable() && cell.isEmpty()){
                    return cell;
                }
            }
        }
        //significa che non ho trovato nessuna cella vuota editabile
        return null;
    }

    //verifica se il numero è valido per essere inserito nella cella specificata.
    private boolean isValidPlacement(Cell cell, int num){
        for(int c=0;c<this.N;c++){
            if(this.workingGrid.getCell(cell.getRow(),c).getValue()==num) {
                return false; //c'è un numero presente sulla riga
            }
        }

        for(int r=0;r<this.N;r++){
            if(this.workingGrid.getCell(r,cell.getCol()).getValue()==num) {
                return false; //c'è un numero presente nella colonna
            }
        }

        //controllo i vincoli della gabbia
        Cage cage=cell.getParentCage();
        if(cage!=null){
            List<Integer>valuesInCage=new ArrayList<>();//lista dei valori della gabbia
            int cellEmptyCount=0;

            //prendiamo tutte le celle della gabbia e le controlliamo
            for(Cell cellInCage:cage.getCellsInCage()){
                //cella corrente della gabbia
                Cell currentCell=this.workingGrid.getCell(cellInCage.getRow(),cellInCage.getCol());
                //se è la cella che stiamo aggiungendo allora inseriamo direttamente il valore nella lista
                if(currentCell.equals(cell)){
                    valuesInCage.add(num);
                }
                //se la cella non è vuota inseriamo il valore nella lista
                else if(!currentCell.isEmpty()){
                    valuesInCage.add(currentCell.getValue());
                }
                //significa che è una cella vuota quindi incremento il contatore
                else{
                    cellEmptyCount++;
                }
            }

            //controllo se la gabbia è completa di tutti i valori
            if(cellEmptyCount==0){
                //controllo se la gabbia è valida
                try{
                    if(!cage.checkConstraint(valuesInCage)){
                        return false;
                    }
                }catch (IllegalArgumentException e){
                    return false;
                }
            }
        }
        return true;
    }

    //verifica se la griglia è valida.
    private boolean isCurrentGridSolutionValid() {
        //controllo unicità riga e colonna della griglia completa
        for(int i=0;i<this.N;i++){
            boolean [] checkRow=new boolean[this.N+1];//creo array che vanno da 1 a N così da inserire true nella pos
            boolean [] checkCol=new boolean[this.N+1];//del valore inserito della riga o colonna.

            for(int j=0;j<this.N;j++){
                //Check riga
                int rowVal=this.workingGrid.getCell(i,j).getValue();
                if(rowVal==0) return false;//significa che griglia non completa
                if(checkRow[rowVal]) return false;//se il valore è gia presente nella riga return false
                checkRow[rowVal]=true;

                //Check colonna
                int colVal=this.workingGrid.getCell(j,i).getValue();
                if(colVal==0) return false;//significa che griglia non completa
                if(checkCol[colVal]) return false;//se il valore è gia presente nella colonna return false
                checkCol[colVal]=true;
            }
        }

        //verifico vincoli delle gabbie
        for(Cage cage:this.originalCages){
            List<Integer>valuesInCage=new ArrayList<>();
            for(Cell cell:cage.getCellsInCage()){
                Cell cellInGrid=this.workingGrid.getCell(cell.getRow(),cell.getCol());
                if(!cellInGrid.isEmpty()){
                    valuesInCage.add(cellInGrid.getValue());
                }else return false;//se c'è una cella vuota nella gabbia return false, anche se si effettua sopra il controllo
            }

            if(valuesInCage.size()!=cage.getCellsInCage().size())return false;

            try{
                if(!cage.checkConstraint(valuesInCage)){
                    return false;
                }
            }catch (IllegalArgumentException e){
                return false;
            }
        }
        return true;
    }
}
