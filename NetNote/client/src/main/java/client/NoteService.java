package client;

import commons.Note;

import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jvnet.hk2.annotations.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoteService {

    /**
     * Injection-Specific Contructor to generic Class {@code NoteService}
     */
    @Inject
    public NoteService() {}

    /**
     * Filters notes based on the search query.
     *
     * @param notes       The list of notes to filter.
     * @param searchQuery The string to filter on.
     * @return A list of notes matching the search query.
     */
    public ObservableList<Note> filtering(ObservableList<Note> notes, String searchQuery) {
        return notes.filtered(note -> {
            String title = note.getTitle();
            String text = note.getText();
            return (title != null && title.contains(searchQuery)) ||
                    (text != null && text.contains(searchQuery));
        });
    }

    /**
     * Highlights occurrences of a search query within the text of a note.
     * The method differentiates between normal text and tags (e.g., #tag) and
     * applies the highlighting accordingly.
     *
     * - If the search query matches an existing tag (wrapped in <span class='tag'>),
     *   the background color of the tag is modified directly.
     * - If the search query appears as plain text, it is wrapped in <mark> tags to
     *   apply highlighting.
     *
     * @param note        The note object containing the text to be searched.
     * @param searchQuery The search string to highlight within the note.
     * @return A string representing the note's text with the search query highlighted.
     *         If no match is found, the original note text is returned.
     */
    public String highlightTextInNotes(Note note, String searchQuery) {
        String noteText = note.getText();
        StringBuffer highlightedText = new StringBuffer();

        if (noteText != null && noteText.contains(searchQuery)) {
            // Match full tags or note links, while preventing partial tag highlighting
            Pattern pattern = Pattern.compile(
                    "(\\[\\[[^\\]]*?" + Pattern.quote(searchQuery) + "[^\\]]*?\\]\\])|" +
                            // Skip note links like [[pasta]]
                            "(#\\w*" + Pattern.quote(searchQuery) + "\\w*)|" +
                            // Match full tags like #StudyMaterial
                            "(<span class='tag'[^>]*?>#?" + Pattern.quote(searchQuery) + "</span>)|" +
                            // Highlight existing #tag
                            "(" + Pattern.quote(searchQuery) + ")",
                    // Allow partial text match (removed \\b)
                    Pattern.CASE_INSENSITIVE
            );

            Matcher matcher = pattern.matcher(noteText);

            while (matcher.find()) {
                if (matcher.group(1) != null) {
                    // Skip note links (e.g., [[pasta]])
                    matcher.appendReplacement(highlightedText, matcher.group(1));
                } else if (matcher.group(2) != null) {
                    // Highlight full matching tag (e.g., #StudyMaterial)
                    matcher.appendReplacement(highlightedText, "<mark>" + matcher.group(2) + "</mark>");
                } else if (matcher.group(3) != null) {
                    // Highlight existing tags with inline style
                    String existingTag = matcher.group(3);
                    matcher.appendReplacement(highlightedText,
                            existingTag.replace("class='tag'", "class='tag' style='background-color: yellow;'"));
                } else if (matcher.group(4) != null) {
                    // Highlight regular text match (outside of tags)
                    matcher.appendReplacement(highlightedText, "<mark>" + matcher.group(4) + "</mark>");
                }
            }
            matcher.appendTail(highlightedText);
        }

        return highlightedText.length() > 0 ? highlightedText.toString() : noteText;
    }

    /**
     * This method gets a prompt name and compares it to all the names
     * of all the titles. If there is a match it adds an inde at the end
     * @param prompt String The title we are going to compare to
     * @param titlesWithPrompt A list of strings containing all titles of all notes
     * @return a String with the altered name (with an index if needed)
     */
    public String handleNumberAssigning(String prompt, List<String> titlesWithPrompt){
        int i = 1;
        boolean checkContains = false;

        for(String titles : titlesWithPrompt){
            checkContains = false;
            for(String titlesCheck : titlesWithPrompt) {
                if (titlesCheck.equals(prompt + " " + i)) {
                    checkContains = true;
                    break;
                }
            }
            if(!checkContains){
                return prompt + " " + (i);
            }
            i++;
        }
        return prompt + " " + (i + 1);
    }

    /**
     * This method find a name with the same name from a list of all notes
     * @param title A title of the note
     * @param allNotes A list of all the notes in the container (visible to the person)
     * @return A Note with the same name as the one you have searched
     */
    public Note findNoteByTitle(String title, ObservableList<Note> allNotes) {
        for (Note note : allNotes) {
            if (note.getTitle().equalsIgnoreCase(title)) {
                return note;
            }
        }
        return null;
    }

    /**
     * Parses and replaces specific elements within the provided HTML content.
     * This method performs three primary operations:
     * 1. Converts markdown-style headers (# Header) into proper HTML header tags.
     * 2. Identifies and transforms inline tags (#tag) into clickable HTML span elements for styling and interaction.
     * 3. Converts note links in the format [[Note Title]] into HTML anchor tags for easy navigation.
     *
     * @param content The HTML content to be parsed and modified. This is typically generated from markdown.
     * @param allNotes An observable list of notes of all visible notes to the user
     * @return A String containing the modified HTML content with headers, tags, and links appropriately replaced.
     *
     */
    public String parseAndReplaceTagsInText(String content, ObservableList<Note> allNotes) {
        if (content == null || content.isEmpty()) return "";
        // Add inline CSS and JavaScript
        StringBuilder cssStyles = new StringBuilder();
        cssApend(cssStyles);
        // Handle Markdown Headers <p> tags
        content = content.replaceAll("(?m)<p>\\s*#\\s(.+?)</p>", "<h1>$1</h1>");
        content = content.replaceAll("(?m)<p>\\s*##\\s(.+?)</p>", "<h2>$1</h2>");
        content = content.replaceAll("(?m)<p>\\s*###\\s(.+?)</p>", "<h3>$1</h3>");
        // Handle inline tags (#tag)
        Pattern tagPattern = Pattern.compile("(?<!\\w)(#\\w+)");
        Matcher tagMatcher = tagPattern.matcher(content);
        StringBuffer tagBuffer = new StringBuffer();

        while (tagMatcher.find()) {
            String tag = tagMatcher.group(1); // Full match with #
            String cleanTag = tag.substring(1); // Remove # for display
            String replacement = "<span class='tag' data-tag='" + tag + "' onclick='onTagClicked(\"" + tag + "\")'>"
                    + cleanTag + "</span>"; // Render without #
            tagMatcher.appendReplacement(tagBuffer, replacement);
        }
        tagMatcher.appendTail(tagBuffer);
        // Handle [[other note]] links
        Pattern linkPattern = Pattern.compile("\\[\\[(.*?)\\]\\]");
        Matcher linkMatcher = linkPattern.matcher(tagBuffer.toString());
        StringBuffer linkBuffer = new StringBuffer();
        while (linkMatcher.find()) {
            String noteTitle = linkMatcher.group(1);
            Note linkedNote = findNoteByTitle(noteTitle, allNotes);

            Locale locale = loadSavedLanguage();
            ResourceBundle bundle = ResourceBundle.getBundle("languages", locale);

            // Apply different styling for existing vs. non-existing notes
            String linkReplacement;
            if (linkedNote != null) {
                // Existing note: style as a valid link
                linkReplacement = "<a href='#' class='note-link valid-link' data-note-title='" + noteTitle + "'>"
                        + noteTitle + "</a>";
            } else {
                // Non-existing note: style as an invalid link with a tooltip suggesting a close match
                String closestMatch = findClosestMatchingNoteTitle(noteTitle, allNotes);
                // Build the tooltip text
                String tooltipText;
                if (closestMatch != null) {
                    tooltipText = bundle.getString("note_not_found") + ". "
                            + bundle.getString("did_you_mean") + " \"" + closestMatch + "\"?";
                } else {
                    tooltipText = bundle.getString("note_not_found") + ".";
                }

// Build the replace button HTML
                String replaceButton = "";
                if (closestMatch != null) {
                    replaceButton = "<button onclick='replaceReference(\"" + noteTitle + "\", \""
                            + closestMatch + "\", event)'>"
                            + bundle.getString("replace_button_text") + "</button>";
                }

// Generate the link replacement
                linkReplacement = """
    <a href='#' class='note-link invalid-link' data-note-title='%s'>
        %s
        <span class='tooltip'>
            %s %s
        </span>
    </a>
""".formatted(noteTitle, noteTitle, tooltipText, replaceButton);
            }
            linkMatcher.appendReplacement(linkBuffer, linkReplacement);
        }
        linkMatcher.appendTail(linkBuffer);
        return cssStyles.toString() + linkBuffer.toString();
    }

    private StringBuilder cssApend(StringBuilder cssStyles){
        cssStyles.append("""
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
            """);
        return cssStyles;
    }


    /**
     * Finds the closest matching note title from a list of all notes based
     * on the Levenshtein distance algorithm.
     * @param noteTitle The title of the note to match against the list.
     * @param allNotes A list of `Note` objects containing titles to compare with the given `noteTitle`.
     * @return String - the title of the closest matching note, or null if no close match is found
     */
    public String findClosestMatchingNoteTitle(String noteTitle, ObservableList<Note> allNotes) {
        String closestMatch = null;
        int minDistance = Integer.MAX_VALUE;


        for (Note note : allNotes) {
            int distance = calculateLevenshteinDistance(noteTitle.toLowerCase(), note.getTitle().toLowerCase());
            if (distance < minDistance) {
                minDistance = distance;
                closestMatch = note.getTitle();
            }
        }

        // Define a threshold to only suggest reasonably close matches
        return (minDistance <= 6) ? closestMatch : null;
    }
    /**
     * Calculates the Levenshtein distance between two strings.
     * It counts the minimum number of single-character edits (insertions, deletions,
     * or substitutions) required to change one string into the other.
     *
     * @param s1 The first string to compare.
     * @param s2 The second string to compare.
     * @return int the Levenshtein distance between the two strings.
     */
    public int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }

    private Locale loadSavedLanguage() {
        try (InputStream input = new FileInputStream("config.properties")) {
            Properties props = new Properties();
            props.load(input);
            String lang = props.getProperty("language");
            return Locale.forLanguageTag(lang.replace('_', '-'));
        } catch (IOException e) {
            return Locale.ENGLISH; // Fallback to English
        }
    }

}