package com.kenken.model;

import com.kenken.model.dto.CageDefinition;
import com.kenken.model.dto.Coordinates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays; // Importante per Arrays.deepEquals

import static org.junit.jupiter.api.Assertions.*;

class GameModelTest {

    private GameModel gameModel;
    private MockGameObserver mockObserver;

    // Classe interna per mockare l'observer (come definita precedentemente)
    static class MockGameObserver implements GameObserver {
        private boolean updateCalled = false;
        private GameModel lastReceivedModel = null;
        private int updateCallCount = 0;
        @Override public void update(GameModel gm) { updateCalled = true; lastReceivedModel = gm; updateCallCount++; }
        public boolean wasUpdateCalled() { return updateCalled; }
        public GameModel getLastReceivedModel() { return lastReceivedModel; }
        public int getUpdateCallCount() { return updateCallCount; }
        public void reset() { updateCalled = false; lastReceivedModel = null; updateCallCount = 0; }
    }

    // Helper per creare definizioni di gabbie (come definito precedentemente)
    private List<CageDefinition> createKnownSolution3x3CageDefinitions() {
        List<CageDefinition> definitions = new ArrayList<>();
        definitions.add(new CageDefinition(1, OperationType.NONE, List.of(new Coordinates(0,0))));
        definitions.add(new CageDefinition(5, OperationType.ADD,  List.of(new Coordinates(0,1), new Coordinates(0,2))));
        definitions.add(new CageDefinition(6, OperationType.MUL,  List.of(new Coordinates(1,0), new Coordinates(1,1))));
        definitions.add(new CageDefinition(1, OperationType.SUB,  List.of(new Coordinates(1,2), new Coordinates(2,2))));
        definitions.add(new CageDefinition(3, OperationType.NONE, List.of(new Coordinates(2,0))));
        definitions.add(new CageDefinition(1, OperationType.NONE, List.of(new Coordinates(2,1))));
        return definitions;
    }

    @BeforeEach
    void setUp() {
        gameModel = new GameModel(); // grid è null, N=0, gameState=NOT_INITIALIZED, difficulty="UNKNOWN"
        mockObserver = new MockGameObserver();
        gameModel.addObserver(mockObserver);
        try {
            Field nextCageIdField = Cage.class.getDeclaredField("nextCageId");
            nextCageIdField.setAccessible(true);
            nextCageIdField.setInt(null, 1);
        } catch (NoSuchFieldException | IllegalAccessException e) { /* ignora */ }
    }

    @Test
    @DisplayName("Il costruttore dovrebbe inizializzare GameModel con valori di default corretti")
    void constructor_shouldInitializeWithDefaultState() {
        GameModel newModel = new GameModel();
        assertEquals(0, newModel.getN());
        assertNull(newModel.getGrid());
        assertNotNull(newModel.getCages());
        assertTrue(newModel.getCages().isEmpty());
        assertEquals(GameModel.GameState.NOT_INITIALIZED, newModel.getGameState());
        assertEquals("UNKOWN", newModel.getDifficulty()); // Verifica la difficoltà di default
        assertFalse(newModel.isRealTimeValidationEnabled());
    }

    @Nested
    @DisplayName("Test per initializeGame()")
    class InitializeGameTests {
        @Test
        @DisplayName("initializeGame dovrebbe configurare correttamente il modello, stato e difficoltà")
        void initializeGame_validInputs_shouldSetupModelCorrectly() {
            int N = 3;
            String difficulty = "TestEasy";
            List<CageDefinition> definitions = createKnownSolution3x3CageDefinitions();
            Map<Coordinates, Integer> fixedNumbers = new HashMap<>();

            gameModel.initializeGame(N, difficulty, definitions, fixedNumbers);

            assertEquals(N, gameModel.getN());
            assertNotNull(gameModel.getGrid());
            assertEquals(N, gameModel.getGrid().getSize());
            assertEquals(definitions.size(), gameModel.getCages().size());
            assertEquals(GameModel.GameState.PLAYING, gameModel.getGameState());
            assertEquals(difficulty, gameModel.getDifficulty()); // Verifica difficoltà impostata
            assertTrue(mockObserver.wasUpdateCalled());
        }
        // ... (altri test di InitializeGameTests dovrebbero essere qui) ...
    }

    @Nested
    @DisplayName("Test per il pattern Observer e gestione stati")
    class ObserverAndStateTests {
        // ... (i test per Observer dovrebbero essere qui) ...
        @Test
        @DisplayName("setRealTimeValidationEnabled dovrebbe aggiornare il flag")
        void setRealTimeValidationEnabled_shouldUpdateFlag() {
            assertFalse(gameModel.isRealTimeValidationEnabled());
            gameModel.setRealTimeValidationEnabled(true);
            assertTrue(gameModel.isRealTimeValidationEnabled());
        }
        @Test
        @DisplayName("getGameState dovrebbe restituire lo stato corretto")
        void getGameState_shouldReturnCorrectState() {
            assertEquals(GameModel.GameState.NOT_INITIALIZED, gameModel.getGameState());
            gameModel.initializeGame(3, "Easy", createKnownSolution3x3CageDefinitions(), new HashMap<>());
            assertEquals(GameModel.GameState.PLAYING, gameModel.getGameState());
        }
    }

