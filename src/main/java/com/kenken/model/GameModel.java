package com.kenken.model;

import com.kenken.model.dto.CageDefinition;
import com.kenken.model.dto.Coordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameModel {

    private Grid grid;
    private List<Cage> cages;
    private int N;
    private final List<GameObserver> observers;

    private enum GameState{ PLAYING, SOLVED, ERROR }
    private GameState gameState;

    public GameModel(){
        this.observers= new ArrayList<>();
        this.N=0;
        this.grid = new Grid(N);
        this.cages = new ArrayList<>();
        this.gameState = GameState.PLAYING;
    }

    public void initializeGame(int N, List<CageDefinition> cageDefinitions, Map<Coordinates,Integer> fixedNumbers){
        this.N=N;
        this.grid = new Grid(N);
        this.cages = new ArrayList<>();

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

                Cell cellFix = grid.getCell(coord.row(), coord.col());
                if (cellFix != null) {
                    cellFix.setValue(value);
                    cellFix.setEditable(false);
                } else
                    System.err.println("ATTENZIONE: La cella con coordinate (" + coord.row() + "," + coord.col() + ") non esiste.");

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


    /*TODO
    public GameStateMemento createMemento(){
       // TODO:  Crea e restituisce un memento con lo stato attuale (es. i valori delle celle)
        //       Potrebbe essere una lista di valori o una copia della matrice dei valori
        return null;
    }

    public void restoreFromMemento(GameStateMemento memento){
        //TODO: Ripristina lo stato del modello dal memento
        //aggiorna i valori nelle celle del grid

    }*/

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
}
