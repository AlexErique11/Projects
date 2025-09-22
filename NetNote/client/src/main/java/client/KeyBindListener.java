
package client;

import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class KeyBindListener {

    /**
     * Counts the number of leading spaces on a string
     * @param text a String
     * @return return the number of leading spaces of the parameter text
     */
    public int countLeadingSpaces(String text) {
        int leadingSpaces = 0;
        for (char c : text.toCharArray()) {
            if (c == ' ') {
                leadingSpaces++;
            }
            else {
                break;
            }
        }
        return leadingSpaces;
    }

    /**
     * @param text text which might be bolded
     * @return returns true when text is surrounded by double asterixes (**), return false when this is not the case.
     */
    public static boolean isBolded(String text) {
        return text.startsWith("**") && text.endsWith("**");
    }

    /**
     * @param text text which might be italic
     * @return returns true when text is surrounded by single asterixes (*),
     * returns false when it is bolded or not surrounded by single asterixes.
     */
    public static boolean isItalic(String text) {
        return text.startsWith("*") && text.endsWith("*") && !text.startsWith("**") && !text.endsWith("**");
    }

    private String innerText = "";
    private int segmentCount = 0;

    /**
     * Call function to manipulate innerText and segmentCount
     * @param selectionText selectionText
     * @param innertext inrText
     * @param isBold if is Boled
     * @return Returns innerText (redundant)
     */
    private String getInnerTextforBoldingArea(String selectionText, String innertext, Boolean isBold) {
        if (isBold){
            boolean hasOuterBoldWrapper = isBolded(selectionText);
            System.out.println("isBolded: " + hasOuterBoldWrapper);
            if (hasOuterBoldWrapper) {
                innerText = selectionText.substring(2, selectionText.length() - 2); // Remove ** from both ends
                System.out.println("NewInnerText: " + innerText);
            }
            if (segmentCount > 0) {
                innerText = innerText.replaceAll("\\*\\*", "");
                innerText = "**" + innerText + "**";
            } else {
                if (hasOuterBoldWrapper) {
                    innerText = innerText.trim();
                } else {
                    innerText = "**" + innerText + "**";
                }
            }
        } else { // Italic
            boolean hasOuterItalicWrapper = isItalic(selectionText);
            System.out.println("isItalic: " + hasOuterItalicWrapper);
            if (hasOuterItalicWrapper) {
                innerText = selectionText.substring(1, selectionText.length() - 1); // Remove * from both ends
                System.out.println("NewInnerText: " + innerText);
            }
            if (segmentCount > 0) {
                innerText = innerText.replaceAll("(?<!\\*)\\*(?!\\*)", "");
                innerText = "*" + innerText + "*";
            } else {
                if (hasOuterItalicWrapper) {
                    innerText = innerText.trim();
                } else {
                    innerText = "*" + innerText + "*";
                }
            }
        }
        return innerText;
    }

    /** Bolds a specified piece of String
     * <p>if "**text**" is selected and bolded, return the unbolded ("text")</p>
     * <p>if "this is **text**" is selected and bolded, return the totalled bold: "**this is text**" </p>
     * <p>if "this **is text** and" is selected and bolded, return the totalled bold: "**text is text and**"</p>
     * <p>These 3 rules define how a string is bolded in text, and ensures that bolding is done like this --> <a href="https://developers.google.com/docs/api/how-tos/format-text">google API</a></p>
     * @param selectionText Text to be bolded, as a String object
     * @return Returns the bolded string (or unbolded, depending on the arguments)
     * @param bolded TRUE: text should be bolded, FALSE: text should be Italicised
     */
    public String boldSelectedArea(String selectionText, Boolean bolded) {
        segmentCount = 0;

        if (selectionText == null || selectionText.isEmpty()) {
            return "";
        }
        // Record space around selected area and Trim
        int frontSpaces = 0;
        int backSpaces = 0;

        if (selectionText.length() != selectionText.trim().length()) {
            frontSpaces = countLeadingSpaces(selectionText);
            backSpaces = selectionText.length() - selectionText.trim().length() - frontSpaces;
            selectionText = selectionText.trim();
        }

        String frontSpacesString = " ".repeat(frontSpaces);
        String backSpacesString = " ".repeat(backSpaces);

        // Find Patterns
        innerText = selectionText;
        Pattern boldPattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
        Pattern italicPattern = Pattern.compile("(?<!\\*)\\*(.+?)\\*(?!\\*)");

        Matcher matcher = boldPattern.matcher(innerText);
        if (!bolded){ matcher = italicPattern.matcher(innerText); }

        while (matcher.find()) {
            segmentCount++;
        }

        // Differentiate between bolded and Italic
        if (bolded) { // BOLD
            innerText = getInnerTextforBoldingArea(selectionText, innerText, true);
        }
        else { // ITALIC
            innerText = getInnerTextforBoldingArea(selectionText, innerText, false);
        }
        return frontSpacesString + innerText + backSpacesString;
    }

    /**
     * General constructor for this class
     */
    public KeyBindListener(){}

    /** Constructor for handling all keybinds when using a textArea
     * @param textArea textArea FXML element the constructor works on
     */
    public KeyBindListener(TextArea textArea) {

        KeyCombination keyCombiBold = new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN);
        KeyCombination keyCombiItal = new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN);
        KeyCombination keyCombiUndo = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);

        textArea.setOnKeyPressed(event -> {
            String selectedText = textArea.getSelectedText();
            String text = textArea.getText();
            int startIndex = textArea.getSelection().getStart();
            int endIndex = textArea.getSelection().getEnd();

            if (keyCombiBold.match(event)) {
                String boldedSelection = boldSelectedArea(selectedText, true);

                String modifiedText = text.substring(0, startIndex) + boldedSelection + text.substring(endIndex);
                textArea.setText(modifiedText);
                textArea.positionCaret(endIndex + boldedSelection.length() - selectedText.length());
            }
            else if (keyCombiItal.match(event)) {
                System.out.println("O.01");
                String italicisedSection = boldSelectedArea(selectedText, false);

                String modifiedText = text.substring(0, startIndex) + italicisedSection + text.substring(endIndex);
                textArea.setText(modifiedText);
                textArea.positionCaret(endIndex + italicisedSection.length() - selectedText.length());
            }
            //else if (keyCombiUndo.match(event)) {
            //    System.out.println("Undo");
            //    var.resolve();
            //}

        });

    }
}

