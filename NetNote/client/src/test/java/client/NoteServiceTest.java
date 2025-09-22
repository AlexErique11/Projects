package client;

import commons.Note;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NoteServiceTest {
    private ObservableList<Note> allNotes;
    private NoteService noteService = new NoteService();

    @BeforeEach
    public void setUp() {
        noteService = new NoteService();
        allNotes = FXCollections.observableArrayList();

        Note note1 = new Note();
        note1.setTitle("Meeting Notes");
        allNotes.add(note1);

        Note note2 = new Note();
        note2.setTitle("Shopping List");
        allNotes.add(note2);

        Note note3 = new Note();
        note3.setTitle("To-Do");
        allNotes.add(note3);

        Note note4 = new Note();
        note4.setTitle("Daily Journal");
        allNotes.add(note4);
    }

    @Test
    public void testFindNoteByTitleFound() {
        Note result = noteService.findNoteByTitle("Shopping List", allNotes);
        assertNotNull(result);
        assertEquals("Shopping List", result.getTitle());
    }

    @Test
    public void testFindNoteByTitleCaseInsensitive() {
        Note result = noteService.findNoteByTitle("shopping list", allNotes);
        assertNotNull(result);
        assertEquals("Shopping List", result.getTitle());
    }

    @Test
    public void testFindNoteByTitleNotFound() {
        Note result = noteService.findNoteByTitle("Non-Existent Note", allNotes);
        assertNull(result);
    }

    @Test
    void filtering() {
        Note n1 = new Note();
        n1.setTitle("This is test text");
        n1.setText("This is a test title");
        Note n2 = new Note();
        n2.setTitle("Cosine^2");
        n2.setText("Sin^2");

        ArrayList<Note> notess = new ArrayList<>();
        notess.add(n1);
        notess.add(n2);
        ObservableList<Note> notes = FXCollections.observableArrayList(notess);
        ArrayList<Note> expectedNotes = new ArrayList<>();
        expectedNotes.add(n1);
        assertEquals(expectedNotes, noteService.filtering(notes,"Sin"));
    }

    @Test
    void highlightTextInNotes() {
        Note n1 = new Note();
        n1.setTitle("This is test text");
        n1.setText("This is a test title");
        assertEquals(noteService.highlightTextInNotes(n1,"test"),
                "This is a <mark>test</mark> title");
    }

    @Test
    void highlightTextInNotesNoMatch() {
        Note n1 = new Note();
        n1.setTitle("This is test text");
        n1.setText("This is a test title");
        assertEquals(noteService.highlightTextInNotes(n1,"testt"),
                "This is a test title");
    }

    @Test
    void highlightTextInNotesURL() {
        Note n1 = new Note();
        n1.setTitle("This is test text");
        n1.setText("(testURL)");
        assertEquals(noteService.highlightTextInNotes(n1,"test"),
                "(<mark>test</mark>URL)");
    }

    @Test
    public void testHandleNumberAssigningNoConflict() {
        List<String> titles = Arrays.asList("Note 1", "Note 2", "Note 3");
        String result = noteService.handleNumberAssigning("New Note", titles);
        assertEquals("New Note 1", result);
    }

    @Test
    public void testHandleNumberAssigningConflict() {
        List<String> titles = Arrays.asList("Note 1", "Note 2", "New Note 1");
        String result = noteService.handleNumberAssigning("New Note", titles);
        assertEquals("New Note 2", result);
    }

    @Test
    public void testHandleNumberAssigningMultipleConflicts() {
        List<String> titles = Arrays.asList("Note 1", "Note 2", "New Note 1", "New Note 2", "New Note 3");
        String result = noteService.handleNumberAssigning("New Note", titles);
        assertEquals("New Note 4", result);
    }

    @Test
    public void testHandleNumberAssigningNoSuffixConflict() {
        List<String> titles = Arrays.asList("Note 1", "Note 2", "New Note");
        String result = noteService.handleNumberAssigning("New Note", titles);
        assertEquals("New Note 1", result);
    }

    @Test
    public void testHandleNumberAssigningEmptyList() {
        List<String> titles = Arrays.asList();
        String result = noteService.handleNumberAssigning("New Note", titles);
        assertEquals("New Note 2", result);
    }
    private String getExpectedCssAndScript() {
        return """
<style>.tag{display:inline-block;background-color:#d0d0d0;color:#000000;border:1px solid #000000;
    border-radius:8px;padding:2px 6px;margin:2px;font-size:12px;font-family:Arial,sans-serif;cursor:pointer;}
    .tag:hover{background-color:#a0a0a0;color:#ffffff;}
    .note-link.valid-link{color:#404040;text-decoration:underline;cursor:pointer;}
    .note-link.valid-link:hover{color:#303030;}
    .note-link.invalid-link{color:red;text-decoration:underline dashed;cursor:not-allowed;position:relative;}
    .tooltip{display:none;position:absolute;top:20px;left:0;background-color:#ffdddd;color:#000000;padding:4px 8px;
    border:1px solid red;border-radius:4px;font-size:12px;white-space:nowrap;z-index:1000;}
    .note-link.invalid-link:hover .tooltip{display:block;}
</style>
<script>
    document.addEventListener('DOMContentLoaded',function(){
        const tooltips=document.querySelectorAll('.tooltip');
        tooltips.forEach(tooltip=>{
            let hideTimeout;
            tooltip.parentElement.addEventListener('mouseover',()=>
            {clearTimeout(hideTimeout);tooltip.style.display='block';});
            tooltip.parentElement.addEventListener('mouseout',()=>
            {hideTimeout=setTimeout(()=>{tooltip.style.display='none';},2000);});
        });
    });
    function replaceReference(oldReference,newReference,event){
        event.stopPropagation();
        console.log("Attempting to replace:",oldReference,"with:",newReference);
        if(window.thisApplicationBridge){
            try{window.thisApplicationBridge.onReferenceReplace(oldReference,newReference);}
            catch(e){console.error("Failed to replace reference:",e);}
        }else{
            console.error("Java bridge (thisApplicationBridge) is not set.");
        }
    }
</script>
        """;
    }

    @Test
    public void testParseAndReplaceTagsInTextHeaders() {
        String content = "<p># Header 1</p><p>## Header 2</p><p>### Header 3</p>";
        String expected = getExpectedCssAndScript() + """
                <h1>Header 1</h1><h2>Header 2</h2><h3>Header 3</h3>
                """;
        String result = noteService.parseAndReplaceTagsInText(content, allNotes);
        assertEquals(expected.trim(), result.trim());
    }

    @Test
    public void testParseAndReplaceTagsInTextNoChanges() {
        String content = "This is plain text without any special tags or links.";
        String expected = getExpectedCssAndScript() + content;
        String result = noteService.parseAndReplaceTagsInText(content, allNotes);
        assertEquals(expected.trim(), result.trim());
    }
    @Test
    public void testParseAndReplaceTagsInTextWithInlineTags() {
        String content = "This is a #tag and another #tag2 here.";
        String expected = getExpectedCssAndScript() + """
            This is a <span class='tag' data-tag='#tag' onclick='onTagClicked("#tag")'>tag</span> and another <span class='tag' data-tag='#tag2' onclick='onTagClicked("#tag2")'>tag2</span> here.
            """;
        String result = noteService.parseAndReplaceTagsInText(content, allNotes);
        assertEquals(expected.trim(), result.trim());
    }

    @Test
    public void testParseAndReplaceTagsInTextWithEmptyContent() {
        String content = "";
        String expected = getExpectedCssAndScript();
        String result = noteService.parseAndReplaceTagsInText(content, allNotes);
        assertEquals("", result.trim());
    }
    @Test
    void testFindClosestMatchingNoteTitle_ExactMatch() {
        String inputTitle = "Meeting Notes";

        String closestMatch = noteService.findClosestMatchingNoteTitle(inputTitle, allNotes);

        assertEquals("Meeting Notes", closestMatch, "The closest match should be 'Meeting Notes'");
    }

    @Test
    void testFindClosestMatchingNoteTitle_CloseMatch() {
        String inputTitle = "Shopping List";

        String closestMatch = noteService.findClosestMatchingNoteTitle(inputTitle, allNotes);

        assertEquals("Shopping List", closestMatch, "The closest match should be 'Shopping List'");
    }

    @Test
    void testFindClosestMatchingNoteTitle_NoMatch() {
        String inputTitle = "Holiday Plans";

        String closestMatch = noteService.findClosestMatchingNoteTitle(inputTitle, allNotes);

        assertNull(closestMatch, "There should be no close match for 'Holiday Plans'");
    }

    @Test
    void testCalculateLevenshteinDistance_SameStrings() {
        String s1 = "hello";
        String s2 = "hello";

        int distance = noteService.calculateLevenshteinDistance(s1, s2);

        assertEquals(0, distance, "The Levenshtein distance should be 0 for identical strings.");
    }

    @Test
    void testCalculateLevenshteinDistance_DifferentStrings() {
        String s1 = "kitten";
        String s2 = "sitting";

        int distance = noteService.calculateLevenshteinDistance(s1, s2);

        assertEquals(3, distance, "The Levenshtein distance between 'kitten' and 'sitting' should be 3.");
    }

    @Test
    void testCalculateLevenshteinDistance_EmptyStrings() {
        String s1 = "";
        String s2 = "non-empty";

        int distance = noteService.calculateLevenshteinDistance(s1, s2);

        assertEquals(9, distance, "The Levenshtein distance between an empty string and 'non-empty' should be 9.");
    }
}