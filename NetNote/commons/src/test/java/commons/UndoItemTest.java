package commons;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UndoItemTest {

    @Test
    void testConstructorSetsId() {
        UndoItem undoItem = new UndoItem(123L);
        assertEquals(123L, undoItem.getId(), "ID should be set correctly in the constructor");
    }

    @Test
    void testSetAndGetPosition() {
        UndoItem undoItem = new UndoItem(123L);
        undoItem.position = 42;
        assertEquals(42, undoItem.getPosition(), "Position should match the set value");
    }

    @Test
    void testSetPositionNegative() {
        UndoItem undoItem = new UndoItem(123L);
        undoItem.position = -1;
        assertEquals(-1, undoItem.getPosition(), "Position should allow negative values");
    }

    @Test
    void testSetAndGetDescription() {
        UndoItem undoItem = new UndoItem(123L);
        undoItem.description = "This is a test description.";
        assertEquals("This is a test description.", undoItem.getDescription(), "Description should match the set value");
    }

    @Test
    void testSetDescriptionNull() {
        UndoItem undoItem = new UndoItem(123L);
        undoItem.description = null;
        assertNull(undoItem.getDescription(), "Description should be null when explicitly set to null");
    }

    @Test
    void testEqualsAndHashCode() {
        UndoItem undoItem1 = new UndoItem(123L);
        UndoItem undoItem2 = new UndoItem(123L);
        UndoItem undoItem3 = new UndoItem(456L);

        assertEquals(undoItem1, undoItem2, "UndoItems with the same ID should be equal");
        assertNotEquals(undoItem1, undoItem3, "UndoItems with different IDs should not be equal");
        assertEquals(undoItem1.hashCode(), undoItem2.hashCode(), "Equal UndoItems should have the same hashCode");
        assertNotEquals(undoItem1.hashCode(), undoItem3.hashCode(), "Unequal UndoItems should have different hashCodes");
    }

    @Test
    void testEqualsWithNull() {
        UndoItem undoItem = new UndoItem(123L);
        assertNotEquals(null, undoItem, "UndoItem should not be equal to null");
    }

    @Test
    void testEqualsWithDifferentClass() {
        UndoItem undoItem = new UndoItem(123L);
        assertNotEquals(undoItem, new Object(), "UndoItem should not be equal to an object of a different class");
    }

}
