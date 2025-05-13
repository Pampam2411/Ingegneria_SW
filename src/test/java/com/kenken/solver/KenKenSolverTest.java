package com.kenken.solver;

import com.kenken.model.Cage;
import com.kenken.model.Cell;
import com.kenken.model.Grid;
import com.kenken.model.OperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test del KenKenSolver")
class KenKenSolverTest {

    // Resetta lo stato statico di Cage.nextCageId prima di ogni test
    // per assicurare l'isolamento dei test riguardo agli ID delle gabbie.
    @BeforeEach
    void resetCageIds() {
        try {
            java.lang.reflect.Field field = Cage.class.getDeclaredField("nextCageId");
            field.setAccessible(true);
            field.set(null, 1); // Resetta a 1 (o al tuo valore iniziale)
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            fail("Impossibile resettare nextCageId in Cage.class: " + e.getMessage());
        }
    }


    private void assertSolutionCorrect(Grid solution, int N, int[][] expectedValues) {
        assertNotNull(solution, "La soluzione non dovrebbe essere nulla.");
        assertEquals(N, solution.getSize(), "La dimensione della griglia della soluzione non è corretta.");
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                assertEquals(expectedValues[r][c], solution.getCell(r, c).getValue(),
                        "Valore errato nella cella (" + r + "," + c + ")");
            }
        }
    }

    private void printGrid(Grid grid, String title) {
        System.out.println(title);
        if (grid == null) {
            System.out.println("Griglia nulla.");
            return;
        }
        grid.printToConsole();
        System.out.println("-----");
    }


    @Test
    @DisplayName("Puzzle 3x3 Semplice - Soluzione Unica (Addizione/Nessuna)")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testSimple3x3_SingleSolution_AdditionNone() {
        int N = 3;
        Grid puzzleGrid = new Grid(N); // Griglia vuota iniziale
        List<Cage> cages = new ArrayList<>();
        resetCageIds(); // Resetta gli ID delle gabbie per l'indipendenza del test

        // Definiamo le gabbie per il puzzle 3x3
        // Soluzione: { {1,2,3}, {3,1,2}, {2,3,1} }

        // C1: (0,0), (1,0) = 4+  (cella 0,0 val 1; cella 1,0 val 3 -> 1+3=4)
        Cage c1 = new Cage(4, OperationType.ADD);
        c1.addCell(puzzleGrid.getCell(0,0)); c1.addCell(puzzleGrid.getCell(1,0)); cages.add(c1);

        // C2: (0,1) = 2= (cella 0,1 val 2)
        Cage c2 = new Cage(2, OperationType.NONE);
        c2.addCell(puzzleGrid.getCell(0,1)); cages.add(c2);

        // C3: (0,2), (1,2) = 5+ (cella 0,2 val 3; cella 1,2 val 2 -> 3+2=5)
        Cage c3 = new Cage(5, OperationType.ADD);
        c3.addCell(puzzleGrid.getCell(0,2)); c3.addCell(puzzleGrid.getCell(1,2)); cages.add(c3);

        // C4: (1,1), (2,1) = 4+ (cella 1,1 val 1; cella 2,1 val 3 -> 1+3=4)
        Cage c4 = new Cage(4, OperationType.ADD);
        c4.addCell(puzzleGrid.getCell(1,1)); c4.addCell(puzzleGrid.getCell(2,1)); cages.add(c4);

        // C5: (2,0) = 2= (cella 2,0 val 2)
        Cage c5 = new Cage(2, OperationType.NONE);
        c5.addCell(puzzleGrid.getCell(2,0)); cages.add(c5);

        // C6: (2,2) = 1= (cella 2,2 val 1)
        Cage c6 = new Cage(1, OperationType.NONE);
        c6.addCell(puzzleGrid.getCell(2,2)); cages.add(c6);


        KenKenSolver solver = new KenKenSolver(puzzleGrid, cages, N);
        List<Grid> solutions = solver.solve(1); // Cerca al massimo 1 soluzione

        assertEquals(1, solutions.size(), "Puzzle 3x3 Semplice dovrebbe avere 1 soluzione.");
        if (!solutions.isEmpty()) {
            printGrid(solutions.getFirst(), "Soluzione 3x3 Semplice:");
            int[][] expectedSolution = {
                    {1, 2, 3},
                    {3, 1, 2},
                    {2, 3, 1}
            };
            assertSolutionCorrect(solutions.getFirst(), N, expectedSolution);
        }
    }

    @Test
    @DisplayName("Puzzle 3x3 - Nessuna Soluzione")
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    void test3x3_NoSolution() {
        int N = 3;
        Grid puzzleGrid = new Grid(N);
        List<Cage> cages = new ArrayList<>();

        // Gabbia impossibile: 7+ con due celle in un 3x3 (max 3+3 = 6)
        Cage c1 = new Cage(7, OperationType.ADD);
        c1.addCell(puzzleGrid.getCell(0, 0));
        c1.addCell(puzzleGrid.getCell(0, 1));
        cages.add(c1);

        // Riempiamo il resto per completezza, anche se la prima gabbia è già un problema
        Cage c2 = new Cage(3, OperationType.NONE);
        c2.addCell(puzzleGrid.getCell(0, 2));
        cages.add(c2);
        Cage c3 = new Cage(1, OperationType.NONE);
        c3.addCell(puzzleGrid.getCell(1, 0));
        cages.add(c3);
        Cage c4 = new Cage(2, OperationType.NONE);
        c4.addCell(puzzleGrid.getCell(1, 1));
        cages.add(c4);
        Cage c5 = new Cage(3, OperationType.NONE);
        c5.addCell(puzzleGrid.getCell(1, 2));
        cages.add(c5);
        Cage c6 = new Cage(1, OperationType.NONE);
        c6.addCell(puzzleGrid.getCell(2, 0));
        cages.add(c6);
        Cage c7 = new Cage(2, OperationType.NONE);
        c7.addCell(puzzleGrid.getCell(2, 1));
        cages.add(c7);
        Cage c8 = new Cage(3, OperationType.NONE);
        c8.addCell(puzzleGrid.getCell(2, 2));
        cages.add(c8);


        KenKenSolver solver = new KenKenSolver(puzzleGrid, cages, N);
        List<Grid> solutions = solver.solve(-1); // Cerca tutte le soluzioni

        assertEquals(0, solutions.size(), "Non dovrebbero esserci soluzioni.");
    }


    // In KenKenSolverTest.java
    @Test
    @DisplayName("Puzzle 3x3 Ultra Semplice - Soluzione Unica (Tutte Gabbie da 1 Cella)")
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    void testSuperSimple3x3_SingleSolution_AllNone() {
        int N = 3;
        Grid puzzleGrid = new Grid(N);
        List<Cage> cages = new ArrayList<>();
        resetCageIds();

        // Soluzione Attesa:
        // 1 2 3
        // 2 3 1
        // 3 1 2

        // Definisci ogni cella come una gabbia a sé stante - MODO CORRETTO:
        Cage c00 = new Cage(1, OperationType.NONE);
        c00.addCell(puzzleGrid.getCell(0,0)); // Associa la cella (0,0) alla gabbia c00
        cages.add(c00); // Aggiungi la gabbia (ora con una cella) alla lista

        Cage c01 = new Cage(2, OperationType.NONE);
        c01.addCell(puzzleGrid.getCell(0,1));
        cages.add(c01);

        Cage c02 = new Cage(3, OperationType.NONE);
        c02.addCell(puzzleGrid.getCell(0,2));
        cages.add(c02);

        Cage c10 = new Cage(2, OperationType.NONE);
        c10.addCell(puzzleGrid.getCell(1,0));
        cages.add(c10);

        Cage c11 = new Cage(3, OperationType.NONE);
        c11.addCell(puzzleGrid.getCell(1,1));
        cages.add(c11);

        Cage c12 = new Cage(1, OperationType.NONE);
        c12.addCell(puzzleGrid.getCell(1,2));
        cages.add(c12);

        Cage c20 = new Cage(3, OperationType.NONE);
        c20.addCell(puzzleGrid.getCell(2,0));
        cages.add(c20);

        Cage c21 = new Cage(1, OperationType.NONE);
        c21.addCell(puzzleGrid.getCell(2,1));
        cages.add(c21);

        Cage c22 = new Cage(2, OperationType.NONE);
        c22.addCell(puzzleGrid.getCell(2,2));
        cages.add(c22);

        KenKenSolver solver = new KenKenSolver(puzzleGrid, cages, N);

        List<Grid> solutions = solver.solve(1);

        assertEquals(1, solutions.size(), "Puzzle 3x3 ultra semplice (tutte NONE) dovrebbe avere 1 soluzione.");
        if (!solutions.isEmpty()) {
            printGrid(solutions.getFirst(), "Soluzione 3x3 Ultra Semplice:");
            int[][] expectedSolution = {
                    {1, 2, 3},
                    {2, 3, 1},
                    {3, 1, 2}
            };
            assertSolutionCorrect(solutions.getFirst(), N, expectedSolution);
        }
    }