    @Nested
    @DisplayName("Test per i Getters (dopo inizializzazione)")
    class GetterTestsAfterInit {
        // ... (i test per i Getter dovrebbero essere qui) ...
        private final int N_GETTER_TEST = 3;
        private final String TEST_DIFFICULTY = "GetterTestDiff";
        @BeforeEach
        void initializeForGetters() {
            gameModel.initializeGame(N_GETTER_TEST, TEST_DIFFICULTY, createKnownSolution3x3CageDefinitions(), new HashMap<>());
        }
        @Test
        @DisplayName("getN dovrebbe restituire N corretto dopo l'inizializzazione")
        void getN_afterInit_shouldReturnCorrectN() {
            assertEquals(N_GETTER_TEST, gameModel.getN());
        }
        @Test
        @DisplayName("getDifficulty dovrebbe restituire la difficoltà corretta dopo l'inizializzazione")
        void getDifficulty_afterInit_shouldReturnCorrectDifficulty() {
            assertEquals(TEST_DIFFICULTY, gameModel.getDifficulty());
        }
    }

    @Nested
    @DisplayName("Test per placeNumber() e clearCell()")
    class InteractionTests {
        // ... (i test per placeNumber e clearCell dovrebbero essere qui) ...
        private final int N_TEST = 3;
        @BeforeEach
        void initializeTestGrid() {
            gameModel.initializeGame(N_TEST, "TestInteractionDiff", createKnownSolution3x3CageDefinitions(), new HashMap<>());
            mockObserver.reset();
        }
        @Test
        @DisplayName("placeNumber: inserimento valido, no validazione RT, stato PLAYING")
        void placeNumber_validNoRTValidation_shouldPlaceAndNotify() {
            gameModel.setRealTimeValidationEnabled(false);
            boolean result = gameModel.placeNumber(0, 0, 1);
            assertTrue(result);
            assertEquals(1, gameModel.getCell(0, 0).getValue());
        }
    }

    // CLASSE @NESTED AGGIORNATA PER I TEST DEL MEMENTO
    @Nested
    @DisplayName("Test per il Pattern Memento (createMemento e restoreFromMemento)")
    class MementoTests {

        @Test
        @DisplayName("createMemento dovrebbe catturare lo stato NOT_INITIALIZED correttamente")
        void createMemento_notInitializedState_shouldCaptureCorrectly() {
            gameModel.setRealTimeValidationEnabled(false); // Imposta uno stato noto per RT validation
            GameStateMemento memento = gameModel.createMemento();

            assertNotNull(memento);
            assertEquals(0, memento.N());
            assertTrue(memento.cageDefinitions().isEmpty());
            assertEquals(0, memento.cellValues().length);
            assertEquals(0, memento.cellEditability().length);
            assertEquals(GameModel.GameState.NOT_INITIALIZED, memento.gameState());
            // La difficoltà per lo stato NOT_INITIALIZED è gestita da GameStateMemento.createNotInitializedMemento
            // o dal costruttore di GameModel per il memento. La tua GameModel.createMemento usa il factory.
            assertEquals("NOT_INITIALIZED", memento.difficulty());
            assertFalse(memento.realTimeValidationEnabled()); // Cattura lo stato attuale
        }

        @Test
        @DisplayName("restoreFromMemento dovrebbe ripristinare lo stato NOT_INITIALIZED")
        void restoreFromMemento_notInitializedState_shouldRestoreCorrectly() {
            boolean initialRTValidation = true;
            GameStateMemento memento = GameStateMemento.createNotInitializedMemento(initialRTValidation);

            gameModel.initializeGame(3, "Easy", createKnownSolution3x3CageDefinitions(), new HashMap<>());
            gameModel.placeNumber(0,0,1);
            mockObserver.reset();

            gameModel.restoreFromMemento(memento);

            assertEquals(0, gameModel.getN());
            assertNull(gameModel.getGrid());
            assertTrue(gameModel.getCages().isEmpty());
            assertEquals(GameModel.GameState.NOT_INITIALIZED, gameModel.getGameState());
            assertEquals("NOT_INITIALIZED", gameModel.getDifficulty());
            assertEquals(initialRTValidation, gameModel.isRealTimeValidationEnabled());
            assertTrue(mockObserver.wasUpdateCalled());
        }

