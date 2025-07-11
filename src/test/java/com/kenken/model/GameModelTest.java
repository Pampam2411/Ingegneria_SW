package com.kenken.model;

// Import delle classi del modello e dei DTO
import com.kenken.model.dto.CageDefinition;
import com.kenken.model.dto.Coordinates;
import com.kenken.model.GameModel.GameState;

// Import delle librerie di test JUnit 5
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di test per la classe GameModel.
 * Verifica le funzionalità di inizializzazione, gioco, validazione e gestione dello stato.
 */
@DisplayName("Test del GameModel di KenKen")
class GameModelTest {

    private GameModel gameModel;
    private List<CageDefinition> valid3x3CageDefs;

    /**
     * Metodo eseguito prima di ogni test.
     * Inizializza un nuovo GameModel e definisce un set valido di gabbie per un puzzle 3x3.
     */
    @BeforeEach
    void setUp() {
        gameModel = new GameModel();

        // Definiamo un puzzle 3x3 valido per i test
        // Soluzione attesa:
        // 1 2 3
        // 3 1 2
        // 2 3 1
        valid3x3CageDefs = new ArrayList<>();
        valid3x3CageDefs.add(new CageDefinition(2, OperationType.SUB, List.of(new Coordinates(0, 0), new Coordinates(1, 0)))); // 3-1=2
        valid3x3CageDefs.add(new CageDefinition(5, OperationType.ADD, List.of(new Coordinates(0, 1), new Coordinates(0, 2)))); // 2+3=5
        valid3x3CageDefs.add(new CageDefinition(2, OperationType.SUB, List.of(new Coordinates(1, 1), new Coordinates(2, 1)))); // 3-1=2
        valid3x3CageDefs.add(new CageDefinition(2, OperationType.MUL, List.of(new Coordinates(1, 2), new Coordinates(2, 2)))); // 2*1=2
        valid3x3CageDefs.add(new CageDefinition(2, OperationType.NONE, List.of(new Coordinates(2, 0)))); // Valore fisso 2
    }

    @Test
    @DisplayName("Inizializzazione corretta del gioco 3x3")
    void testInitializeGame_Success() {
        gameModel.initializeGame(3, "Test", valid3x3CageDefs, Collections.emptyMap());

        assertEquals(3, gameModel.getN());
        assertEquals("Test", gameModel.getDifficulty());
        assertEquals(GameState.PLAYING, gameModel.getGameState());
        assertNotNull(gameModel.getGrid());
        assertEquals(5, gameModel.getCages().size());

        // Verifica che la cella della gabbia NONE non sia editabile
        assertFalse(gameModel.getCell(2, 0).isEditable(), "La cella (2,0) con operatore NONE dovrebbe essere fissa.");
        assertEquals(2, gameModel.getCell(2, 0).getValue(), "Il valore della cella (2,0) dovrebbe essere 2.");
    }

    @Test
    @DisplayName("Inizializzazione fallisce con N non valido")
    void testInitializeGame_InvalidN() {
        gameModel.initializeGame(2, "Test Fallito", valid3x3CageDefs, Collections.emptyMap());
        assertEquals(GameState.ERROR, gameModel.getGameState()); //
        assertEquals(0, gameModel.getN()); //
    }

    @Test
    @DisplayName("Errore se le gabbie non coprono tutte le celle")
    void testInitializeGame_IncompleteCages() {
        // Rimuoviamo una gabbia per rendere la definizione incompleta
        valid3x3CageDefs.remove(0);
        gameModel.initializeGame(3, "Incompleto", valid3x3CageDefs, Collections.emptyMap());

        assertEquals(GameState.ERROR, gameModel.getGameState(), "Lo stato dovrebbe essere ERROR se non tutte le celle sono assegnate."); //
        assertEquals("ERROR_INCOMPLETE_CAGES", gameModel.getDifficulty()); //
    }

    @Test
    @DisplayName("Posizionamento di un numero valido in una cella")
    void testPlaceNumber_Success() {
        gameModel.initializeGame(3, "Test", valid3x3CageDefs, Collections.emptyMap());
        gameModel.placeNumber(0, 0, 1);

        assertEquals(1, gameModel.getCell(0, 0).getValue());
        assertEquals(GameState.PLAYING, gameModel.getGameState());
    }

