package com.kenken.model;

import com.kenken.model.dto.CageDefinition;
import com.kenken.model.dto.Coordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameModel {

    private Grid grid;
    private List<Cage> cages;
    private int N;
    private final List<GameObserver> observers;

    private String difficulty;
    //di defualt il controllo dei vincoli è disabilitato
    private boolean realTimeValidationEnabled=false;

    public enum GameState{ NOT_INITIALIZED, PLAYING, CONSTRAINT_VIOLATION, SOLVED, ERROR }
    private GameState gameState;

    public GameModel(){
        this.observers= new ArrayList<>();
        this.N=0;
        this.grid =null;
        this.cages = new ArrayList<>();
        this.gameState = GameState.NOT_INITIALIZED;
        this.difficulty="UNKOWN";
    }

    public void initializeGame(int N, String difficulty, List<CageDefinition> cageDefinitions, Map<Coordinates,Integer> fixedNumbers){
        this.N=N;
        this.difficulty=(difficulty != null && !difficulty.trim().isEmpty()) ? difficulty : "CUSTOM";
        this.grid = new Grid(N);
        this.cages = new ArrayList<>();
        this.gameState = GameState.PLAYING;

        if(cageDefinitions==null || cageDefinitions.isEmpty())
            throw new IllegalArgumentException("ATTENZIONE: La lista della definizione delle gabbie è vuota. Il gioco potrebbe non essere configurato correttamente.");
        else{
            for(CageDefinition def: cageDefinitions){
                Cage cage = new Cage(def.targetValue(), def.operationType());
                if(def.cellsCoordinates()==null || def.cellsCoordinates().isEmpty()) {
                    throw new IllegalArgumentException("ATTENZIONE: La definizione della gabbia "+cage.getCageId()+" non contiene coordinate.");
                }

                for(Coordinates coord:def.cellsCoordinates()){
                    Cell cell=grid.getCell(coord.row(), coord.col());
                    if(cell!=null)cage.addCell(cell);
                    else throw new IllegalArgumentException("ATTENZIONE: La cella con coordinate ("+coord.row()+","+coord.col()+") non esiste.");
                }
                cages.add(cage);
            }
        }

        if(fixedNumbers!=null && !fixedNumbers.isEmpty()){
            for(Map.Entry<Coordinates,Integer> entry:fixedNumbers.entrySet()) {
                Coordinates coord = entry.getKey();
                Integer value = entry.getValue();

                try {
                    Cell cellFix = grid.getCell(coord.row(), coord.col());

                    cellFix.setValue(value);
                    cellFix.setEditable(false);
                }catch (IndexOutOfBoundsException e){
                    System.err.println("ATTENZIONE: La cella per il numero fisso con coordinate (" + coord.row() + "," + coord.col() + ") non esiste o è fuori dai limiti.");
                }


            }
        }
        notifyObservers();
        this.gameState = GameState.PLAYING;
        System.out.println("Nuova partita "+N+"x"+N+" inizializzata con "+cages.size()+" gabbie");
        if(fixedNumbers!=null && !fixedNumbers.isEmpty())
            System.out.println("Le celle con valore fisse sono: "+fixedNumbers.size());
        grid.printToConsole();
    }

    public void notifyObservers(){
        List<GameObserver> observersCopy = new ArrayList<>(observers);
        for(GameObserver observer:observersCopy)
            observer.update(this);
    }

    public void addObserver(GameObserver observer){
       if(observer!=null && !this.observers.contains(observer))
            this.observers.add(observer);
    }

    public void removeObserver(GameObserver observer){
        if(observer!=null)
            this.observers.remove(observer);
    }

    public boolean placeNumber(int row, int col, int value){
        if(this.grid==null || this.N==0 || this.gameState==GameState.NOT_INITIALIZED)
            throw new IllegalStateException("Il modello non è ancora inizializzato");

        if(this.gameState==GameState.SOLVED)
            throw new IllegalStateException("Il gioco è già stato risolto");

        if(value<1 || value>this.N)
            throw new IllegalArgumentException("Errore: Valore: "+value+" non valido.Il valore inserito deve essere compreso tra 1 e "+this.N);

        Cell cell = this.grid.getCell(row, col);

        if(!cell.isEditable()) {
            this.gameState=GameState.ERROR;
            notifyObservers();
            throw new IllegalStateException("Impossibile modificare il valore di una cella fissa");
        }

        cell.setValue(value);
        this.gameState=GameState.PLAYING;

        if(this.realTimeValidationEnabled) {
            boolean isRowValid= isNumberUniqueInRow(row, value, col);
            boolean isColValid= isNumberUniqueInCol(col, value, row);
            boolean isCageValid= checkCageConstraintIfFull(cell.getParentCage());

            if(!isRowValid || !isColValid || !isCageValid) {
                this.gameState=GameState.CONSTRAINT_VIOLATION;
            }
        }

        if(this.gameState!=GameState.CONSTRAINT_VIOLATION && isGameSolved()) {
            this.gameState=GameState.SOLVED;
        }

        notifyObservers();
        return true;
    }

    private boolean isNumberUniqueInRow(int row, int value, int excludeCol) {
        if(this.grid==null || this.N==0) throw new IllegalStateException("La griglia non è ancora inizializzato");

        for(int c=0;c<this.N;c++) {
            if(c==excludeCol) continue;
            Cell currentCell= this.grid.getCell(row, c);
            if(currentCell.getValue()==value) return false;
        }
        return true;
    }
    private boolean isNumberUniqueInCol(int col, int value, int excludeRow) {
        if(this.grid==null || this.N==0) throw new IllegalStateException("La griglia non è ancora inizializzato");

        for(int r=0;r<this.N;r++) {
            if(r==excludeRow) continue;
            Cell currentCell= this.grid.getCell(r, col);
            if(currentCell.getValue()==value) return false;
        }
        return true;
    }
    private boolean checkCageConstraintIfFull(Cage cage){
        if(cage==null || this.grid==null) return true;

        List<Integer>valuesInCage=new ArrayList<>();
        for(Cell cell:cage.getCellsInCage()) {
            if(cell.isEmpty())
                return true;
            valuesInCage.add(cell.getValue());
        }
        try{
            return cage.checkConstraint();
        }catch (IllegalArgumentException e){
            System.err.println("Errore durante la validazione della gabbia " + cage.getCageId() + ": " + e.getMessage());
            return false;
        }
    }
    private boolean isGameSolved() {
        if(this.grid==null || this.N==0) throw new IllegalStateException("La griglia non è ancora inizializzata");

        //verifica che nella griglia non ci siano celle vuote
        for(int r=0;r<this.N;r++)
            for(int c=0;c<this.N;c++)
                if(grid.getCell(r,c).isEmpty())
                    return false;

        //controllo unicità righe
        for(int r=0;r<this.N;r++){
            List<Integer>valuesInRow=new ArrayList<>();
            for(int c=0;c<this.N;c++) {
                if(valuesInRow.contains(grid.getCell(r,c).getValue()))
                    return false;
                valuesInRow.add(grid.getCell(r,c).getValue());
            }
        }

        //controllo unicità colonne
        for(int c=0;c<this.N;c++){
            List<Integer>valuesInCol=new ArrayList<>();
            for(int r=0;r<this.N;r++) {
                if(valuesInCol.contains(grid.getCell(r,c).getValue()))
                    return false;
                valuesInCol.add(grid.getCell(r,c).getValue());
            }
        }

        //controllo unicità gabbie
        for(Cage cage:this.cages){
            if(!cage.checkConstraint())
                return false;
        }
        return true;
    }
    public void clearCell(int row, int col){
        if(this.grid==null || this.N==0)
            throw new IllegalStateException("Il modello non è ancora inizializzato");

        Cell cell= this.grid.getCell(row, col);

        if(!cell.isEditable())
            throw new IllegalStateException("Impossibile modificare il valore di una cella fissa");

        if(cell.getValue()!=0) {
            cell.clearValue();
            this.gameState=GameState.PLAYING;
            notifyObservers();
        }
    }

    public GameStateMemento createMemento(){
        if (this.gameState == GameState.NOT_INITIALIZED) {
            return GameStateMemento.createNotInitializedMemento(this.realTimeValidationEnabled);
        }
       if(this.N==0 || this.grid==null){
           throw new IllegalStateException("Il modello non è ancora inizializzato");
       }

       //Estrarre la definizione delle gabbie, per ricreare cageDefinition dalle cage attuali
       List<CageDefinition> cageDefinitions= new ArrayList<>();
       for(Cage cage:this.cages){
           if (cage.getCellsInCage() != null) { // Controllo aggiunto
               List<Coordinates> cellCoords = cage.getCellsInCage().stream().map(cell -> new Coordinates(cell.getRow(), cell.getCol()))                       .collect(Collectors.toList());
               cageDefinitions.add(new CageDefinition(cage.getTargetValue(), cage.getOperationType(), cellCoords));
           }
       }

       //Estrarre le celle editabili e quelle non con i relativi valori
       int[][] currentCellValue=new int[this.N][this.N];
       boolean[][] currentCellEditable=new boolean[this.N][this.N];
       for(int r=0;r<this.N;r++) {
           for (int c = 0; c < this.N; c++) {
               Cell cell = this.grid.getCell(r, c);
               currentCellValue[r][c] = cell.getValue();
               currentCellEditable[r][c] = cell.isEditable();
           }
       }

       //restituizione del memento
        return new GameStateMemento(this.N, cageDefinitions, currentCellValue, currentCellEditable,this.gameState, this.difficulty,this.realTimeValidationEnabled);
    }

    public void restoreFromMemento(GameStateMemento memento){
        if(memento==null)
            throw new IllegalArgumentException("Il memento non può essere null");

        //se il memento non è stato inizializzato
        if(memento.N()==0 && memento.gameState()==GameState.NOT_INITIALIZED) {
            this.N = 0;
            this.cages = new ArrayList<>();
            this.grid = null;
            this.gameState = GameState.NOT_INITIALIZED;
            this.difficulty = memento.difficulty();
            this.realTimeValidationEnabled = memento.realTimeValidationEnabled();
            notifyObservers();
            return;
        }

        //se il memento è inizializzato ripristino dello stato di gioco
        this.N = memento.N();
        this.difficulty = memento.difficulty();
        this.cages = new ArrayList<>();
        this.grid = new Grid(this.N);

        //ricostruzione delle gabbie e assegna le cella dalla nuova griglia
        for(CageDefinition def:memento.cageDefinitions()){
            Cage cage = new Cage(def.targetValue(), def.operationType());
            if(def.cellsCoordinates()==null || def.cellsCoordinates().isEmpty()) {
               System.err.println("ATTENZIONE: CageDefinition nel memento contiene delle coordinate vuote o null.");
               continue;
            }
            for(Coordinates coord:def.cellsCoordinates()){
                try {
                    Cell cellFromGrid = this.grid.getCell(coord.row(), coord.col());
                    cage.addCell(cellFromGrid);
                }catch (IndexOutOfBoundsException e){
                    System.err.println("ATTENZIONE: Coordinata"+coord+") non esiste o è fuori dai limiti.");
                }
            }
            this.cages.add(cage);
        }

        //ripristino di valori e dell'editabilità di ogni cella
        int[][] mementoCellValues=memento.cellValues();
        boolean[][] mementoCellEditability=memento.cellEditability();
        for(int r=0;r<this.N;r++) {
            for (int c = 0; c < this.N; c++) {
                Cell cellToRestore = this.grid.getCell(r, c);
                cellToRestore.setEditable(mementoCellEditability[r][c]);
                if(mementoCellEditability[r][c]) {
                    cellToRestore.setValue(mementoCellValues[r][c]);
                }else{
                    if(!cellToRestore.isEditable()) {
                        cellToRestore.setEditable(true);
                        cellToRestore.setValue(mementoCellValues[r][c]);
                        cellToRestore.setEditable(mementoCellEditability[r][c]);
                    }
                }
            }
        }

        this.gameState=memento.gameState();
        this.realTimeValidationEnabled=memento.realTimeValidationEnabled();
        notifyObservers();
    }

    public Grid getGrid() {
        return this.grid;
    }
    public List<Cage> getCages() {
        return this.cages;
    }
    public int getN() {
        return this.N;
    }
    public Cell getCell(int row, int col){
        if(this.grid==null)
            throw new IllegalStateException("Il modello non è ancora inizializzato");
        if(row<0 || row>=this.N || col<0 || col>=this.N)
            throw new IndexOutOfBoundsException("La cella non esiste");
        return this.grid.getCell(row,col);
    }
    public String getDifficulty() {
        return this.difficulty;
    }
    public void setRealTimeValidationEnabled(boolean realTimeValidationEnabled){
        this.realTimeValidationEnabled=realTimeValidationEnabled;
        notifyObservers();
    }
    public boolean isRealTimeValidationEnabled(){
        return this.realTimeValidationEnabled;
    }
    public GameState getGameState() {
        return this.gameState;
    }

}
