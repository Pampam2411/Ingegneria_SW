package com.kenken.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class GridTest {

    @Nested
    @DisplayName("Test del Costruttore Grid(int N)")
    class ConstructorTests {

        @ParameterizedTest
        @ValueSource(ints = {3, 4, 5, 6}) // Valori validi per N
        @DisplayName("Costruttore dovrebbe creare correttamente la griglia per N valido")
        void constructor_validN_shouldCreateGridCorrectly(int n) {
            Grid grid = new Grid(n);

            assertEquals(n, grid.getSize(), "La dimensione N della griglia non è corretta.");
            assertNotNull(grid.toString(), "toString non dovrebbe essere nullo."); // Un check base per toString

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    Cell cell = grid.getCell(i, j);
                    assertNotNull(cell, "La cella (" + i + "," + j + ") non dovrebbe essere nulla.");
                    assertEquals(i, cell.getRow(), "La riga della cella (" + i + "," + j + ") non è corretta.");
                    assertEquals(j, cell.getCol(), "La colonna della cella (" + i + "," + j + ") non è corretta.");
                    // Per default, una nuova cella ha valore 0 ed è editabile
                    assertEquals(0, cell.getValue(), "Il valore di default della cella (" + i + "," + j + ") dovrebbe essere 0.");
                    assertTrue(cell.isEditable(), "La cella (" + i + "," + j + ") di default dovrebbe essere editabile.");
                }
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0, 1, 2, 7, 10}) // Valori non validi per N
        @DisplayName("Costruttore dovrebbe lanciare IllegalArgumentException per N non valido")
        void constructor_invalidN_shouldThrowIllegalArgumentException(int n) {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                new Grid(n);
            });
            assertEquals("Il numero di righe o colonne deve essere compreso tra 3 e 6", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Test per getCell(int row, int col)")
    class GetCellTests {
        private Grid grid;
        private final int N = 4; // Dimensione di test

        @BeforeEach
        void setUp() {
            grid = new Grid(N);
        }

        @Test
        @DisplayName("getCell dovrebbe restituire la cella corretta per coordinate valide")
        void getCell_validCoordinates_shouldReturnCorrectCell() {
            Cell cell00 = grid.getCell(0, 0);
            assertNotNull(cell00);
            assertEquals(0, cell00.getRow());
            assertEquals(0, cell00.getCol());

            Cell cellNM1_NM1 = grid.getCell(N - 1, N - 1);
            assertNotNull(cellNM1_NM1);
            assertEquals(N - 1, cellNM1_NM1.getRow());
            assertEquals(N - 1, cellNM1_NM1.getCol());

            Cell cellMid = grid.getCell(1, 2); // Esempio di cella interna
            assertNotNull(cellMid);
            assertEquals(1, cellMid.getRow());
            assertEquals(2, cellMid.getCol());
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, N, N + 1}) // Coordinate di riga non valide
        @DisplayName("getCell dovrebbe lanciare IndexOutOfBoundsException per riga non valida")
        void getCell_invalidRow_shouldThrowIndexOutOfBoundsException(int invalidRow) {
            Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> {
                grid.getCell(invalidRow, 0); // Colonna valida, riga non valida
            });
            assertEquals("La cella non esiste", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, N, N + 1}) // Coordinate di colonna non valide
        @DisplayName("getCell dovrebbe lanciare IndexOutOfBoundsException per colonna non valida")
        void getCell_invalidCol_shouldThrowIndexOutOfBoundsException(int invalidCol) {
            Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> {
                grid.getCell(0, invalidCol); // Riga valida, colonna non valida
            });
            assertEquals("La cella non esiste", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Test per getSize()")
    class GetSizeTests {
        @ParameterizedTest
        @ValueSource(ints = {3, 5, 6})
        @DisplayName("getSize dovrebbe restituire la dimensione N corretta")
        void getSize_shouldReturnCorrectN(int n) {
            Grid grid = new Grid(n);
            assertEquals(n, grid.getSize());
        }
    }

    @Nested
    @DisplayName("Test per printToConsole()")
    class PrintToConsoleTests {

        @Test
        @DisplayName("printToConsole non dovrebbe lanciare eccezioni per una griglia valida")
        void printToConsole_validGrid_shouldNotThrowException() {
            Grid grid = new Grid(3);
            // Modifichiamo qualche valore per avere output più vario
            grid.getCell(0,0).setValue(1);
            grid.getCell(1,1).setValue(2);
            grid.getCell(2,2).setValue(3);

            // Per non inquinare la console dei test, potremmo reindirizzare System.out,
            // ma per questo test ci limitiamo a verificare che non ci siano eccezioni.
            assertDoesNotThrow(() -> {
                grid.printToConsole();
            });
        }

        @Test
        @DisplayName("printToConsole dovrebbe stampare i valori corretti (test opzionale più approfondito)")
        void printToConsole_shouldPrintCorrectValues() {
            Grid grid = new Grid(3);
            grid.getCell(0, 0).setValue(1);
            grid.getCell(0, 1).setValue(2);
            grid.getCell(0, 2).setValue(3);
            grid.getCell(1, 0).setValue(4);
            // Le altre rimangono 0

            // Cattura l'output della console
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            grid.printToConsole();

            // Ripristina l'output originale
            System.setOut(originalOut);

            String expectedOutput = "1\t2\t3\t" + System.lineSeparator() +
                    "4\t0\t0\t" + System.lineSeparator() +
                    "0\t0\t0\t" + System.lineSeparator();
            assertEquals(expectedOutput, outContent.toString(), "L'output di printToConsole non è quello atteso.");
        }
    }

    @Nested
    @DisplayName("Test per toString()")
    class ToStringTests {
        @ParameterizedTest
        @ValueSource(ints = {3, 4, 6})
        @DisplayName("toString dovrebbe restituire la stringa formattata correttamente")
        void toString_shouldReturnCorrectFormattedString(int n) {
            Grid grid = new Grid(n);
            String expectedString = "Size Grid-> " + n + "x" + n + "\n";
            assertEquals(expectedString, grid.toString());
        }
    }
}