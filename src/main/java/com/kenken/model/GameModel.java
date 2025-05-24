package com.kenken.model;

import com.kenken.model.dto.CageDefinition;
import com.kenken.model.dto.Coordinates;

import java.util.*;
import java.util.stream.Collectors;

public class GameModel {

    private Grid grid;
    private List<Cage> cages;
    private int N;
    private final List<GameObserver> observers;

    private String difficulty;
    private boolean realTimeValidationEnabled = false;
    private final Set<Coordinates> violatingCells; // Nuovo: per tracciare le celle in errore

    public enum GameState { NOT_INITIALIZED, PLAYING, CONSTRAINT_VIOLATION, SOLVED, ERROR }

    private GameState gameState;

    public GameModel() {
        this.observers = new ArrayList<>();
        this.N = 0;
        this.grid = null;
        this.cages = new ArrayList<>();
        this.violatingCells = new HashSet<>(); // Inizializza il Set
        this.gameState = GameState.NOT_INITIALIZED;
        this.difficulty = "UNKNOWN";
    }

    public void initializeGame(int N, String difficulty, List<CageDefinition> cageDefinitions, Map<Coordinates, Integer> fixedNumbersFromExternal) {
        // ... (inizio del metodo initializeGame come prima) ...
        if (N == 0 && (difficulty != null && difficulty.startsWith("ERROR"))) {
            this.N = 0;
            this.difficulty = difficulty;
            this.grid = null;
            this.cages = new ArrayList<>();
            this.violatingCells.clear(); // Svuota anche qui
            this.gameState = GameState.ERROR;
            System.err.println("GameModel: Inizializzazione in stato di errore: " + difficulty);
            notifyObservers();
            return;
        }
        // ... (controlli su N e cageDefinitions come prima) ...
        if (N < 3 || N > 6) {
            this.N = 0; this.difficulty = "INVALID_N"; this.gameState = GameState.ERROR;
            this.violatingCells.clear(); notifyObservers(); return;
        }
        if (cageDefinitions == null || cageDefinitions.isEmpty()) {
            this.N = N; this.difficulty = (difficulty != null && !difficulty.trim().isEmpty()) ? difficulty : "CUSTOM_EMPTY";
            this.grid = new Grid(N); this.cages = new ArrayList<>(); this.gameState = GameState.ERROR;
            this.violatingCells.clear(); notifyObservers(); return;
        }

        this.N = N;
        this.difficulty = (difficulty != null && !difficulty.trim().isEmpty()) ? difficulty : "CUSTOM";
        this.grid = new Grid(N);
        this.cages = new ArrayList<>();
        this.violatingCells.clear(); // Svuota all'inizio di una nuova partita
        this.gameState = GameState.PLAYING;

        // ... (popolamento gabbie e verifica allCellsAssigned come prima) ...
        for (CageDefinition def : cageDefinitions) {
            if (def == null || def.cellsCoordinates() == null || def.cellsCoordinates().isEmpty()) continue;
            Cage cage = new Cage(def.targetValue(), def.operationType());
            for (Coordinates coord : def.cellsCoordinates()) {
                try {
                    Cell cell = grid.getCell(coord.row(), coord.col());
                    if (cell != null) cage.addCell(cell);
                } catch (IndexOutOfBoundsException e) { System.err.println("GameModel: Coordinata " + coord + " errata. " + e.getMessage()); }
            }
            if (!cage.getCellsInCage().isEmpty()) cages.add(cage);
            else System.err.println("GameModel: Gabbia creata senza celle valide.");
        }

        boolean allCellsAssigned = true;
        if (this.grid != null) {
            for (int r_idx = 0; r_idx < this.N; r_idx++) {
                for (int c_idx = 0; c_idx < this.N; c_idx++) {
                    if (this.grid.getCell(r_idx,c_idx).getParentCage() == null) {
                        System.err.println("GameModel WARNING: Cella (" + r_idx + "," + c_idx + ") non assegnata!");
                        allCellsAssigned = false;
                    }
                }
            }
        }
        if (!allCellsAssigned && this.gameState != GameState.ERROR) {
            this.gameState = GameState.ERROR; this.difficulty = "ERROR_INCOMPLETE_CAGES";
        }


        // Impostazione valori fissi per gabbie NONE
        int noneCagesSet = 0;
        for (Cage cage : this.cages) {
            if (cage.getOperationType() == OperationType.NONE && cage.getCellsInCage().size() == 1) {
                Cell cellToFix = cage.getCellsInCage().get(0);
                int valueToSet = cage.getTargetValue();
                if (valueToSet >= 1 && valueToSet <= this.N) {
                    try {
                        cellToFix.setValue(valueToSet);
                        cellToFix.setEditable(false);
                        noneCagesSet++;
                    } catch (IllegalStateException e) { System.err.println("GameModel: Errore impostazione cella fissa NONE: " + e.getMessage()); }
                } else {
                    System.err.println("GameModel: TargetValue " + valueToSet + " per gabbia NONE non valido per N=" + this.N + " in (" + cellToFix.getRow() + "," + cellToFix.getCol() + ").");
                }
            }
        }
        if (noneCagesSet > 0) System.out.println("GameModel: Impostati " + noneCagesSet + " valori fissi per gabbie NONE.");

        // Impostazione numeri fissi da mappa esterna
        if (fixedNumbersFromExternal != null && !fixedNumbersFromExternal.isEmpty()) {
            // ... (logica come prima) ...
        }

        System.out.println("GameModel: Nuova partita " + N + "x" + N + " (" + difficulty + ") inizializzata. Stato: " + this.gameState);
        notifyObservers();
    }

