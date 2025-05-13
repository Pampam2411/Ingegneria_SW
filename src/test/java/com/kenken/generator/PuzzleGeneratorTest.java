package com.kenken.generator;

import com.kenken.model.Cage;
import com.kenken.model.Grid;
import com.kenken.model.OperationType;
import com.kenken.model.dto.CageDefinition;
import com.kenken.model.dto.Coordinates;
import com.kenken.solver.KenKenSolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class PuzzleGeneratorTest {

    private PuzzleGenerator generator;
    private static final int TIMEOUT_SECONDS_N3 = 30;
    private static final int TIMEOUT_SECONDS_N4 = 60;
    private static final int TIMEOUT_SECONDS_N5 = 120;
    private static final int TIMEOUT_SECONDS_N6 = 240;
    private static final int GENERATION_REPETITIONS = 3; // Mantieni le ripetizioni per testare la robustezza della generazione

    private static void resetGlobalCageId() {
        try {
            Field field = Cage.class.getDeclaredField("nextCageId");
            field.setAccessible(true);
            field.set(null, 1);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Attenzione: Impossibile resettare nextCageId in Cage. " + e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        generator = new PuzzleGenerator();
        resetGlobalCageId();
    }

    // --- Test per DifficultyConfiguration (invariati) ---
    @Test
    @DisplayName("DifficultyConfig: Operatori validi per combinazioni N/Difficoltà corrette")
    void testAllowedOperators_ValidCombinations() {
        assertDoesNotThrow(() -> PuzzleGenerator.DifficultyConfiguration.getAllowedOperators(3, PuzzleGenerator.DIFFICULTY_EASY));
        assertDoesNotThrow(() -> PuzzleGenerator.DifficultyConfiguration.getAllowedOperators(4, PuzzleGenerator.DIFFICULTY_MEDIUM));
        assertDoesNotThrow(() -> PuzzleGenerator.DifficultyConfiguration.getAllowedOperators(6, PuzzleGenerator.DIFFICULTY_HARD));
    }

    @ParameterizedTest
    @CsvSource({
            "2, EASY, N troppo piccolo",
            "7, EASY, N troppo grande",
            "3, MEDIUM, MEDIUM non per N=3",
            "5, HARD, HARD non per N=5",
            "4, INVALID_DIFFICULTY, Difficoltà non esistente",
            "4, '', Difficoltà vuota",
            "4, '  ', Difficoltà con solo spazi"
    })
    @DisplayName("DifficultyConfig: Eccezione per combinazioni N/Difficoltà non valide")
    void testAllowedOperators_InvalidCombinations_ThrowsException(int N, String difficulty, String testName) {
        assertThrows(IllegalArgumentException.class, () -> PuzzleGenerator.DifficultyConfiguration.getAllowedOperators(N, difficulty),
                "Avrebbe dovuto lanciare IllegalArgumentException per: " + testName);
    }

    @Test
    @DisplayName("DifficultyConfig: Eccezione per difficoltà nulla")
    void testAllowedOperators_NullDifficulty_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> PuzzleGenerator.DifficultyConfiguration.getAllowedOperators(4, null));
    }

    // --- Test per il metodo generatePuzzle ---

    @RepeatedTest(value = GENERATION_REPETITIONS, name = "3x3 EASY Gen #{currentRepetition}")
    @Timeout(value = TIMEOUT_SECONDS_N3, unit = TimeUnit.SECONDS)
    void testGeneratePuzzle_3x3_Easy_IsSolvable( /* RepetitionInfo repetitionInfo */ ) {
        System.out.println("Testing 3x3 EASY generation (solvable)...");
        List<CageDefinition> definitions = generator.generatePuzzle(3, PuzzleGenerator.DIFFICULTY_EASY);
        assertSolvablePuzzleProperties(3, definitions, PuzzleGenerator.DifficultyConfiguration.getAllowedOperators(3, PuzzleGenerator.DIFFICULTY_EASY));
    }

    @RepeatedTest(value = GENERATION_REPETITIONS, name = "4x4 EASY Gen #{currentRepetition}")
    @Timeout(value = TIMEOUT_SECONDS_N4, unit = TimeUnit.SECONDS)
    void testGeneratePuzzle_4x4_Easy_IsSolvable( /* RepetitionInfo repetitionInfo */ ) {
        System.out.println("Testing 4x4 EASY generation (solvable)...");
        List<CageDefinition> definitions = generator.generatePuzzle(4, PuzzleGenerator.DIFFICULTY_EASY);
        assertSolvablePuzzleProperties(4, definitions, PuzzleGenerator.DifficultyConfiguration.getAllowedOperators(4, PuzzleGenerator.DIFFICULTY_EASY));
    }

    @RepeatedTest(value = GENERATION_REPETITIONS, name = "4x4 MEDIUM Gen #{currentRepetition}")
    @Timeout(value = TIMEOUT_SECONDS_N4, unit = TimeUnit.SECONDS)
    void testGeneratePuzzle_4x4_Medium_IsSolvable( /* RepetitionInfo repetitionInfo */ ) {
        System.out.println("Testing 4x4 MEDIUM generation (solvable)...");
        List<CageDefinition> definitions = generator.generatePuzzle(4, PuzzleGenerator.DIFFICULTY_MEDIUM);
        assertSolvablePuzzleProperties(4, definitions, PuzzleGenerator.DifficultyConfiguration.getAllowedOperators(4, PuzzleGenerator.DIFFICULTY_MEDIUM));
    }

    @RepeatedTest(value = GENERATION_REPETITIONS, name = "5x5 MEDIUM Gen #{currentRepetition}")
    @Timeout(value = TIMEOUT_SECONDS_N5, unit = TimeUnit.SECONDS)
    void testGeneratePuzzle_5x5_Medium_IsSolvable( /* RepetitionInfo repetitionInfo */ ) {
        System.out.println("Testing 5x5 MEDIUM generation (solvable)...");
        List<CageDefinition> definitions = generator.generatePuzzle(5, PuzzleGenerator.DIFFICULTY_MEDIUM);
        assertSolvablePuzzleProperties(5, definitions, PuzzleGenerator.DifficultyConfiguration.getAllowedOperators(5, PuzzleGenerator.DIFFICULTY_MEDIUM));
    }

    @RepeatedTest(value = GENERATION_REPETITIONS, name = "6x6 HARD Gen #{currentRepetition}")
    @Timeout(value = TIMEOUT_SECONDS_N6 + 60, unit = TimeUnit.SECONDS)
    void testGeneratePuzzle_6x6_Hard_IsSolvable( /* RepetitionInfo repetitionInfo */ ) {
        System.out.println("Testing 6x6 HARD generation (solvable)...");
        List<CageDefinition> definitions = generator.generatePuzzle(6, PuzzleGenerator.DIFFICULTY_HARD);
        assertSolvablePuzzleProperties(6, definitions, PuzzleGenerator.DifficultyConfiguration.getAllowedOperators(6, PuzzleGenerator.DIFFICULTY_HARD));
    }

    @Test
    @DisplayName("Generate: Verifica che il generatore completi per una configurazione valida")
    void testGeneratePuzzle_CompletesForValidConfig() {
        System.out.println("Testing generazione base senza eccezioni...");
        assertDoesNotThrow(() -> {
            generator.generatePuzzle(3, PuzzleGenerator.DIFFICULTY_EASY);
        }, "La generazione per una configurazione valida non dovrebbe fallire catastroficamente.");
    }


    // --- Metodi Helper per i Test di PuzzleGenerator ---

    /**
     * Metodo helper principale per asserire le proprietà di un puzzle generato,
     * focalizzandosi sulla validità (risolvibilità) piuttosto che sull'unicità.
     */
    private void assertSolvablePuzzleProperties(int N, List<CageDefinition> definitions, Set<OperationType> expectedOperatorSet) {
        assertNotNull(definitions, "N=" + N + ": La lista delle definizioni non dovrebbe essere nulla.");
        assertFalse(definitions.isEmpty(), "N=" + N + ": Dovrebbero essere generate delle definizioni di gabbie.");

        assertTrue(areAllCellsCovered(N, definitions), "N=" + N + ": Non tutte le celle sono coperte da una gabbia.");
        assertTrue(areOperatorsValid(definitions, expectedOperatorSet), "N=" + N + ": Usati operatori non permessi per la difficoltà.");
        assertTrue(areCageSizesConsistentWithOperators(definitions), "N=" + N + ": Dimensioni gabbie non consistenti con gli operatori (SUB/DIV/NONE).");

        // Verifica che il puzzle sia RISOLVIBILE (abbia almeno una soluzione)
        TestPuzzleVerificationResult verification = verifyGeneratedPuzzleInternally(N, definitions);
        assertTrue(verification.isValid(),
                "N=" + N + ": Puzzle generato dovrebbe essere valido (risolvibile). Num solutions trovate: " + verification.numberOfSolutions());

        // Logga il numero di soluzioni per informazione, ma non far fallire il test se > 1
        System.out.println("INFO: Puzzle N=" + N + " generato con " + verification.numberOfSolutions() + " soluzione(i).");
    }

    // areAllCellsCovered, areOperatorsValid, areCageSizesConsistentWithOperators (invariati)
    private boolean areAllCellsCovered(int N, List<CageDefinition> definitions) {
        Set<Coordinates> coveredCells = new HashSet<>();
        for (CageDefinition def : definitions) {
            if (def.cellsCoordinates() == null) { System.err.println("Errore copertura: Def. gabbia con coord nulle."); return false; }
            for (Coordinates coord : def.cellsCoordinates()) {
                if (coord.row() < 0 || coord.row() >= N || coord.col() < 0 || coord.col() >= N) { System.err.println("Errore copertura: Cella fuori limiti " + coord); return false; }
                coveredCells.add(coord);
            }
        }
        if (coveredCells.size() != N * N) {
            System.err.println("Errore copertura: Celle coperte " + coveredCells.size() + " != N*N " + (N * N));
            return false;
        }
        return true;
    }

    private boolean areOperatorsValid(List<CageDefinition> definitions, Set<OperationType> allowedOperators) {
        for (CageDefinition def : definitions) {
            if (!allowedOperators.contains(def.operationType())) {
                System.err.println("Errore operatori: Op non permesso " + def.operationType() + ". Permessi: " + allowedOperators);
                return false;
            }
        }
        return true;
    }

    private boolean areCageSizesConsistentWithOperators(List<CageDefinition> definitions) {
        for (CageDefinition def : definitions) {
            int cageSize = def.cellsCoordinates().size();
            OperationType op = def.operationType();
            if (op == OperationType.NONE && cageSize != 1) { System.err.println("Errore dim: NONE con " + cageSize + " celle."); return false; }
            if ((op == OperationType.SUB || op == OperationType.DIV) && cageSize != 2) { System.err.println("Errore dim: " + op + " con " + cageSize + " celle."); return false; }
            if ((op == OperationType.ADD || op == OperationType.MUL) && cageSize < 1) { System.err.println("Errore dim: " + op + " con " + cageSize + " celle."); return false; }
        }
        return true;
    }

    /**
     * @param isValid True se numberOfSolutions > 0
     */ // Replica di PuzzleVerificationResult (invariata)
        private record TestPuzzleVerificationResult(boolean isValid, int numberOfSolutions) {
    }

    // verifyGeneratedPuzzleInternally (invariato - chiede sempre 2 soluzioni al solver)
    private TestPuzzleVerificationResult verifyGeneratedPuzzleInternally(int N, List<CageDefinition> definitions) {
        Grid puzzleGrid = new Grid(N);
        List<Cage> actualCages = new ArrayList<>();
        for (CageDefinition def : definitions) {
            Cage newCage = new Cage(def.targetValue(), def.operationType());
            if (def.cellsCoordinates() == null || def.cellsCoordinates().isEmpty()) { return new TestPuzzleVerificationResult(false, 0); }
            for (Coordinates coord : def.cellsCoordinates()) {
                if (coord.row() < 0 || coord.row() >= N || coord.col() < 0 || coord.col() >= N) { return new TestPuzzleVerificationResult(false,  0); }
                newCage.addCell(puzzleGrid.getCell(coord.row(), coord.col()));
            }
            actualCages.add(newCage);
        }
        KenKenSolver solver = new KenKenSolver(puzzleGrid, actualCages, N);
        List<Grid> solutions = solver.solve(2); // Chiede sempre fino a 2 per sapere se è unica o multipla
        int numSolutions = solutions.size();
        return new TestPuzzleVerificationResult(numSolutions > 0, numSolutions);
    }
}