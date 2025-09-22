package client;

import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.Test;

import java.security.Key;

import static org.junit.jupiter.api.Assertions.*;

class KeyBindListenerTest {
    private KeyBindListener utils = new KeyBindListener();

    @Test
    void testCountLeadingSpacesNoSpaces() {
        assertEquals(0, utils.countLeadingSpaces("No leading spaces"));
    }

    @Test
    void testCountLeadingSpacesWithSpaces() {
        assertEquals(3, utils.countLeadingSpaces("   Three leading spaces"));
    }

    @Test
    void testCountLeadingSpacesEmptyString() {
        assertEquals(0, utils.countLeadingSpaces(""));
    }

    @Test
    void testCountLeadingSpacesNoLeadingSpaces() {
        assertEquals(0, utils.countLeadingSpaces("Text with no spaces"));
    }

    @Test
    void testCountLeadingSpacesSpacesFollowedByText() {
        assertEquals(4, utils.countLeadingSpaces("    Text with four spaces"));
    }

    @Test
    void testIsBoldedTrue() {
        assertTrue(KeyBindListener.isBolded("**bolded text**"));
    }

    @Test
    void testIsBoldedFalseNoSurroundingAsterisks() {
        assertFalse(KeyBindListener.isBolded("not bolded text"));
    }

    @Test
    void testIsBoldedFalseSingleAsterisks() {
        assertFalse(KeyBindListener.isBolded("*italic text*"));
    }

    @Test
    void testIsBoldedFalseOnlyOpeningAsterisks() {
        assertFalse(KeyBindListener.isBolded("**only opening bolded text"));
    }

    @Test
    void testIsBoldedFalseOnlyClosingAsterisks() {
        assertFalse(KeyBindListener.isBolded("only closing bolded text**"));
    }

    @Test
    void testIsBoldedEmptyString() {
        assertFalse(KeyBindListener.isBolded(""));
    }

    @Test
    void testIsItalicTrue() {
        assertTrue(KeyBindListener.isItalic("*italic text*"));
    }

    @Test
    void testIsItalicFalseBoldedText() {
        assertFalse(KeyBindListener.isItalic("**bolded text**"));
    }

    @Test
    void testIsItalicFalseNoSurroundingAsterisks() {
        assertFalse(KeyBindListener.isItalic("not italic text"));
    }
    @Test
    void testBoldSelectedAreaAlreadyBoldWithExtraFormatting() {
        String input = "  **this is bolded text**  ";
        String expected = "  **this is bolded text**  ";
        assertEquals(expected, utils.boldSelectedArea(input, true));
    }

    @Test
    void testItalicSelectedAreaAlreadyItalicWithExtraFormatting() {
        String input = "  *this is italic text*  ";
        String expected = "  *this is italic text*  ";
        assertEquals(expected, utils.boldSelectedArea(input, false));
    }

    @Test
    void testBoldSelectedAreaWithInnerItalicSegments() {
        String input = "this is *italic* but should be bolded";
        String expected = "**this is *italic* but should be bolded**";
        assertEquals(expected, utils.boldSelectedArea(input, true));
    }

    @Test
    void testItalicSelectedAreaWithInnerBoldSegments() {
        String input = "this is **bold** but should be italicized";
        String expected = "*this is **bold** but should be italicized*";
        assertEquals(expected, utils.boldSelectedArea(input, false));
    }

    @Test
    void testBoldSelectedAreaComplexNestedFormatting() {
        String input = "*italic* and **bold** in the same sentence";
        String expected = "***italic* and bold in the same sentence**";
        assertEquals(expected, utils.boldSelectedArea(input, true));
    }

    @Test
    void testItalicSelectedAreaComplexNestedFormatting() {
        String input = "**bold** and *italic* in the same sentence";
        String expected = "***bold** and italic in the same sentence*";
        assertEquals(expected, utils.boldSelectedArea(input, false));
    }

    @Test
    void testBoldSelectedAreaTrimSpacesPreserveFormatting() {
        String input = "   ** bolded **   ";
        String expected = "   ** bolded **   ";
        assertEquals(expected, utils.boldSelectedArea(input, true));
    }

}