// Aggiungi questo costruttore a Cage.java (temporaneamente per test o permanentemente se utile):
// public Cage(int targetValue, OperationType operationType, Cell cell) {
//    this(targetValue, operationType); // Chiama il costruttore esistente
//    this.addCell(cell);
// }

    // Metodo helper per convertire Grid in int[][] (se non già presente)
    private int[][] convertGridToArray(Grid grid, int N) {
        int[][] array = new int[N][N];
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                array[r][c] = grid.getCell(r, c).getValue();
            }
        }
        return array;
    }

    private boolean gridMatchesExpected(Grid actual, int N, int[][] expected) {
        if (actual.getSize() != N) return false;
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                if (actual.getCell(r, c).getValue() != expected[r][c]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Test
    @DisplayName("Puzzle 4x4 - Soluzione Unica con Varie Operazioni")
    @Timeout(value = 2, unit = TimeUnit.SECONDS) // Più tempo per 4x4
    void test4x4_SingleSolution_MixedOperations() {
        int N = 4;
        Grid puzzleGrid = new Grid(N); // Griglia vuota iniziale
        List<Cage> cages = new ArrayList<>();
        resetCageIds(); // Assicura che gli ID delle gabbie partano da 1 per questo test

        // Gabbie per il puzzle 4x4 verificato
        // C1: (0,0), (1,0) : 3-
        Cage c1 = new Cage(3, OperationType.SUB);
        c1.addCell(puzzleGrid.getCell(0,0)); c1.addCell(puzzleGrid.getCell(1,0)); cages.add(c1);

        // C2: (0,1), (0,2) : 3x
        Cage c2 = new Cage(3, OperationType.MUL);
        c2.addCell(puzzleGrid.getCell(0,1)); c2.addCell(puzzleGrid.getCell(0,2)); cages.add(c2);

        // C3: (0,3) : 2=
        Cage c3 = new Cage(2, OperationType.NONE);
        c3.addCell(puzzleGrid.getCell(0,3)); cages.add(c3);

        // C4: (1,1), (2,1) : 6+
        Cage c4 = new Cage(6, OperationType.ADD);
        c4.addCell(puzzleGrid.getCell(1,1)); c4.addCell(puzzleGrid.getCell(2,1)); cages.add(c4);

        // C5: (1,2), (1,3) : 7+
        Cage c5 = new Cage(7, OperationType.ADD);
        c5.addCell(puzzleGrid.getCell(1,2)); c5.addCell(puzzleGrid.getCell(1,3)); cages.add(c5);

        // C6: (2,0) : 3=
        Cage c6 = new Cage(3, OperationType.NONE);
        c6.addCell(puzzleGrid.getCell(2,0)); cages.add(c6);

        // C7: (2,2), (3,2) : 3+
        Cage c7 = new Cage(3, OperationType.ADD);
        c7.addCell(puzzleGrid.getCell(2,2)); c7.addCell(puzzleGrid.getCell(3,2)); cages.add(c7);

        // C8: (2,3), (3,3) : 5+
        Cage c8 = new Cage(5, OperationType.ADD);
        c8.addCell(puzzleGrid.getCell(2,3)); c8.addCell(puzzleGrid.getCell(3,3)); cages.add(c8);

        // C9: (3,0), (3,1) : 1-
        Cage c9 = new Cage(1, OperationType.SUB);
        c9.addCell(puzzleGrid.getCell(3,0)); c9.addCell(puzzleGrid.getCell(3,1)); cages.add(c9);


        // Assicurati che tutte le celle siano coperte da una gabbia.
        // In questo setup, tutte le 16 celle sono coperte.

        KenKenSolver solver = new KenKenSolver(puzzleGrid, cages, N);
        List<Grid> solutions = solver.solve(1); // Cerca una soluzione

        assertEquals(1, solutions.size(), "Puzzle 4x4 Verificato dovrebbe avere 1 soluzione.");
        if (!solutions.isEmpty()) {
            printGrid(solutions.getFirst(), "Soluzione 4x4 Verificata:");
            int[][] expectedSolution = {
                    {4, 1, 3, 2},
                    {1, 2, 4, 3},
                    {3, 4, 2, 1},
                    {2, 3, 1, 4}
            };
            assertSolutionCorrect(solutions.getFirst(), N, expectedSolution);
        }
    }

    @Test
    @DisplayName("Puzzle 3x3 con Celle Pre-compilate - Soluzione Unica")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void test3x3_WithPreFilledCells() {
        int N = 3;
        Grid puzzleGrid = new Grid(N); // Griglia vuota iniziale
        resetCageIds();

        // Pre-compila una cella
        puzzleGrid.getCell(0,0).setValue(1);
        puzzleGrid.getCell(0,0).setEditable(false); // Molto importante!

        List<Cage> cages = new ArrayList<>();
        // Stesse gabbie del test testSimple3x3_SingleSolution_AdditionNone
        Cage c1 = new Cage(4, OperationType.ADD);
        c1.addCell(puzzleGrid.getCell(0,0)); c1.addCell(puzzleGrid.getCell(1,0)); cages.add(c1);
        Cage c2 = new Cage(2, OperationType.NONE);
        c2.addCell(puzzleGrid.getCell(0,1)); cages.add(c2);
        Cage c3 = new Cage(5, OperationType.ADD);
        c3.addCell(puzzleGrid.getCell(0,2)); c3.addCell(puzzleGrid.getCell(1,2)); cages.add(c3);
        Cage c4 = new Cage(4, OperationType.ADD);
        c4.addCell(puzzleGrid.getCell(1,1)); c4.addCell(puzzleGrid.getCell(2,1)); cages.add(c4);
        Cage c5 = new Cage(2, OperationType.NONE);
        c5.addCell(puzzleGrid.getCell(2,0)); cages.add(c5);
        Cage c6 = new Cage(1, OperationType.NONE);
        c6.addCell(puzzleGrid.getCell(2,2)); cages.add(c6);

        KenKenSolver solver = new KenKenSolver(puzzleGrid, cages, N);
        List<Grid> solutions = solver.solve(1);

        assertEquals(1, solutions.size(), "Puzzle 3x3 con cella pre-compilata dovrebbe avere 1 soluzione.");
        if (!solutions.isEmpty()) {
            printGrid(solutions.getFirst(), "Soluzione 3x3 Pre-compilata:");
            int[][] expectedSolution = {
                    {1, 2, 3},
                    {3, 1, 2},
                    {2, 3, 1}
            };
            assertSolutionCorrect(solutions.getFirst(), N, expectedSolution);
        }
    }

    @Test
    @DisplayName("Test Costruttore Solver - Griglia Null")
    void testSolverConstructor_NullGrid() {
        assertThrows(IllegalArgumentException.class, () -> {
            new KenKenSolver(null, new ArrayList<>(), 3);
        });
    }

    @Test
    @DisplayName("Test Costruttore Solver - Gabbie Null")
    void testSolverConstructor_NullCages() {
        assertThrows(IllegalArgumentException.class, () -> {
            new KenKenSolver(new Grid(3), null, 3);
        });
    }

    @Test
    @DisplayName("Test Costruttore Solver - Dimensione N non corrispondente")
    void testSolverConstructor_SizeMismatch() {
        assertThrows(IllegalArgumentException.class, () -> {
            new KenKenSolver(new Grid(4), new ArrayList<>(), 3);
        });
    }

    @Test
    @DisplayName("Test Solve con maxSolutions = 0")
    void testSolve_MaxSolutionsZero() {
        // Usa un puzzle qualsiasi, non dovrebbe nemmeno iniziare a risolvere
        int N = 3;
        Grid puzzleGrid = new Grid(N);
        List<Cage> cages = new ArrayList<>();
        Cage c1 = new Cage(1, OperationType.NONE); c1.addCell(puzzleGrid.getCell(0,0)); cages.add(c1);
        // ... (aggiungi abbastanza gabbie per coprire tutte le celle o rendilo un puzzle valido)
        for(int r=0; r<N; r++) for(int c=0; c<N; c++) if(puzzleGrid.getCell(r,c).getParentCage() == null) {
            Cage dummyCage = new Cage(r+1, OperationType.NONE); // Valore target casuale, non importa
            dummyCage.addCell(puzzleGrid.getCell(r,c));
            cages.add(dummyCage);
        }


        KenKenSolver solver = new KenKenSolver(puzzleGrid, cages, N);
        List<Grid> solutions = solver.solve(0);
        assertTrue(solutions.isEmpty(), "Non dovrebbero essere trovate soluzioni se maxSolutions è 0.");
    }


    // Helper per validare una soluzione completa (usato raramente, il solver lo fa internamente)
    private boolean isGridValidKenKenSolution(Grid grid, int N, List<Cage> originalCagesDefinition) {
        if (grid == null || grid.getSize() != N) return false;

        // 1. Controlla unicità righe e colonne e completezza
        for (int i = 0; i < N; i++) {
            boolean[] rowCheck = new boolean[N + 1];
            boolean[] colCheck = new boolean[N + 1];
            for (int j = 0; j < N; j++) {
                int rVal = grid.getCell(i, j).getValue();
                if (rVal == 0 || rVal > N || rowCheck[rVal]) return false;
                rowCheck[rVal] = true;

                int cVal = grid.getCell(j, i).getValue();
                if (cVal == 0 || cVal > N || colCheck[cVal]) return false;
                colCheck[cVal] = true;
            }
        }

        // 2. Controlla vincoli delle gabbie
        // Questo è più complesso perché le 'originalCagesDefinition' contengono celle dalla griglia *originale*.
        // Dobbiamo creare delle "runtime cages" con le celle della griglia *soluzione*
        // o assumere che il solver abbia già fatto questo correttamente internamente.
        // Per un test esterno, potremmo dover ricostruire le gabbie sulla griglia soluzione.

        // Per semplicità, qui facciamo un controllo basato sulle gabbie del solver
        // che dovrebbero essere state create con le celle della workingGrid.
        // Questo test è più per la validazione di puzzle complessi e si affida al fatto
        // che il solver, se produce una soluzione, l'ha già validata.
        // Ricreiamo le gabbie per la validazione qui:
        Map<Integer, Cage> validationCagesMap = new HashMap<>();
        for (Cage originalDefCage : originalCagesDefinition) {
            Cage validationCage = new Cage(originalDefCage.getTargetValue(), originalDefCage.getOperationType());
            for (Cell originalDefCell : originalDefCage.getCellsInCage()) {
                validationCage.addCell(grid.getCell(originalDefCell.getRow(), originalDefCell.getCol()));
            }
            validationCagesMap.put(originalDefCage.getCageId(), validationCage);
        }

        for (Cage validationCage : validationCagesMap.values()) {
            try {
                if (!validationCage.checkConstraint()) return false;
            } catch (Exception e) { // IllegalArgumentException da checkConstraint
                return false;
            }
        }
        return true;
    }
}