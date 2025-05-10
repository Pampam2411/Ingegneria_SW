package com.kenken.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CageTest {

    // Utility per resettare il contatore statico cageId prima di ogni test
    @BeforeEach
    void resetCageIdCounter() {
        try {
            Field nextCageIdField = Cage.class.getDeclaredField("nextCageId");
            nextCageIdField.setAccessible(true);
            nextCageIdField.setInt(null, 1);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Non far fallire il test se il reset non è critico per tutti i casi
        }
    }

    @Test
    @DisplayName("Il costruttore dovrebbe inizializzare correttamente la gabbia")
    void constructor_shouldInitializeCageCorrectly() {
        Cage cage1 = new Cage(5, OperationType.ADD);
        assertEquals(5, cage1.getTargetValue());
        assertEquals(OperationType.ADD, cage1.getOperationType());
        assertNotNull(cage1.getCellsInCage());
        assertTrue(cage1.getCellsInCage().isEmpty());
        assertEquals(1, cage1.getCageId());

        Cage cage2 = new Cage(10, OperationType.MUL);
        assertEquals(2, cage2.getCageId());
    }

    @Test
    @DisplayName("Il costruttore dovrebbe lanciare IllegalArgumentException per OperationType nullo")
    void constructor_shouldThrowExceptionForNullOperationType() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Cage(5, null);
        });
        assertEquals("Operatore nullo", exception.getMessage());
    }

    @Test
    @DisplayName("getters dovrebbero restituire i valori corretti")
    void getters_shouldReturnCorrectValues() {
        Cage cage = new Cage(7, OperationType.SUB);
        assertEquals(7, cage.getTargetValue());
        assertEquals(OperationType.SUB, cage.getOperationType());
        assertEquals(1, cage.getCageId());
        assertNotNull(cage.getCellsInCage());
    }


    @Nested
    @DisplayName("Test per il metodo addCell(Cell cell)")
    class AddCellTests {
        private Cage cage;
        private Cell cell1;
        private Cell cell2;

        @BeforeEach
        void setUp() {
            cage = new Cage(5, OperationType.ADD);
            cell1 = new Cell(0, 0);
            cell2 = new Cell(0, 1);
        }

        @Test
        @DisplayName("Dovrebbe aggiungere una cella correttamente")
        void addCell_shouldAddCellSuccessfully() {
            cage.addCell(cell1);
            List<Cell> cellsInCage = cage.getCellsInCage();
            assertEquals(1, cellsInCage.size());
            assertTrue(cellsInCage.contains(cell1));

            cage.addCell(cell2);
            assertEquals(2, cellsInCage.size());
            assertTrue(cellsInCage.contains(cell2));
        }

        @Test
        @DisplayName("Dovrebbe lanciare IllegalArgumentException se la cella è nulla")
        void addCell_shouldThrowExceptionIfCellIsNull() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                cage.addCell(null);
            });
            assertEquals("La cella non può essere nulla", exception.getMessage());
        }

        @Test
        @DisplayName("Dovrebbe lanciare IllegalArgumentException se la cella è già presente")
        void addCell_shouldThrowExceptionIfCellAlreadyExists() {
            cage.addCell(cell1); // Aggiunge la prima volta
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                cage.addCell(cell1); // Tenta di aggiungere di nuovo
            });
            assertEquals("La cella è già presente nella gabbia", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Test per il metodo checkConstraint()")
    class CheckConstraintTests {
        private Cage cage;
        private Cell cell1, cell2, cell3;

        @BeforeEach
        void setUp() {
            // Cage e celle verranno create specificamente in ogni test o MethodSource
            // per garantire l'isolamento e la chiarezza dello stato iniziale.
            cell1 = new Cell(0, 0);
            cell2 = new Cell(0, 1);
            cell3 = new Cell(0, 2);
        }

        @Test
        @DisplayName("Dovrebbe restituire false se cellsInCage è vuota")
        void checkConstraint_emptyCage_shouldReturnFalse() {
            cage = new Cage(5, OperationType.ADD);
            assertFalse(cage.checkConstraint(), "checkConstraint dovrebbe restituire false per una gabbia senza celle.");
        }

        @Test
        @DisplayName("Dovrebbe restituire false se una cella nella gabbia è vuota (valore 0)")
        void checkConstraint_cellIsEmpty_shouldReturnFalse() {
            cage = new Cage(5, OperationType.ADD);
            cell1.setValue(5); // Valore valido
            // cell2 rimane vuota (valore 0 di default)
            cage.addCell(cell1);
            cage.addCell(cell2);
            assertFalse(cage.checkConstraint(), "checkConstraint dovrebbe restituire false se una cella è vuota.");
        }

        // NOTA: Il caso operationType == null è prevenuto dal costruttore di Cage.
        // Se si potesse impostare operationType a null dopo la costruzione, si dovrebbe testare.

        // --- Test per OperationType.NONE ---
        @Test
        @DisplayName("NONE: Dovrebbe restituire true se una cella ha il valore target")
        void checkConstraint_NONE_valid_shouldReturnTrue() {
            cage = new Cage(7, OperationType.NONE);
            cell1.setValue(7);
            cage.addCell(cell1);
            assertTrue(cage.checkConstraint());
        }

        @Test
        @DisplayName("NONE: Dovrebbe restituire false se una cella non ha il valore target")
        void checkConstraint_NONE_invalidValue_shouldReturnFalse() {
            cage = new Cage(7, OperationType.NONE);
            cell1.setValue(5);
            cage.addCell(cell1);
            assertFalse(cage.checkConstraint());
        }

        @Test
        @DisplayName("NONE: Dovrebbe restituire false se ci sono più celle (anche se piene)")
        void checkConstraint_NONE_tooManyCells_shouldReturnFalse() {
            cage = new Cage(7, OperationType.NONE);
            cell1.setValue(7);
            cell2.setValue(1); // Valore irrilevante, ma cella piena
            cage.addCell(cell1);
            cage.addCell(cell2);
            assertFalse(cage.checkConstraint(), "NONE con più di una cella dovrebbe restituire false.");
        }

        // --- Test per OperationType.ADD ---
        static Stream<Arguments> addProvider() {
            return Stream.of(
                    Arguments.of("ADD_Valid_TwoCells", 8, OperationType.ADD, List.of(3, 5), true),
                    Arguments.of("ADD_Valid_ThreeCells", 12, OperationType.ADD, List.of(2, 4, 6), true),
                    Arguments.of("ADD_Valid_OneCell", 5, OperationType.ADD, List.of(5), true),
                    Arguments.of("ADD_Invalid_TwoCells", 8, OperationType.ADD, List.of(3, 6), false)
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("addProvider")
        @DisplayName("ADD: Test validi e invalidi")
        void checkConstraint_ADD_scenarios(String name, int target, OperationType op, List<Integer> values, boolean expected) {
            cage = new Cage(target, op);
            for (int i = 0; i < values.size(); i++) {
                Cell cell = new Cell(0, i); // Coordinate fittizie
                cell.setValue(values.get(i));
                cage.addCell(cell);
            }
            assertEquals(expected, cage.checkConstraint());
        }

        // --- Test per OperationType.SUB ---
        static Stream<Arguments> subProvider() {
            return Stream.of(
                    Arguments.of("SUB_Valid_5_2_target_3", 3, List.of(5, 2), true),
                    Arguments.of("SUB_Valid_2_5_target_3", 3, List.of(2, 5), true),
                    Arguments.of("SUB_Invalid_5_1_target_3", 3, List.of(5, 1), false)
            );
        }
        @ParameterizedTest(name = "{0}")
        @MethodSource("subProvider")
        @DisplayName("SUB: Test validi e invalidi")
        void checkConstraint_SUB_scenarios(String name, int target, List<Integer> values, boolean expected) {
            cage = new Cage(target, OperationType.SUB);
            // Aggiunge esattamente due celle
            Cell c1 = new Cell(0,0); c1.setValue(values.get(0)); cage.addCell(c1);
            Cell c2 = new Cell(0,1); c2.setValue(values.get(1)); cage.addCell(c2);
            assertEquals(expected, cage.checkConstraint());
        }

        @Test
        @DisplayName("SUB: Dovrebbe lanciare IllegalArgumentException per numero di celle diverso da 2")
        void checkConstraint_SUB_wrongNumberOfCells_shouldThrowException() {
            cage = new Cage(3, OperationType.SUB);
            cell1.setValue(5);
            cage.addCell(cell1); // Solo una cella
            assertThrows(IllegalArgumentException.class, () -> cage.checkConstraint(), "SUB con una cella dovrebbe lanciare eccezione.");

            cage = new Cage(3, OperationType.SUB); // Ricrea per pulizia
            cell1.setValue(5); cage.addCell(cell1);
            cell2.setValue(2); cage.addCell(cell2);
            cell3.setValue(1); cage.addCell(cell3); // Tre celle
            assertThrows(IllegalArgumentException.class, () -> cage.checkConstraint(), "SUB con tre celle dovrebbe lanciare eccezione.");
        }

        // --- Test per OperationType.MUL ---
        static Stream<Arguments> mulProvider() {
            return Stream.of(
                    Arguments.of("MUL_Valid_TwoCells", 15, List.of(3, 5), true),
                    Arguments.of("MUL_Valid_ThreeCells", 24, List.of(2, 3, 4), true),
                    Arguments.of("MUL_Valid_OneCell", 7, List.of(7), true),
                    Arguments.of("MUL_Invalid_TwoCells", 15, List.of(3, 6), false),
                    Arguments.of("MUL_Invalid_WithZero_TargetNonZero", 10, List.of(5, 0, 2), false),
                    Arguments.of("MUL_Invalid_NoZero_TargetZero", 0, List.of(5,1,2),false)

            );
        }
        @ParameterizedTest(name = "{0}")
        @MethodSource("mulProvider")
        @DisplayName("MUL: Test validi e invalidi")
        void checkConstraint_MUL_scenarios(String name, int target, List<Integer> values, boolean expected) {
            cage = new Cage(target, OperationType.MUL);
            for (int i = 0; i < values.size(); i++) {
                Cell cell = new Cell(0, i);
                cell.setValue(values.get(i));
                cage.addCell(cell);
            }
            assertEquals(expected, cage.checkConstraint());
        }

        // --- Test per OperationType.DIV ---
        static Stream<Arguments> divProvider() {
            return Stream.of(
                    Arguments.of("DIV_Valid_6_2_target_3", 3, List.of(6, 2), true),
                    Arguments.of("DIV_Valid_2_6_target_3", 3, List.of(2, 6), true),
                    Arguments.of("DIV_Valid_5_5_target_1", 1, List.of(5, 5), true),
                    Arguments.of("DIV_Invalid_NonIntegerResult", 3, List.of(7, 2), false),
                    Arguments.of("DIV_Invalid_WrongResult", 3, List.of(8, 2), false) // 8/2 = 4 != 3
            );
        }
        @ParameterizedTest(name = "{0}")
        @MethodSource("divProvider")
        @DisplayName("DIV: Test validi e invalidi")
        void checkConstraint_DIV_scenarios(String name, int target, List<Integer> values, boolean expected) {
            cage = new Cage(target, OperationType.DIV);
            Cell c1 = new Cell(0,0); c1.setValue(values.get(0)); cage.addCell(c1);
            Cell c2 = new Cell(0,1); c2.setValue(values.get(1)); cage.addCell(c2);
            assertEquals(expected, cage.checkConstraint());
        }

        @Test
        @DisplayName("DIV: Dovrebbe lanciare IllegalArgumentException per numero di celle diverso da 2")
        void checkConstraint_DIV_wrongNumberOfCells_shouldThrowException() {
            cage = new Cage(3, OperationType.DIV);
            cell1.setValue(6);
            cage.addCell(cell1); // Solo una cella
            assertThrows(IllegalArgumentException.class, () -> cage.checkConstraint(), "DIV con una cella dovrebbe lanciare eccezione.");

            cage = new Cage(3, OperationType.DIV); // Ricrea per pulizia
            cell1.setValue(6); cage.addCell(cell1);
            cell2.setValue(2); cage.addCell(cell2);
            cell3.setValue(1); cage.addCell(cell3); // Tre celle
            assertThrows(IllegalArgumentException.class, () -> cage.checkConstraint(), "DIV con tre celle dovrebbe lanciare eccezione.");
        }
    }

    @Nested
    @DisplayName("Test per equals() e hashCode()")
    class EqualsAndHashCodeTests {
        // ... (i test per equals e hashCode rimangono come prima) ...
        // (Ho omesso per brevità, ma dovrebbero essere inclusi come nella tua versione precedente)
        private Cage cage1_A;
        private Cage cage1_B;
        private Cage cage2;

        @BeforeEach
        void setUpEquals() {
            cage1_A = new Cage(5, OperationType.ADD);
            cage1_B = new Cage(5, OperationType.ADD);
            cage2 = new Cage(10, OperationType.MUL);
        }

        @Test
        @DisplayName("equals dovrebbe restituire true per lo stesso oggetto")
        void equals_sameObject_shouldReturnTrue() {
            assertTrue(cage1_A.equals(cage1_A));
        }

        @Test
        @DisplayName("equals dovrebbe restituire false per oggetto nullo")
        void equals_nullObject_shouldReturnFalse() {
            assertFalse(cage1_A.equals(null));
        }

        @Test
        @DisplayName("equals dovrebbe restituire false per un tipo di oggetto diverso")
        void equals_differentObjectType_shouldReturnFalse() {
            assertFalse(cage1_A.equals("una stringa"));
        }

        @Test
        @DisplayName("equals dovrebbe restituire false per Cage con cageId diversi")
        void equals_differentCagesDueToDifferentId_shouldReturnFalse() {
            assertFalse(cage1_A.equals(cage1_B), "Due Cage con ID diversi dovrebbero essere non uguali.");
        }

        @Test
        @DisplayName("equals dovrebbe restituire false se targetValue è diverso")
        void equals_differentTargetValue_shouldReturnFalse() {
            Cage cage_temp_target_diff = new Cage(10, OperationType.ADD);
            assertFalse(cage1_A.equals(cage_temp_target_diff));
        }

        @Test
        @DisplayName("equals dovrebbe restituire false se OperationType è diverso")
        void equals_differentOperationType_shouldReturnFalse() {
            Cage cage_temp_op_diff = new Cage(5, OperationType.SUB);
            assertFalse(cage1_A.equals(cage_temp_op_diff));
        }

        @Test
        @DisplayName("equals dovrebbe restituire false se le liste di celle sono diverse (assumendo che equals le controlli)")
        void equals_differentCells_shouldReturnFalse() {
            Cell cellA = new Cell(0,0);
            Cell cellB = new Cell(1,1);

            Cage cage_A_con_cella = new Cage(5, OperationType.ADD); // ID 1 (dopo reset generale)
            cage_A_con_cella.addCell(cellA);

            // Per testare equals, se confronta anche le celle, abbiamo bisogno di un modo
            // per avere due cage con lo STESSO ID ma celle diverse, il che non è
            // possibile con la costruzione attuale, oppure l'equals deve ignorare l'ID se si
            // confrontano altri campi.
            // Dato che l'ID è univoco per istanza, ci concentriamo sul fatto che istanze diverse sono diverse.
            // Se l'equals confrontasse i contenuti delle celle, questo test andrebbe rivisto.
            // Il test attuale è più sull'unicità dell'ID.

            Cage cage_B_con_cella_diversa = new Cage(5, OperationType.ADD); // ID successivo
            cage_B_con_cella_diversa.addCell(cellB);
            assertFalse(cage_A_con_cella.equals(cage_B_con_cella_diversa));

            Cage cage_B_senza_celle = new Cage(5, OperationType.ADD); // ID ancora successivo
            assertFalse(cage_A_con_cella.equals(cage_B_senza_celle));
        }

        @Test
        @DisplayName("hashCode dovrebbe essere consistente")
        void hashCode_shouldBeConsistent() {
            int hash1 = cage1_A.hashCode();
            int hash2 = cage1_A.hashCode();
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("hashCode: oggetti uguali (stesso riferimento) dovrebbero avere lo stesso hashCode")
        void hashCode_equalObjects_shouldHaveSameHashCode() {
            Cage cage_A_ref = cage1_A;
            if (cage1_A.equals(cage_A_ref)) {
                assertEquals(cage1_A.hashCode(), cage_A_ref.hashCode());
            }
        }
    }

    @Test
    @DisplayName("toString dovrebbe restituire una rappresentazione stringa significativa")
    void toString_shouldReturnMeaningfulString() {
        Cage cage = new Cage(7, OperationType.SUB); // ID 1
        Cell c1 = new Cell(0,0);
        cage.addCell(c1);
        // Il tuo toString() usa operationType.getSymbol() e cellsInCage.size()
        String expectedString = "Cage{" +
                "cageId=" + cage.getCageId() +
                ", targetValue=" + 7 +
                ", operationType=" + OperationType.SUB.getSymbol() +
                ", cellsInCage=" + 1 + // Numero di celle aggiunte
                '}';
        assertEquals(expectedString, cage.toString());
    }
}