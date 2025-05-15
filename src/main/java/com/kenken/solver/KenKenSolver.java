package com.kenken.solver;

import com.kenken.model.Cage;
import com.kenken.model.Cell;
import com.kenken.model.Grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KenKenSolver {
    private final Grid workingGrid;
    private final List<Cage> solverCages;
    private final int N;
    private List<Grid> solutions; //lista delle soluzioni trovate
    private int maxSolutionsToFind; //numero massimo di soluzioni da trovare

    public KenKenSolver(Grid originalGridFromGame, List<Cage> originalCagesFromGame, int N) {
        if (originalGridFromGame == null || originalGridFromGame.getSize() != N) {
            throw new IllegalArgumentException("La griglia originale non può essere nulla o di dimensione diversa da N.");
        }
        if (originalCagesFromGame == null) {
            throw new IllegalArgumentException("La lista delle gabbie originali non può essere nulla.");
        }
        this.N = N;
        this.workingGrid = new Grid(N); // Crea una nuova griglia con le sue nuove celle per il solver.

        this.solverCages = new ArrayList<>();
        Map<Integer, Cage> originalCageIdToSolverCageMap = new HashMap<>();

        // Crea le istanze di solverCage (vuote per ora) basate sulle originalCagesFromGame.
        // Mappa gli ID delle gabbie originali alle nuove solverCage.
        for (Cage originalCage : originalCagesFromGame) {
            if (originalCage == null) {
                System.err.println("Attenzione: trovata una Cage nulla nella lista originalCagesFromGame. Sarà ignorata.");
                continue;
            }
            Cage solverCage = new Cage(originalCage.getTargetValue(), originalCage.getOperationType());
            this.solverCages.add(solverCage);
            originalCageIdToSolverCageMap.put(originalCage.getCageId(), solverCage);
        }

        //  Popola la workingGrid con i valori e l'editabilità dalla originalGridFromGame.
        //  Associa ogni cella della workingGrid alla sua solverCage corrispondente.
        //  Aggiunge le celle della workingGrid alle liste interne delle solverCages.
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                Cell cellFromOriginalGrid = originalGridFromGame.getCell(r, c);
                Cell cellInWorkingGrid = this.workingGrid.getCell(r, c);

                if(!cellFromOriginalGrid.isEditable()){
                    cellInWorkingGrid.setValue(cellFromOriginalGrid.getValue());
                    cellInWorkingGrid.setEditable(false);
                }


                // Collega la cella della workingGrid alla sua solverCage.
                if (cellFromOriginalGrid.getParentCage() != null) {
                    Cage originalParentCage = cellFromOriginalGrid.getParentCage();
                    Cage solverParentCage = originalCageIdToSolverCageMap.get(originalParentCage.getCageId());

                    if (solverParentCage != null) {
                        solverParentCage.addCell(cellInWorkingGrid);
                    } else {
                        // Questo non dovrebbe accadere se originalCagesFromGame è coerente.
                        System.err.println("Errore critico: Impossibile trovare la solverCage corrispondente per la gabbia originale con ID: "
                                + originalParentCage.getCageId() + " per la cella (" + r + "," + c + ")");
                    }
                }
            }
        }
    }

    public List<Grid> solve(int maxSolutionsToFind){
        this.solutions=new ArrayList<>();
        this.maxSolutionsToFind=maxSolutionsToFind;
        if(this.maxSolutionsToFind==0)return this.solutions;
        if(this.maxSolutionsToFind<0)this.maxSolutionsToFind=100;

        findSolutionRecursive(0,0);
        return this.solutions;
    }

    private boolean findSolutionRecursive(int row, int col){
        if(this.solutions.size()>=this.maxSolutionsToFind) return true;

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
                return this.solutions.size()>=this.maxSolutionsToFind; //se abbiamo raggiunto le soluzione return true.
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

            int originalValue= cell.getValue();
            boolean wasEditable=cell.isEditable();

            if(!wasEditable)cell.setEditable(true);

            cell.setValue(num);

           boolean allCellsInCageFilled=true;


            //prendiamo tutte le celle della gabbia e le controlliamo
            for(Cell cellInCage:cage.getCellsInCage()) {
                //se è la cella che stiamo aggiungendo allora inseriamo direttamente il valore nella lista
                if (cellInCage.isEmpty()) {
                    allCellsInCageFilled=false;
                    break;
                }
            }

            if(allCellsInCageFilled){
                try{
                    if(!cage.checkConstraint()){
                        cell.setValue(originalValue);
                        if(!wasEditable)cell.setEditable(false);
                        return false;
                    }
                }catch (IllegalArgumentException e){
                    cell.setValue(originalValue);
                    if(!wasEditable)cell.setEditable(false);
                    return false;
                }
            }

            cell.setValue(originalValue);
            if(!wasEditable) cell.setEditable(false);
        }
        return true;
    }

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
        for(Cage cage:this.solverCages){
            try{
                if(!cage.checkConstraint()){
                    // Se ANCHE SOLO UNA gabbia non soddisfa il vincolo, l'intera soluzione non è valida
                    return false;
                }
            }catch (IllegalArgumentException e){
                System.err.println("Eccezione durante checkConstraint per la gabbia " + cage.getCageId() +
                        " (Target: " + cage.getTargetValue() + cage.getOperationType().getSymbol() +
                        ", Celle: " + cage.getCellsInCage().size() + "): " + e.getMessage());
                return false;
            }
        }
        return true;
    }

}
