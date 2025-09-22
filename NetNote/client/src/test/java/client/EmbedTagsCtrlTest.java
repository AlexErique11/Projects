package client;

import commons.Note;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EmbedTagsCtrlTest {

    private EmbedTagsCtrl ec = new EmbedTagsCtrl();

    private ObservableList<Note> allNotes;

    @Test
    void testNullContent() {
        assertEquals("", ec.parseAndReplaceTagsInText(null));
    }

    @Test
    void testEmptyContent() {
        assertEquals("", ec.parseAndReplaceTagsInText(""));
    }

    @Test
    void testSingleMarkdownHeader() {
        String input = "<p># Header</p>";
        String expected = "<h1>Header</h1>";
        assertEquals(expected, ec.parseAndReplaceTagsInText(input));
    }

    @Test
    void testMultipleHeaders() {
        String input = "<p># Header1</p>\n<p>## Header2</p>\n<p>### Header3</p>";
        String expected = "<h1>Header1</h1>\n<h2>Header2</h2>\n<h3>Header3</h3>";
        assertEquals(expected, ec.parseAndReplaceTagsInText(input));
    }

    @Test
    void testInlineTagReplacement() {
        String input = "This is a #tag.";
        String expected = "This is a <span class='tag' data-tag='#tag' onclick='onTagClicked(\"#tag\")'>tag</span>.";
        assertEquals(expected, ec.parseAndReplaceTagsInText(input));
    }

    @Test
    void testMultipleInlineTags() {
        String input = "Multiple tags: #tag1, #tag2, and #tag3.";
        String expected = "Multiple tags: <span class='tag' data-tag='#tag1' onclick='onTagClicked(\"#tag1\")'>tag1</span>, " +
                "<span class='tag' data-tag='#tag2' onclick='onTagClicked(\"#tag2\")'>tag2</span>, and " +
                "<span class='tag' data-tag='#tag3' onclick='onTagClicked(\"#tag3\")'>tag3</span>.";
        assertEquals(expected, ec.parseAndReplaceTagsInText(input));
    }

    @Test
    void testNoteLinkReplacement() {
        String input = "Refer to [[Note Title]].";
        String expected = "Refer to <a href='#' class='note-link' data-note-title='Note Title'>Note Title</a>.";
        assertEquals(expected, ec.parseAndReplaceTagsInText(input));
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
    void testNullInput() {
        assertEquals(Set.of(), ec.extractTags(null));
    }

    @Test
    void testEmptyInput() {
        assertEquals(Set.of(), ec.extractTags(""));
    }

    @Test
    void testSingleTag() {
        assertEquals(Set.of("tag"), ec.extractTags("#tag"));
    }

    @Test
    void testMultipleTags() {
        assertEquals(Set.of("tag1", "tag2", "tag3"), ec.extractTags("#tag1 #tag2 #tag3"));
    }

    @Test
    void testDuplicateTags() {
        assertEquals(Set.of("tag"), ec.extractTags("#tag #tag"));
    }

    @Test
    void testTagsWithText() {
        assertEquals(Set.of("tag1", "tag2"), ec.extractTags("This is a #tag1 with another #tag2."));
    }

    @Test
    void testTagsWithSpecialCharacters() {
        assertEquals(Set.of("tag", "tag_with_underscore"), ec.extractTags("#tag #tag-with-dash #tag_with_underscore"));
    }

}