    @Test
    @DisplayName("Lancia eccezione se si posiziona un numero non valido")
    void testPlaceNumber_InvalidValue() {
        gameModel.initializeGame(3, "Test", valid3x3CageDefs, Collections.emptyMap());

        assertThrows(IllegalArgumentException.class, () -> {
            gameModel.placeNumber(0, 0, 4); // Valore > N
        }, "Dovrebbe lanciare IllegalArgumentException per valori fuori range.");
    }

    @Test
    @DisplayName("Lancia eccezione se si modifica una cella fissa")
    void testPlaceNumber_ToNonEditableCell() {
        gameModel.initializeGame(3, "Test", valid3x3CageDefs, Collections.emptyMap());

        assertThrows(IllegalStateException.class, () -> {
            gameModel.placeNumber(2, 0, 1); // Cella (2,0) è fissa
        }, "Dovrebbe lanciare IllegalStateException per celle non editabili.");
    }

    @Test
    @DisplayName("Crea una violazione di riga e la rileva")
    void testPlaceNumber_RowViolation() {
        gameModel.initializeGame(3, "Test", valid3x3CageDefs, Collections.emptyMap());
        gameModel.setRealTimeValidationEnabled(true);

        gameModel.placeNumber(0, 0, 3);
        gameModel.placeNumber(0, 1, 3); // Viola la regola della riga

        assertEquals(GameState.CONSTRAINT_VIOLATION, gameModel.getGameState()); //
        assertTrue(gameModel.getViolatingCells().contains(new Coordinates(0, 0)), "La cella (0,0) dovrebbe essere in violazione.");
        assertTrue(gameModel.getViolatingCells().contains(new Coordinates(0, 1)), "La cella (0,1) dovrebbe essere in violazione.");
    }

    @Test
    @DisplayName("Crea una violazione di gabbia e la rileva")
    void testPlaceNumber_CageViolation() {
        gameModel.initializeGame(3, "Test", valid3x3CageDefs, Collections.emptyMap());
        gameModel.setRealTimeValidationEnabled(true);

        // La gabbia in (0,1), (0,2) ha target 5+ e operatore ADD
        gameModel.placeNumber(0, 1, 1);
        gameModel.placeNumber(0, 2, 1); // 1+1 != 5, violazione

        assertEquals(GameState.CONSTRAINT_VIOLATION, gameModel.getGameState()); //
        assertTrue(gameModel.getViolatingCells().contains(new Coordinates(0, 1)), "La cella (0,1) dovrebbe essere in violazione di gabbia.");
        assertTrue(gameModel.getViolatingCells().contains(new Coordinates(0, 2)), "La cella (0,2) dovrebbe essere in violazione di gabbia.");
    }

    @Test
    @DisplayName("Risoluzione del puzzle e stato SOLVED")
    void testPlaceNumber_SolveGame() {
        gameModel.initializeGame(3, "Test", valid3x3CageDefs, Collections.emptyMap());

        // Inseriamo la soluzione corretta
        // Riga 0
        gameModel.placeNumber(0, 0, 1);
        gameModel.placeNumber(0, 1, 2);
        gameModel.placeNumber(0, 2, 3);
        // Riga 1
        gameModel.placeNumber(1, 0, 3);
        gameModel.placeNumber(1, 1, 1);
        gameModel.placeNumber(1, 2, 2);
        // Riga 2
        // gameModel.placeNumber(2, 0, 2); // Già impostato da NONE
        gameModel.placeNumber(2, 1, 3);
        gameModel.placeNumber(2, 2, 1);

        assertEquals(GameState.SOLVED, gameModel.getGameState(), "Lo stato dovrebbe essere SOLVED dopo aver completato correttamente la griglia."); //
        assertTrue(gameModel.getViolatingCells().isEmpty(), "Non dovrebbero esserci celle in violazione."); //
    }

    @Test
    @DisplayName("Cancellazione del valore da una cella")
    void testClearCell() {
        gameModel.initializeGame(3, "Test", valid3x3CageDefs, Collections.emptyMap());
        gameModel.placeNumber(0, 0, 1);
        assertEquals(1, gameModel.getCell(0, 0).getValue());

        gameModel.clearCell(0, 0);
        assertEquals(0, gameModel.getCell(0, 0).getValue(), "Il valore della cella dovrebbe essere 0 dopo la cancellazione."); //
        assertTrue(gameModel.getCell(0, 0).isEmpty(), "La cella dovrebbe essere vuota."); //
    }
}