    public void placeNumber(int row, int col, int value) {
        if (this.grid == null || this.N == 0 || this.gameState == GameState.NOT_INITIALIZED) {
            throw new IllegalStateException("Il modello non è ancora inizializzato.");
        }
        if (this.gameState == GameState.SOLVED || this.gameState == GameState.ERROR) return;
        if (value < 1 || value > this.N) {
            throw new IllegalArgumentException("Valore: " + value + " non valido. Deve essere tra 1 e " + this.N);
        }
        Cell cell = this.grid.getCell(row, col);
        if (!cell.isEditable()) {
            throw new IllegalStateException("Impossibile modificare il valore di una cella fissa ("+row+","+col+").");
        }

        // Svuota le celle in errore precedenti prima di una nuova valutazione
        this.violatingCells.clear();
        cell.setValue(value);
        this.gameState = GameState.PLAYING; // Stato temporaneo

        if (this.realTimeValidationEnabled) {
            performSingleCellValidation(row, col, value);
        }

        boolean isGridNowFull = isGridFull();
        System.out.println("GameModel.placeNumber: isGridNowFull = " + isGridNowFull);

        if (isGridNowFull) {
            System.out.println("GameModel: Griglia completata. Eseguo validazione globale.");
            performGlobalValidation();
            System.out.println("GameModel.placeNumber: Stato dopo performGlobalValidation: " + this.gameState);
        } else {
            System.out.println("GameModel.placeNumber: Griglia non piena. Stato attuale: " + this.gameState);
        }

        notifyObservers();
    }

    private void performSingleCellValidation(int row, int col, int newValue) {
        // violatingCells è già stato svuotato in placeNumber
        boolean violationFound = false;

        for (int c = 0; c < this.N; c++) {
            if (c != col && this.grid.getCell(row, c).getValue() == newValue && newValue != 0) {
                violationFound = true;
                violatingCells.add(new Coordinates(row, col)); violatingCells.add(new Coordinates(row, c));
            }
        }
        for (int r = 0; r < this.N; r++) {
            if (r != row && this.grid.getCell(r, col).getValue() == newValue && newValue != 0) {
                violationFound = true;
                violatingCells.add(new Coordinates(row, col)); violatingCells.add(new Coordinates(r, col));
            }
        }

        Cell currentCell = this.grid.getCell(row, col);
        Cage parentCage = currentCell.getParentCage();
        if (parentCage != null) {
            boolean allCellsInCageFilled = true;
            for (Cell cellInCage : parentCage.getCellsInCage()) if (cellInCage.isEmpty()) {allCellsInCageFilled = false; break;}
            if (allCellsInCageFilled) {
                try {
                    if (!parentCage.checkConstraint()) {
                        violationFound = true;
                        for (Cell cellInCage : parentCage.getCellsInCage()) violatingCells.add(new Coordinates(cellInCage.getRow(), cellInCage.getCol()));
                    }
                } catch (IllegalArgumentException e) {
                    violationFound = true;
                    for (Cell cellInCage : parentCage.getCellsInCage()) violatingCells.add(new Coordinates(cellInCage.getRow(), cellInCage.getCol()));
                }
            }
        }

        if (violationFound) {
            this.gameState = GameState.CONSTRAINT_VIOLATION;
        } else {
            // Se nessuna violazione è stata trovata da questa specifica mossa,
            // e lo stato era PLAYING, rimane PLAYING.
            // Se era CONSTRAINT_VIOLATION, e questa mossa ha risolto TUTTE le violazioni
            // (cosa che questo metodo da solo non può garantire, serve una rivalutazione globale
            // se vogliamo che CONSTRAINT_VIOLATION torni a PLAYING solo con una singola mossa correttiva),
            // per ora lo lasciamo a PLAYING. Una successiva validazione globale lo sistemerebbe.
            // La logica più semplice è: se questa mossa non crea errori, lo stato è PLAYING.
            this.gameState = GameState.PLAYING;
        }
        System.out.println("GameModel.performSingleCellValidation: Stato dopo validazione singola cella: " + this.gameState); // DEBUG
    }