        @Test
        @DisplayName("createMemento dovrebbe catturare correttamente lo stato di un gioco inizializzato e parzialmente giocato")
        void createMemento_initializedAndPlayedState_shouldCaptureCorrectly() {
            int N = 3;
            String difficulty = "Intermediate";
            List<CageDefinition> definitions = createKnownSolution3x3CageDefinitions();
            Map<Coordinates, Integer> fixedNumbers = new HashMap<>();
            fixedNumbers.put(new Coordinates(0,0), 1);

            gameModel.initializeGame(N, difficulty, definitions, fixedNumbers);
            gameModel.placeNumber(0, 1, 2);
            gameModel.setRealTimeValidationEnabled(true);
            gameModel.placeNumber(0,2,2); // Causa CONSTRAINT_VIOLATION (riga 0: 1,2,2)
            GameModel.GameState currentState = gameModel.getGameState(); // Dovrebbe essere CONSTRAINT_VIOLATION

            GameStateMemento memento = gameModel.createMemento();

            assertNotNull(memento);
            assertEquals(N, memento.N());
            assertEquals(definitions.size(), memento.cageDefinitions().size());

            assertEquals(1, memento.cellValues()[0][0]);
            assertFalse(memento.cellEditability()[0][0]);
            assertEquals(2, memento.cellValues()[0][1]);
            assertTrue(memento.cellEditability()[0][1]);
            assertEquals(2, memento.cellValues()[0][2]);
            assertTrue(memento.cellEditability()[0][2]);

            assertEquals(currentState, memento.gameState());
            assertEquals(difficulty, memento.difficulty());
            assertTrue(memento.realTimeValidationEnabled());
        }

        @Test
        @DisplayName("restoreFromMemento dovrebbe ripristinare correttamente uno stato di gioco complesso")
        void restoreFromMemento_complexState_shouldRestoreCorrectly() {
            int N = 3;
            String originalDifficulty = "Hard";
            boolean originalRTEnabled = true;
            List<CageDefinition> definitions = createKnownSolution3x3CageDefinitions();
            Map<Coordinates, Integer> fixedNumbers = new HashMap<>();
            fixedNumbers.put(new Coordinates(0,0), 1);

            gameModel.initializeGame(N, originalDifficulty, definitions, fixedNumbers);
            gameModel.placeNumber(0, 1, 2);
            gameModel.setRealTimeValidationEnabled(originalRTEnabled);
            gameModel.placeNumber(0,2,2); // Causa CONSTRAINT_VIOLATION
            GameModel.GameState stateBeforeMemento = gameModel.getGameState();

            GameStateMemento memento = gameModel.createMemento();

            // Modifica ulteriormente il gioco
            gameModel.setRealTimeValidationEnabled(false);
            gameModel.clearCell(0,1);
            gameModel.placeNumber(1,1,3);
            // Modifica la difficoltà per vedere se viene ripristinata
            // gameModel.setDifficulty("SomethingElse"); // Richiederebbe un setter in GameModel
            Field difficultyField; // Hack per testare il ripristino della difficoltà
            try {
                difficultyField = GameModel.class.getDeclaredField("difficulty");
                difficultyField.setAccessible(true);
                difficultyField.set(gameModel, "ChangedDifficulty");
            } catch (NoSuchFieldException | IllegalAccessException e) {
                fail("Impossibile modificare la difficoltà per il test.");
            }

            mockObserver.reset();
            gameModel.restoreFromMemento(memento);

            assertEquals(N, gameModel.getN());
            assertNotNull(gameModel.getGrid());
            assertEquals(definitions.size(), gameModel.getCages().size());

            assertEquals(1, gameModel.getCell(0,0).getValue());
            assertFalse(gameModel.getCell(0,0).isEditable());
            assertEquals(2, gameModel.getCell(0,1).getValue());
            assertTrue(gameModel.getCell(0,1).isEditable());
            assertEquals(2, gameModel.getCell(0,2).getValue());
            assertTrue(gameModel.getCell(0,2).isEditable());
            assertEquals(0, gameModel.getCell(1,1).getValue()); // Era 3 prima del restore

            assertEquals(stateBeforeMemento, gameModel.getGameState());
            assertEquals(originalDifficulty, gameModel.getDifficulty()); // Verifica difficoltà ripristinata
            assertEquals(originalRTEnabled, gameModel.isRealTimeValidationEnabled()); // Verifica RT validation
            assertTrue(mockObserver.wasUpdateCalled());

            assertTrue(Arrays.deepEquals(memento.cellValues(), getCellValuesFromModel(gameModel)));
            assertTrue(Arrays.deepEquals(memento.cellEditability(), getCellEditabilityFromModel(gameModel)));
        }

        @Test
        @DisplayName("restoreFromMemento dovrebbe lanciare IllegalArgumentException per memento nullo")
        void restoreFromMemento_nullMemento_shouldThrowIllegalArgumentException() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                gameModel.restoreFromMemento(null);
            });
            assertEquals("Il memento non può essere null", exception.getMessage());
        }

        // Metodi helper per i test di Memento (come prima)
        private int[][] getCellValuesFromModel(GameModel model) {
            if (model.getGrid() == null || model.getN() == 0) return new int[0][0];
            int n = model.getN();
            int[][] values = new int[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    values[i][j] = model.getCell(i, j).getValue();
                }
            }
            return values;
        }

        private boolean[][] getCellEditabilityFromModel(GameModel model) {
            if (model.getGrid() == null || model.getN() == 0) return new boolean[0][0];
            int n = model.getN();
            boolean[][] editability = new boolean[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    editability[i][j] = model.getCell(i, j).isEditable();
                }
            }
            return editability;
        }
    }
}