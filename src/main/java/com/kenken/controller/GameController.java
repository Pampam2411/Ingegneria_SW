package com.kenken.controller;

import com.kenken.model.GameModel;
import com.kenken.model.Grid;
import com.kenken.model.GameStateMemento;
import com.kenken.model.dto.CageDefinition;
import com.kenken.model.dto.Coordinates; // Assicurati che sia importato
import com.kenken.generator.PuzzleGenerator;
import com.kenken.solver.KenKenSolver;
import com.kenken.view.UserFeedback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class GameController {

    private GameModel gameModel;
    private PuzzleGenerator puzzleGenerator;
    private UserFeedback userFeedback;
    private List<Grid> foundSolutions;
    private int currentSolutionIndex;
    private static final int DEFAULT_MAX_SOLUTIONS_TO_FIND = 100;

    private Coordinates currentlySelectedCellInGrid; // Nuovo: per la cella selezionata dalla GridPanel

    public GameController(GameModel model, UserFeedback feedback) {
        this.gameModel = model;
        this.puzzleGenerator = new PuzzleGenerator();
        this.userFeedback = feedback;
        this.foundSolutions = new ArrayList<>();
        this.currentSolutionIndex = -1;
        this.currentlySelectedCellInGrid = null;
    }

    /**
     * Chiamato da GridPanel quando una cella viene selezionata (o deselezionata con null).
     * @param coord Le coordinate della cella selezionata, o null se nessuna.
     */
    public void setActiveCellFromGrid(Coordinates coord) {
        this.currentlySelectedCellInGrid = coord;
        System.out.println("GameController: Cella attiva impostata a: " + (coord != null ? coord.toString() : "null"));
        // Notifica la UI (es. NumberInputPanel) per abilitare/disabilitare i pulsanti
        // Questo può essere fatto tramite l'observer pattern del GameModel se lo stato della cella selezionata
        // viene messo nel modello, o il MainFrame può passare questo stato al NumberInputPanel.
        // Per ora, il NumberInputPanel si abiliterà se N > 0.
        // Una notifica esplicita potrebbe essere gameModel.notifyObservers() se lo stato della selezione fosse nel modello.
    }

    /**
     * Chiamato da NumberInputPanel quando un pulsante numerico viene premuto.
     * @param number Il numero da inserire.
     */
    public void inputNumberViaButton(int number) {
        if (currentlySelectedCellInGrid != null) {
            System.out.println("GameController: Input da pulsante: " + number + " per cella " + currentlySelectedCellInGrid);
            placeNumberInCell(currentlySelectedCellInGrid.row(), currentlySelectedCellInGrid.col(), number);
        } else {
            if (userFeedback != null) {
                userFeedback.showInfoMessage("Nessuna Cella Selezionata", "Per favore, seleziona una cella nella griglia prima di inserire un numero.");
            }
            System.out.println("GameController: Input da pulsante ignorato, nessuna cella selezionata.");
        }
    }

    /**
     * Chiamato da NumberInputPanel quando il pulsante "Cancella" (o 0) viene premuto.
     */
    public void clearNumberViaButton() {
        if (currentlySelectedCellInGrid != null) {
            System.out.println("GameController: Cancellazione da pulsante per cella " + currentlySelectedCellInGrid);
            clearCell(currentlySelectedCellInGrid.row(), currentlySelectedCellInGrid.col());
        } else {
            if (userFeedback != null) {
                userFeedback.showInfoMessage("Nessuna Cella Selezionata", "Per favore, seleziona una cella nella griglia per cancellare.");
            }
            System.out.println("GameController: Cancellazione da pulsante ignorata, nessuna cella selezionata.");
        }
    }


    public void startNewGame(int N, String difficulty) {
        System.out.println("GameController: Avvio nuova partita N=" + N + ", Difficoltà=" + difficulty);
        clearFoundSolutions();
        setActiveCellFromGrid(null); // Deseleziona qualsiasi cella
        try {
            List<CageDefinition> cageDefs = puzzleGenerator.generatePuzzle(N, difficulty);
            if (cageDefs == null || cageDefs.isEmpty()) {
                String errorMsg = "Generazione puzzle fallita: nessuna gabbia definita per N=" + N + ", Diff=" + difficulty + ".";
                if (userFeedback != null) userFeedback.showErrorMessage("Errore Generazione Puzzle", errorMsg);
                gameModel.initializeGame(0, "ERROR_GEN_EMPTY", new ArrayList<>(), new HashMap<>());
                return;
            }
            Map<Coordinates, Integer> fixedNumbers = new HashMap<>();
            gameModel.initializeGame(N, difficulty, cageDefs, fixedNumbers);
        } catch (IllegalArgumentException e) {
            String errorMsg = "Parametri non validi (N=" + N + ", Diff=" + difficulty + "): " + e.getMessage();
            if (userFeedback != null) userFeedback.showErrorMessage("Errore Nuova Partita", errorMsg);
            gameModel.initializeGame(0, "ERROR_ARGS", new ArrayList<>(), new HashMap<>());
        } catch (RuntimeException e) {
            String errorMsg = "Errore imprevisto generazione puzzle (N=" + N + ", Diff=" + difficulty + "): " + e.getMessage();
            if (userFeedback != null) userFeedback.showErrorMessage("Errore Generazione Puzzle", errorMsg);
            gameModel.initializeGame(0, "ERROR_GEN_RUNTIME", new ArrayList<>(), new HashMap<>());
        }
    }

    // ... (metodi solvePuzzle, displaySolutionAtIndex, showNext/PreviousSolution, canShowNext/PreviousSolution,
    //      getCurrentSolutionIndex, getTotalSolutionsFound, clearFoundSolutions,
    //      placeNumberInCell, clearCell, setRealTimeValidation, saveGame, loadGame, validateCurrentGrid
    //      rimangono come nell'artefatto "game_controller_save_load_fs" o la tua versione più recente)
    //      Assicurati che placeNumberInCell e clearCell gestiscano correttamente le eccezioni e
    //      che il feedback all'utente sia dato se l'azione non è permessa.

    public void solvePuzzle() {
        System.out.println("GameController: Richiesta Risoluzione Puzzle.");
        if (gameModel.getGameState() == GameModel.GameState.NOT_INITIALIZED || gameModel.getN() == 0) {
            if (userFeedback != null) userFeedback.showErrorMessage("Errore Risoluzione", "Nessuna partita attiva da risolvere.");
            return;
        }
        clearFoundSolutions();
        setActiveCellFromGrid(null); // Deseleziona cella quando si mostra soluzione
        KenKenSolver solver = new KenKenSolver(gameModel.getGrid(), gameModel.getCages(), gameModel.getN());
        System.out.println("GameController: Avvio KenKenSolver per trovare max " + DEFAULT_MAX_SOLUTIONS_TO_FIND + " soluzioni...");
        this.foundSolutions = solver.solve(DEFAULT_MAX_SOLUTIONS_TO_FIND);

        if (this.foundSolutions != null && !this.foundSolutions.isEmpty()) {
            this.currentSolutionIndex = 0;
            displaySolutionAtIndex(this.currentSolutionIndex);
            String msg = "Trovata/e " + this.foundSolutions.size() + " soluzione/i. Mostrando la prima.";
            if (this.foundSolutions.size() > 1) {
                msg += "\nUsa i comandi di navigazione soluzione.";
            }
            if (userFeedback != null) userFeedback.showInfoMessage("Risoluzione Puzzle", msg);
        } else {
            if (userFeedback != null) userFeedback.showErrorMessage("Risoluzione Puzzle", "Nessuna soluzione trovata.");
        }
        gameModel.notifyObservers();
    }

    private void displaySolutionAtIndex(int index) {
        if (foundSolutions != null && index >= 0 && index < foundSolutions.size()) {
            Grid solutionToShow = foundSolutions.get(index);
            gameModel.setGridValuesFromSolution(solutionToShow);
        } else {
            System.err.println("GameController: Indice soluzione non valido: " + index);
        }
    }

    public void showNextSolution() {
        if (canShowNextSolution()) {
            currentSolutionIndex++;
            displaySolutionAtIndex(currentSolutionIndex);
            if (userFeedback != null) userFeedback.showInfoMessage("Navigazione Soluzioni", "Mostrando soluzione " + (currentSolutionIndex + 1) + " di " + foundSolutions.size() + ".");
            gameModel.notifyObservers();
        } else {
            if (userFeedback != null) userFeedback.showInfoMessage("Navigazione Soluzioni", "Sei già all'ultima soluzione.");
        }
    }

    public void showPreviousSolution() {
        if (canShowPreviousSolution()) {
            currentSolutionIndex--;
            displaySolutionAtIndex(currentSolutionIndex);
            if (userFeedback != null) userFeedback.showInfoMessage("Navigazione Soluzioni", "Mostrando soluzione " + (currentSolutionIndex + 1) + " di " + foundSolutions.size() + ".");
            gameModel.notifyObservers();
        } else {
            if (userFeedback != null) userFeedback.showInfoMessage("Navigazione Soluzioni", "Sei già alla prima soluzione.");
        }
    }
    public boolean canShowNextSolution() { return foundSolutions != null && currentSolutionIndex < foundSolutions.size() - 1; }
    public boolean canShowPreviousSolution() { return foundSolutions != null && currentSolutionIndex > 0; }
    public int getCurrentSolutionIndex() { return currentSolutionIndex; }
    public int getTotalSolutionsFound() { return foundSolutions != null ? foundSolutions.size() : 0; }
    public void clearFoundSolutions() {
        if (this.foundSolutions != null) this.foundSolutions.clear();
        this.currentSolutionIndex = -1;
        if (gameModel != null) gameModel.notifyObservers();
    }

    public void placeNumberInCell(int row, int col, int value) {
        if (gameModel.getGameState() == GameModel.GameState.PLAYING || gameModel.getGameState() == GameModel.GameState.CONSTRAINT_VIOLATION) {
            try { gameModel.placeNumber(row, col, value); }
            catch (IllegalArgumentException e) { if (userFeedback != null) userFeedback.showErrorMessage("Input Non Valido", "Valore '" + value + "' non valido.\nInserisci un numero tra 1 e " + gameModel.getN() + "."); }
            catch (IllegalStateException e) { if (userFeedback != null) userFeedback.showErrorMessage("Azione Non Permessa", "La cella (" + (row + 1) + "," + (col + 1) + ") non è modificabile."); }
        } else { if (userFeedback != null) userFeedback.showInfoMessage("Azione Non Permessa", "Non è possibile inserire numeri ora. Stato gioco: " + gameModel.getGameState()); }
    }
    public void clearCell(int row, int col) {
        if (gameModel.getGameState() == GameModel.GameState.PLAYING || gameModel.getGameState() == GameModel.GameState.CONSTRAINT_VIOLATION) {
            try { gameModel.clearCell(row, col); }
            catch (IllegalStateException e) { if (userFeedback != null) userFeedback.showErrorMessage("Azione Non Permessa", "La cella (" + (row + 1) + "," + (col + 1) + ") non è modificabile."); }
        } else { if (userFeedback != null) userFeedback.showInfoMessage("Azione Non Permessa", "Non è possibile cancellare numeri ora. Stato gioco: " + gameModel.getGameState());}
    }
    public void setRealTimeValidation(boolean enabled) {
        gameModel.setRealTimeValidationEnabled(enabled);
        if (userFeedback != null) userFeedback.showInfoMessage("Validazione", "Validazione in tempo reale " + (enabled ? "ATTIVATA" : "DISATTIVATA") + ".");
    }
    public void saveGame(File file) {
        if (file == null || gameModel.getGameState() == GameModel.GameState.NOT_INITIALIZED || gameModel.getN() == 0) {
            if (userFeedback != null) userFeedback.showErrorMessage("Salva Partita", "Nessuna partita attiva da salvare o file non specificato.");
            return;
        }
        GameStateMemento memento = gameModel.createMemento();
        if (memento == null) { if (userFeedback != null) userFeedback.showErrorMessage("Errore Salvataggio", "Impossibile creare stato da salvare."); return; }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(memento);
            if (userFeedback != null) userFeedback.showInfoMessage("Salvataggio Completato", "Partita salvata in:\n" + file.getName());
        } catch (IOException e) { if (userFeedback != null) userFeedback.showErrorMessage("Errore Salvataggio", "Errore I/O:\n" + e.getMessage()); e.printStackTrace(); }
    }
    public boolean loadGame(File file) {
        if (file == null) { if (userFeedback != null) userFeedback.showErrorMessage("Errore Caricamento", "File non specificato."); return false; }
        clearFoundSolutions(); setActiveCellFromGrid(null);
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object loadedObject = ois.readObject();
            if (loadedObject instanceof GameStateMemento) {
                gameModel.restoreFromMemento((GameStateMemento) loadedObject);
                if (userFeedback != null) userFeedback.showInfoMessage("Caricamento Completato", "Partita caricata da:\n" + file.getName());
                return true;
            } else { if (userFeedback != null) userFeedback.showErrorMessage("Errore Caricamento", "File non valido (tipo oggetto errato)."); return false; }
        } catch (IOException e) { if (userFeedback != null) userFeedback.showErrorMessage("Errore Caricamento", "Errore I/O:\n" + e.getMessage()); e.printStackTrace(); return false;
        } catch (ClassNotFoundException e) { if (userFeedback != null) userFeedback.showErrorMessage("Errore Caricamento", "Formato file non compatibile."); e.printStackTrace(); return false;
        } catch (Exception e) { if (userFeedback != null) userFeedback.showErrorMessage("Errore Caricamento", "Errore imprevisto:\n" + e.getMessage()); e.printStackTrace(); return false; }
    }
    public void validateCurrentGrid() {
        if (gameModel.getGameState() == GameModel.GameState.NOT_INITIALIZED || gameModel.getN() == 0) {
            if (userFeedback != null) userFeedback.showErrorMessage("Errore Validazione", "Nessuna partita attiva da validare."); return;
        }
        boolean isSolved = gameModel.isGameEffectivelySolved(); // Questo potrebbe cambiare lo stato interno del modello
        if (isSolved) {
            if (userFeedback != null) userFeedback.showInfoMessage("Validazione", "Congratulazioni! La soluzione è corretta.");
        } else {
            boolean allFilled = true; if (gameModel.getGrid() != null) { for (int r = 0; r < gameModel.getN(); r++) { for (int c = 0; c < gameModel.getN(); c++) { if (gameModel.getCell(r, c).isEmpty()) { allFilled = false; break; } } if (!allFilled) break; } } else allFilled = false;
            if (allFilled) { if (userFeedback != null) userFeedback.showErrorMessage("Validazione", "Griglia completa, ma soluzione non corretta."); }
            else { if (userFeedback != null) userFeedback.showInfoMessage("Validazione", "La griglia non è ancora completa."); }
        }
    }
}
