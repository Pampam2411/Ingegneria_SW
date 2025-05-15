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
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class GameController {

    private final GameModel gameModel;
    private final PuzzleGenerator puzzleGenerator;
    private final UserFeedback userFeedback;
    private List<Grid> foundSolutions;
    private int currentSolutionIndex;
    private static final int DEFAULT_MAX_SOLUTIONS_TO_FIND = 100;

    private Coordinates currentlySelectedCellInGrid;

    public GameController(GameModel model, UserFeedback feedback) {
        this.gameModel = model;
        this.puzzleGenerator = new PuzzleGenerator();
        this.userFeedback = feedback;
        this.foundSolutions = new ArrayList<>();
        this.currentSolutionIndex = -1;
        this.currentlySelectedCellInGrid = null;
    }

    public void setActiveCellFromGrid(Coordinates coord) {
        this.currentlySelectedCellInGrid = coord;
        System.out.println("GameController: Cella attiva impostata a: " + (coord != null ? coord.toString() : "null"));
    }

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

    public void solvePuzzle() {
        System.out.println("GameController: Richiesta Risoluzione Puzzle.");
        if (gameModel.getGameState() == GameModel.GameState.NOT_INITIALIZED || gameModel.getN() == 0) {
            if (userFeedback != null) userFeedback.showErrorMessage("Error", "No active game to resolve.");
            return;
        }
        clearFoundSolutions();
        setActiveCellFromGrid(null);
        KenKenSolver solver = new KenKenSolver(gameModel.getGrid(), gameModel.getCages(), gameModel.getN());
        System.out.println("GameController: Avvio KenKenSolver per trovare max " + DEFAULT_MAX_SOLUTIONS_TO_FIND + " soluzioni...");
        this.foundSolutions = solver.solve(DEFAULT_MAX_SOLUTIONS_TO_FIND);

        if (this.foundSolutions != null && !this.foundSolutions.isEmpty()) {
            this.currentSolutionIndex = 0;
            displaySolutionAtIndex(this.currentSolutionIndex);
            String msg = "Found " + this.foundSolutions.size() + " solution.";
            if (this.foundSolutions.size() > 1) {
                msg += "\nUse browse command to show all solutions.";
            }
            if (userFeedback != null) userFeedback.showInfoMessage("Solve Puzzle", msg);
        } else {
            if (userFeedback != null) userFeedback.showErrorMessage("Solve Puzzle", "Not found any solution.");
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
            gameModel.notifyObservers();
        }
    }

    public void showPreviousSolution() {
        if (canShowPreviousSolution()) {
            currentSolutionIndex--;
            displaySolutionAtIndex(currentSolutionIndex);
            gameModel.notifyObservers();
        }
    }

    public boolean canShowNextSolution() {
        return foundSolutions != null && currentSolutionIndex < foundSolutions.size() - 1; }

    public boolean canShowPreviousSolution() {
        return foundSolutions != null && currentSolutionIndex > 0; }

    public int getCurrentSolutionIndex() {
        return currentSolutionIndex; }

    public int getTotalSolutionsFound() {
        return foundSolutions != null ? foundSolutions.size() : 0; }

    public void clearFoundSolutions() {
        if (this.foundSolutions != null) this.foundSolutions.clear();
        this.currentSolutionIndex = -1;
        if (gameModel != null) gameModel.notifyObservers();
    }

    public void placeNumberInCell(int row, int col, int value) {
        if (gameModel.getGameState() == GameModel.GameState.PLAYING || gameModel.getGameState() == GameModel.GameState.CONSTRAINT_VIOLATION) {
            try { gameModel.placeNumber(row, col, value); }
            catch (IllegalArgumentException e) { if (userFeedback != null) userFeedback.showErrorMessage("Invalid Input", "Value '" + value + "' invalid.\nEnter a number between 1 and " + gameModel.getN() + "."); }
            catch (IllegalStateException e) { if (userFeedback != null) userFeedback.showErrorMessage("Action Allowed", "Cells (" + (row + 1) + "," + (col + 1) + ") it not editable."); }
        } else { if (userFeedback != null) userFeedback.showInfoMessage("Action Allowed", "It is not possible to enter numbers now. Game status: " + gameModel.getGameState()); }
    }

    public void clearCell(int row, int col) {
        if (gameModel.getGameState() == GameModel.GameState.PLAYING || gameModel.getGameState() == GameModel.GameState.CONSTRAINT_VIOLATION) {
            try { gameModel.clearCell(row, col); }
            catch (IllegalStateException e) { if (userFeedback != null) userFeedback.showErrorMessage("Action Allowed", "Cells (" + (row + 1) + "," + (col + 1) + ") it not editable."); }
        } else { if (userFeedback != null) userFeedback.showInfoMessage("Action Allowed", "It is not possible to enter numbers now. Game status " + gameModel.getGameState());}
    }

    public void setRealTimeValidation(boolean enabled) {
        gameModel.setRealTimeValidationEnabled(enabled);
        if (userFeedback != null) userFeedback.showInfoMessage("Validation", "Validation realtime " + (enabled ? "Active" : "Deactivated") + ".");
    }

    public void saveGame(File file) {
        if (file == null || gameModel.getGameState() == GameModel.GameState.NOT_INITIALIZED || gameModel.getN() == 0) {
            if (userFeedback != null) userFeedback.showErrorMessage("Save Game", "No active game to save or file not specified");
            return;
        }
        GameStateMemento memento = gameModel.createMemento();
        if (memento == null) { if (userFeedback != null) userFeedback.showErrorMessage("Save Error", "Unable to create state to save."); return; }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(memento);
            if (userFeedback != null) userFeedback.showInfoMessage("Save Completed", "Game save in:\n" + file.getName());
        } catch (IOException e) { if (userFeedback != null) userFeedback.showErrorMessage("Save Error", "Error I/O:\n" + e.getMessage());}
    }

    public boolean loadGame(File file) {
        if (file == null) { if (userFeedback != null) userFeedback.showErrorMessage("Load Error", "File not specified."); return false; }
        clearFoundSolutions(); setActiveCellFromGrid(null);
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object loadedObject = ois.readObject();
            if (loadedObject instanceof GameStateMemento) {
                gameModel.restoreFromMemento((GameStateMemento) loadedObject);
                if (userFeedback != null) userFeedback.showInfoMessage("Load Completed", "Load game from:\n" + file.getName());
                return true;
            } else { if (userFeedback != null) userFeedback.showErrorMessage("Error Load", "File not valid (error object type)."); return false; }
        } catch (IOException e) { if (userFeedback != null) userFeedback.showErrorMessage("Method…", "Error I/O:\n" + e.getMessage()); return false;
        } catch (ClassNotFoundException e) { if (userFeedback != null) userFeedback.showErrorMessage("Method…", "Incompatible file format."); return false;
        } catch (Exception e) { if (userFeedback != null) userFeedback.showErrorMessage("Method…", "Unexpected error:\n" + e.getMessage()); return false; }
    }
}