    public void performGlobalValidation() {
        if (this.grid == null || this.N == 0 || this.gameState == GameState.NOT_INITIALIZED || this.gameState == GameState.ERROR) {
            System.out.println("GameModel.performGlobalValidation: Validazione non applicabile allo stato attuale.");
            return;
        }
        this.violatingCells.clear();
        boolean anyViolationFound = false;
        boolean isGridFull = true;

        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                Cell currentCell = grid.getCell(r, c);
                if (currentCell.isEmpty()) {
                    isGridFull = false;
                    continue;
                }
                int currentValue = currentCell.getValue();
                for (int c2 = 0; c2 < N; c2++) {
                    if (c2 != c && grid.getCell(r, c2).getValue() == currentValue) {
                        anyViolationFound = true;
                        violatingCells.add(new Coordinates(r, c));
                        violatingCells.add(new Coordinates(r, c2));
                    }
                }
                for (int r2 = 0; r2 < N; r2++) {
                    if (r2 != r && grid.getCell(r2, c).getValue() == currentValue) {
                        anyViolationFound = true;
                        violatingCells.add(new Coordinates(r, c));
                        violatingCells.add(new Coordinates(r2, c));
                    }
                }
            }
        }

        if (this.cages != null) {
            for (Cage cage : this.cages) {
                boolean allCellsInCageFilled = true;
                if (cage.getCellsInCage().isEmpty()) continue;
                for (Cell cellInCage : cage.getCellsInCage()) {
                    if (cellInCage.isEmpty()) {
                        allCellsInCageFilled = false;
                        break;
                    }
                }
                if (allCellsInCageFilled) {
                    try {
                        if (!cage.checkConstraint()) {
                            anyViolationFound = true;
                            for (Cell cellInCage : cage.getCellsInCage()) {
                                violatingCells.add(new Coordinates(cellInCage.getRow(), cellInCage.getCol()));
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        System.err.println("GameModel.performGlobalValidation: Errore nel vincolo della gabbia " + cage.getCageId() + ": " + e.getMessage());
                        anyViolationFound = true;
                        for (Cell cellInCage : cage.getCellsInCage()) {
                            violatingCells.add(new Coordinates(cellInCage.getRow(), cellInCage.getCol()));
                        }
                    }
                }
            }
        }

        if (anyViolationFound) {
            this.gameState = GameState.CONSTRAINT_VIOLATION;
        } else {
            if (isGridFull) {
                this.gameState = GameState.SOLVED;
                System.out.println("GameModel: Validazione Globale - Gioco RISOLTO!");
            } else {
                this.gameState = GameState.PLAYING;
            }
        }
        // Non chiamare notifyObservers() qui, sarà chiamato dal metodo che invoca performGlobalValidation
        // o da placeNumber() se è il contesto.
    }

    public void clearCell(int row, int col) {
        // ... (controlli iniziali come prima) ...
        if (this.grid == null || this.N == 0) throw new IllegalStateException("Il modello non è ancora inizializzato.");
        if (this.gameState == GameState.SOLVED || this.gameState == GameState.ERROR) return;
        Cell cell = this.grid.getCell(row, col);
        if (!cell.isEditable()) {
            throw new IllegalStateException("Impossibile modificare il valore di una cella fissa ("+row+","+col+").");
        }

        if (!cell.isEmpty()) {
            cell.clearValue();
            this.gameState = GameState.PLAYING; // Torna a PLAYING
            this.violatingCells.clear();       // Svuota le celle in errore

            // Opzionale: Se la validazione in tempo reale è attiva, potremmo
            // voler rivalutare l'intera griglia per vedere se la cancellazione
            // ha risolto una violazione che coinvolgeva altre celle.
            // Per ora, la semplice cancellazione e il reset a PLAYING sono sufficienti.
            // Una successiva immissione di numero rieseguirà la validazione.
            notifyObservers();
        }
    }

    public boolean isGridFull() {
        if (this.grid == null || this.N == 0) return false;
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                if (grid.getCell(r, c).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public Set<Coordinates> getViolatingCells() {
        return violatingCells;
    }

    public void addObserver(GameObserver observer) {
        if (observer != null && !this.observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    public void notifyObservers() {
        List<GameObserver> observersCopy = new ArrayList<>(observers);
        for (GameObserver observer : observersCopy) {
            observer.update(this);
        }
    }

    public void setGridValuesFromSolution(Grid solutionGrid) {
        if (solutionGrid == null || solutionGrid.getSize() != this.N || this.grid == null) {
            System.err.println("GameModel.setGridValuesFromSolution: Griglia soluzione non valida o non compatibile.");
            this.gameState = GameState.ERROR;
            this.difficulty = "ERROR_SOL_DISPLAY";
            this.violatingCells.clear();
            notifyObservers();
            return;
        }
        this.violatingCells.clear();
        for (int r = 0; r < this.N; r++) {
            for (int c = 0; c < this.N; c++) {
                Cell modelCell = this.grid.getCell(r, c);
                Cell solutionCell = solutionGrid.getCell(r, c);
                modelCell.setEditable(true);
                modelCell.setValue(solutionCell.getValue());
                modelCell.setEditable(false);
            }
        }
        this.gameState = GameState.SOLVED;
        System.out.println("GameModel: Valori della griglia impostati dalla soluzione. Stato: SOLVED.");
        notifyObservers();
    }

    public GameStateMemento createMemento() {
        if (this.gameState == GameState.NOT_INITIALIZED && this.N == 0) {
            return GameStateMemento.createNotInitializedMemento(this.realTimeValidationEnabled);
        }
        if (this.N == 0 || this.grid == null) {
            System.err.println("GameModel.createMemento: Tentativo di creare memento ma N è 0 o griglia è null, e lo stato non è NOT_INITIALIZED.");
            return GameStateMemento.createNotInitializedMemento(this.realTimeValidationEnabled);
        }
        List<CageDefinition> cageDefinitions = new ArrayList<>();
        if (this.cages != null) {
            for (Cage cage : this.cages) {
                if (cage != null && cage.getCellsInCage() != null) {
                    List<Coordinates> cellCoords = cage.getCellsInCage().stream()
                            .map(cell -> new Coordinates(cell.getRow(), cell.getCol()))
                            .collect(Collectors.toList());
                    if (!cellCoords.isEmpty()) {
                        cageDefinitions.add(new CageDefinition(cage.getTargetValue(), cage.getOperationType(), cellCoords));
                    }
                }
            }
        }
        int[][] currentCellValues = new int[this.N][this.N];
        boolean[][] currentCellEditable = new boolean[this.N][this.N];
        for (int r = 0; r < this.N; r++) {
            for (int c = 0; c < this.N; c++) {
                Cell cell = this.grid.getCell(r, c);
                currentCellValues[r][c] = cell.getValue();
                currentCellEditable[r][c] = cell.isEditable();
            }
        }
        return new GameStateMemento(this.N, cageDefinitions, currentCellValues, currentCellEditable, this.gameState, this.difficulty, this.realTimeValidationEnabled);
    }

    public void restoreFromMemento(GameStateMemento memento) {
        if (memento == null) {
            throw new IllegalArgumentException("Il memento non può essere null.");
        }
        this.violatingCells.clear(); // Svuota le violazioni quando si carica
        if (memento.N() == 0 && memento.gameState() == GameState.NOT_INITIALIZED) {
            this.N = 0; this.cages = new ArrayList<>(); this.grid = null;
            this.gameState = GameState.NOT_INITIALIZED;
            this.difficulty = memento.difficulty();
            this.realTimeValidationEnabled = memento.realTimeValidationEnabled();
            System.out.println("GameModel: Ripristinato a stato non inizializzato da memento.");
            notifyObservers();
            return;
        }
        // ... (resto del metodo restoreFromMemento come prima, assicurati che sia robusto) ...
        if (memento.N() < 3 || memento.N() > 6 || memento.cageDefinitions() == null || memento.cellValues() == null || memento.cellEditability() == null) {
            System.err.println("GameModel.restoreFromMemento: Memento non valido o corrotto.");
            this.gameState = GameState.ERROR; this.difficulty = "ERROR_MEMENTO";
            this.N = 0; this.grid = null; this.cages = new ArrayList<>();
            notifyObservers(); return;
        }
        this.N = memento.N(); this.difficulty = memento.difficulty();
        this.grid = new Grid(this.N); this.cages = new ArrayList<>();
        for (CageDefinition def : memento.cageDefinitions()) {
            if (def == null || def.cellsCoordinates() == null || def.cellsCoordinates().isEmpty()) continue;
            Cage cage = new Cage(def.targetValue(), def.operationType());
            for (Coordinates coord : def.cellsCoordinates()) {
                try { cage.addCell(this.grid.getCell(coord.row(), coord.col())); }
                catch (IndexOutOfBoundsException e) { System.err.println("GameModel.restoreFromMemento: Coordinata " + coord + " errata. " + e.getMessage());}
            }
            if (!cage.getCellsInCage().isEmpty()) this.cages.add(cage);
        }
        int[][] mementoCellValues = memento.cellValues();
        boolean[][] mementoCellEditability = memento.cellEditability();
        if (this.N > 0 && (mementoCellValues.length != this.N || mementoCellValues[0].length != this.N ||
                mementoCellEditability.length != this.N || mementoCellEditability[0].length != this.N)) {
            System.err.println("GameModel.restoreFromMemento: Dimensioni matrici memento errate.");
            this.gameState = GameState.ERROR; this.difficulty = "ERROR_MEMENTO_CELLS";
        } else if (this.N > 0) {
            for (int r = 0; r < this.N; r++) {
                for (int c = 0; c < this.N; c++) {
                    Cell cellToRestore = this.grid.getCell(r, c);
                    cellToRestore.setEditable(true);
                    cellToRestore.setValue(mementoCellValues[r][c]);
                    cellToRestore.setEditable(mementoCellEditability[r][c]);
                }
            }
        }
        this.gameState = memento.gameState();
        this.realTimeValidationEnabled = memento.realTimeValidationEnabled();
        System.out.println("GameModel: Stato ripristinato da memento. N=" + this.N + ", Stato=" + this.gameState);
        notifyObservers();
    }

    public Grid getGrid() { return this.grid; }

    public List<Cage> getCages() { return this.cages; }

    public int getN() { return this.N; }

    public GameState getGameState() { return this.gameState; }

    public String getDifficulty() { return this.difficulty; }

    public boolean isRealTimeValidationEnabled() { return this.realTimeValidationEnabled; }

    public Cell getCell(int row, int col) {
        if (this.grid == null) throw new IllegalStateException("La griglia non è inizializzata.");
        if (row < 0 || row >= this.N || col < 0 || col >= this.N) {
            throw new IndexOutOfBoundsException("Cella (" + row + "," + col + ") fuori dai limiti per N=" + this.N);
        }
        return this.grid.getCell(row, col);
    }

    public void setRealTimeValidationEnabled(boolean enabled) {
        if (this.realTimeValidationEnabled != enabled) {
            this.realTimeValidationEnabled = enabled;
            System.out.println("GameModel: Validazione in tempo reale impostata a " + enabled);
            if (!enabled) { // Se la validazione viene disattivata, cancella le violazioni esistenti
                this.violatingCells.clear();
                if (this.gameState == GameState.CONSTRAINT_VIOLATION) {
                    this.gameState = GameState.PLAYING; // Torna a PLAYING
                }
            }
            notifyObservers();
        }
    }
}
