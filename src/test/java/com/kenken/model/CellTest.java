package com.kenken.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class CellTest {

    private Cell cell;

    @Nested
    @DisplayName("Test dei Costruttori")
    class ConstructorTests {

        @Test
        @DisplayName("Costruttore primario (row, col) dovrebbe inizializzare correttamente")
        void primaryConstructor_shouldInitializeCorrectly() {
            Cell c = new Cell(1, 2);
            assertEquals(1, c.getRow(), "La riga non è corretta");
            assertEquals(2, c.getCol(), "La colonna non è corretta");
            assertEquals(0, c.getValue(), "Il valore di default dovrebbe essere 0");
            assertTrue(c.isEditable(), "La cella di default dovrebbe essere editabile");
            assertNull(c.getParentCage(), "La ParentCage di default dovrebbe essere nulla");
            assertTrue(c.isEmpty(), "La cella di default dovrebbe essere vuota");
        }

        @Test
        @DisplayName("Costruttore secondario (row, col, initialValue, isEditable) dovrebbe inizializzare correttamente")
        void secondaryConstructor_shouldInitializeCorrectly() {
            Cell c = new Cell(3, 4, 5, false);
            assertEquals(3, c.getRow(), "La riga non è corretta");
            assertEquals(4, c.getCol(), "La colonna non è corretta");
            assertEquals(5, c.getValue(), "Il valore iniziale non è corretto");
            assertFalse(c.isEditable(), "Lo stato isEditable non è corretto");
            assertNull(c.getParentCage(), "La ParentCage di default dovrebbe essere nulla");
            assertFalse(c.isEmpty(), "La cella non dovrebbe essere vuota se initialValue non è 0");

            Cell cEmpty = new Cell(0,0,0,true);
            assertTrue(cEmpty.isEmpty(), "La cella dovrebbe essere vuota se initialValue è 0");
        }
    }

    @Nested
    @DisplayName("Test dei Getters")
    class GetterTests {
        private Cell cellWithValue;
        private Cell cellDefault;

        @BeforeEach
        void setUp() {
            cellDefault = new Cell(0, 0);
            cellWithValue = new Cell(1, 1, 9, false);
        }

        @Test
        @DisplayName("getRow dovrebbe restituire la riga corretta")
        void getRow_shouldReturnCorrectRow() {
            assertEquals(0, cellDefault.getRow());
            assertEquals(1, cellWithValue.getRow());
        }

        @Test
        @DisplayName("getCol dovrebbe restituire la colonna corretta")
        void getCol_shouldReturnCorrectCol() {
            assertEquals(0, cellDefault.getCol());
            assertEquals(1, cellWithValue.getCol());
        }

        @Test
        @DisplayName("getValue dovrebbe restituire il valore corretto")
        void getValue_shouldReturnCorrectValue() {
            assertEquals(0, cellDefault.getValue());
            assertEquals(9, cellWithValue.getValue());
        }

        @Test
        @DisplayName("isEmpty dovrebbe restituire true se value è 0, altrimenti false")
        void isEmpty_shouldReturnCorrectState() {
            assertTrue(cellDefault.isEmpty());
            cellDefault.setValue(5); // Assumendo che sia editabile per il test
            assertFalse(cellDefault.isEmpty());

            assertFalse(cellWithValue.isEmpty());
        }

        @Test
        @DisplayName("isEditable dovrebbe restituire lo stato di editabilità corretto")
        void isEditable_shouldReturnCorrectEditableState() {
            assertTrue(cellDefault.isEditable());
            assertFalse(cellWithValue.isEditable());
        }

        @Test
        @DisplayName("getParentCage dovrebbe restituire la Cage genitore corretta")
        void getParentCage_shouldReturnCorrectParentCage() {
            assertNull(cellDefault.getParentCage());
            // Test con una cage settata verrà fatto nei test dei setter
        }
    }

    @Nested
    @DisplayName("Test dei Setters e Metodi di Modifica")
    class SetterAndModifierTests {
        @BeforeEach
        void setUp() {
            cell = new Cell(2, 2); // Editabile di default
        }

        @Test
        @DisplayName("setValue dovrebbe aggiornare il valore se la cella è editabile")
        void setValue_whenEditable_shouldUpdateValue() {
            cell.setValue(7);
            assertEquals(7, cell.getValue());
            assertFalse(cell.isEmpty());
        }

        @Test
        @DisplayName("setValue dovrebbe lanciare IllegalStateException se la cella non è editabile")
        void setValue_whenNotEditable_shouldThrowException() {
            cell.setEditable(false);
            Exception exception = assertThrows(IllegalStateException.class, () -> {
                cell.setValue(7);
            });
            assertEquals("Impossibile modificare il valore di una cella non editabile", exception.getMessage());
            assertEquals(0, cell.getValue(), "Il valore non dovrebbe cambiare se non editabile");
        }

        @Test
        @DisplayName("setEditable dovrebbe aggiornare lo stato di editabilità")
        void setEditable_shouldUpdateEditableState() {
            assertTrue(cell.isEditable());
            cell.setEditable(false);
            assertFalse(cell.isEditable());
            cell.setEditable(true);
            assertTrue(cell.isEditable());
        }

        @Test
        @DisplayName("setParentCage dovrebbe aggiornare la Cage genitore")
        void setParentCage_shouldUpdateParentCage() {
            Cage mockCage = new Cage(1, OperationType.NONE); // Istanza reale o mock
            // Se si usa Mockito:
            // Cage mockCage = mock(Cage.class);

            assertNull(cell.getParentCage());
            cell.setParentCage(mockCage);
            assertSame(mockCage, cell.getParentCage(), "La parentCage non è stata settata correttamente");
        }

        @Test
        @DisplayName("clearValue dovrebbe impostare il valore a 0 se la cella è editabile")
        void clearValue_whenEditable_shouldSetValueToZero() {
            cell.setValue(9);
            assertFalse(cell.isEmpty());
            cell.clearValue();
            assertEquals(0, cell.getValue());
            assertTrue(cell.isEmpty());
        }

        @Test
        @DisplayName("clearValue non dovrebbe cambiare il valore se la cella non è editabile")
        void clearValue_whenNotEditable_shouldNotChangeValue() {
            // Creiamo una cella inizialmente non editabile e con un valore
            Cell nonEditableCell = new Cell(0, 0, 5, false);
            assertEquals(5, nonEditableCell.getValue());

            nonEditableCell.clearValue(); // Questo non dovrebbe fare nulla perché non è editabile

            assertEquals(5, nonEditableCell.getValue(), "Il valore di una cella non editabile non dovrebbe essere resettato da clearValue");
            assertFalse(nonEditableCell.isEmpty());
        }
    }

    @Nested
    @DisplayName("Test per toString()")
    class ToStringTests {
        // Per Cage.hashCode(), dobbiamo resettare il contatore statico se lo usiamo per prevedibilità
        // Oppure usare un mock con hashCode definito.
        // Per semplicità qui assumiamo che Cage sia istanziabile e il suo hashCode() sia deterministico per l'istanza.
        // Resettare nextCageId di Cage:
        private static void resetCageIdCounter() {
            try {
                java.lang.reflect.Field nextCageIdField = Cage.class.getDeclaredField("nextCageId");
                nextCageIdField.setAccessible(true);
                nextCageIdField.setInt(null, 1);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Ignora se il campo non esiste o non è accessibile, il test potrebbe essere meno preciso
            }
        }


        @Test
        @DisplayName("toString dovrebbe formattare correttamente (editabile, no cage)")
        void toString_editableNoCage() {
            Cell c = new Cell(1, 2); // value=0, isEditable=true, parentCage=null
            assertEquals("[1,2] Y  - 0", c.toString());
        }

        @Test
        @DisplayName("toString dovrebbe formattare correttamente (non editabile, no cage, con valore)")
        void toString_notEditableNoCageWithValue() {
            Cell c = new Cell(3, 4, 7, false); // value=7, isEditable=false, parentCage=null
            assertEquals("[3,4] N  - 7", c.toString());
        }

        @Test
        @DisplayName("toString dovrebbe formattare correttamente (editabile, con cage)")
        void toString_editableWithCage() {
            resetCageIdCounter(); // Assicura che cageId sia prevedibile (es. 1)
            Cell c = new Cell(5, 6); // value=0, isEditable=true
            Cage parent = new Cage(10, OperationType.ADD); // cageId sarà 1 (se resettato)
            c.setParentCage(parent);
            // L'hashCode di Cage, nella tua implementazione, è cageId, che dovrebbe essere 1
            // dopo il reset, o comunque un valore fisso per l'istanza `parent`.
            // Per un test robusto, l'hashCode del mockCage sarebbe meglio.
            // Il tuo Cage.toString() usa cage.hashCode(), se Cage lo implementa.
            // Dalla tua classe Cell: String cageStr= parentCage==null ? " - ":" "+parentCage.hashCode()+" ";
            String expected = "[5,6] Y  "+parent.hashCode()+" 0";
            assertEquals(expected, c.toString());
        }
    }

    @Nested
    @DisplayName("Test per equals() e hashCode()")
    class EqualsAndHashCodeTests {
        private Cell cellA_1_2;
        private Cell cellB_1_2; // Stesse row/col di cellA
        private Cell cellC_1_3; // Diversa col di cellA
        private Cell cellD_3_2; // Diversa row di cellA

        @BeforeEach
        void setUp() {
            cellA_1_2 = new Cell(1, 2, 5, true);  // Valore e isEditable diversi non dovrebbero influire
            cellB_1_2 = new Cell(1, 2, 0, false);
            cellC_1_3 = new Cell(1, 3);
            cellD_3_2 = new Cell(3, 2);
        }

        @Test
        @DisplayName("equals: una cella dovrebbe essere uguale a se stessa")
        void equals_sameInstance_shouldReturnTrue() {
            assertTrue(cellA_1_2.equals(cellA_1_2));
        }

        @Test
        @DisplayName("equals: dovrebbe restituire true per celle con stessa riga e colonna")
        void equals_sameRowAndCol_shouldReturnTrue() {
            assertTrue(cellA_1_2.equals(cellB_1_2));
        }

        @Test
        @DisplayName("equals: dovrebbe restituire false per celle con colonne diverse")
        void equals_differentCol_shouldReturnFalse() {
            assertFalse(cellA_1_2.equals(cellC_1_3));
        }

        @Test
        @DisplayName("equals: dovrebbe restituire false per celle con righe diverse")
        void equals_differentRow_shouldReturnFalse() {
            assertFalse(cellA_1_2.equals(cellD_3_2));
        }

        @Test
        @DisplayName("equals: dovrebbe restituire false per confronto con null")
        void equals_nullObject_shouldReturnFalse() {
            assertFalse(cellA_1_2.equals(null));
        }

        @Test
        @DisplayName("equals: dovrebbe restituire false per confronto con un tipo di oggetto diverso")
        void equals_differentType_shouldReturnFalse() {
            assertFalse(cellA_1_2.equals("Not a Cell object"));
        }

        @Test
        @DisplayName("hashCode: celle uguali (stessa riga/col) dovrebbero avere lo stesso hashCode")
        void hashCode_equalCells_shouldHaveSameHashCode() {
            assertEquals(cellA_1_2.hashCode(), cellB_1_2.hashCode());
        }

        @Test
        @DisplayName("hashCode: dovrebbe essere consistente")
        void hashCode_shouldBeConsistent() {
            int initialHashCode = cellA_1_2.hashCode();
            assertEquals(initialHashCode, cellA_1_2.hashCode(), "hashCode non è consistente");
        }

        @Test
        @DisplayName("hashCode: celle diverse (riga/col) dovrebbero preferibilmente avere hashCode diversi")
        void hashCode_differentCells_shouldPreferablyHaveDifferentHashCodes() {
            // Non è un requisito stretto, ma buona pratica per la performance delle HashMap
            assertNotEquals(cellA_1_2.hashCode(), cellC_1_3.hashCode(), "HashCodes per (1,2) e (1,3) dovrebbero essere diversi");
            assertNotEquals(cellA_1_2.hashCode(), cellD_3_2.hashCode(), "HashCodes per (1,2) e (3,2) dovrebbero essere diversi");
        }
    }
}