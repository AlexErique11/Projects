package client;

import commons.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class collectionSceneControllerTest {

    // Initialize an array of collections using the setter for collection name
    private final Collection[] collections = new Collection[4];

    @BeforeEach
    public void setup(){
        collections[0] = new Collection();
        collections[0].setName("Collection1");

        collections[1] = new Collection();
        collections[1].setName("Collection2");

        collections[2] = new Collection();
        collections[2].setName("Collection3");

        collections[3] = new Collection();
        collections[3].setName("Collection4");
    }

    private final CollectionSceneController instance = new CollectionSceneController();

    @Test
    void testExistingCollectionName() {
        assertTrue(instance.checkForExistingCollectionName("Collection1", collections));
    }

    @Test
    void testNonExistingCollectionName() {
        assertFalse(instance.checkForExistingCollectionName("Collection5", collections));
    }

    @Test
    void testMatchingWithTrimmedSpaces() {
        assertTrue(instance.checkForExistingCollectionName(" Collection3 ", collections)); // should match Collection3 with spaces trimmed
    }

    @Test
    void testEmptyName() {
        assertFalse(instance.checkForExistingCollectionName(" ", collections)); // empty string should not match any collection
    }

    @Test
    void testNullName() {
        assertFalse(instance.checkForExistingCollectionName(" s", collections)); // null should not match any collection
    }

    @Test
    void testCaseSensitiveComparison() {
        assertFalse(instance.checkForExistingCollectionName("collection1", collections)); // test case sensitivity
    }

    @Test
    void testWhitespaceOnlyName() {
        assertFalse(instance.checkForExistingCollectionName("   ", collections)); // whitespace only should not match any collection
    }
}