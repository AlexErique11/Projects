package server.service;

import commons.Note;
import commons.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.database.NoteRepository;
import server.database.TagRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NoteSrv {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private NoteRepository noteRepository;


    /**
     * This method is used to save a Note entity along with associated Tag entities.
     *
     * First, it parses the text of the note to identify tags. These new or existing tags get saved in the database.
     * The tags are then linked to the note. Finally, the note, with its tags, is saved to the database.
     *
     * @param note The Note entity which is to be saved, with the text containing hashtags.
     * @return Note This returns the saved Note entity, with associated tags.
     *
     */
    public Note saveNoteWithTags(Note note) {
        Set<Tag> tags = parseTags(note.getText());

        // Save all the new Tags to the database
        tagRepository.saveAll(tags);

        // Link the tags to the note
        note.setTags(tags);

        // Save the note to the database
        return noteRepository.save(note);
    }

    /**
     * This method is used to parse hashtags from a provided text.
     *
     * It identifies hashtags within the text, fetches or creates corresponding Tag entities,
     * and returns a Set of these tags.
     * For the new tag, it gets created and added to the set without being saved.
     *
     * @param text This is a string from where hashtags need to be extracted.
     * @return Set<Tag> This returns a Set of tags identified from the input text.
     */
    private Set<Tag> parseTags(String text) {
        Pattern pattern = Pattern.compile("\\B#\\w+"); // Matches hashtags like #tag
        Matcher matcher = pattern.matcher(text);

        Set<Tag> tags = new HashSet<>();
        while (matcher.find()) {
            String tagName = matcher.group().substring(1); // Remove the '#' symbol
            // Fetch or create the Tag entity. For the new tag, just creating it without saving now.
            Tag tag = tagRepository.findByName(tagName)
                    .orElse(new Tag(tagName));
            tags.add(tag);
        }
        return tags;
    }

    /**
     * This method processes the text of a Note entity, replacing note title markers with actual HTML links.
     *
     * More specifically, it looks for markers in the format [[Any Text]], attempting to match 'Any Text' with
     * the title of an existing note. If a matching note is found, it replaces the marker with a clickable link
     * to the note. If no matching note is found, the marker is replaced with a 'Note not found' message.
     *
     * Note links have the following format: http://localhost:8080/notes/<noteId>
     *
     * @param note The Note entity whose text is to be processed.
     * @return String This returns the processed text with note title markers replaced as described.
     *
     */
    public String createLinksInNotes(Note note) {
        String noteText = note.getText();

        Pattern pattern = Pattern.compile("\\[\\[(.*?)\\]\\]");  // Matches [[Any text]]
        Matcher matcher = pattern.matcher(noteText);

        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String linkedNoteTitle = matcher.group(1);
            Optional<Note> optLinkedNote = noteRepository.findByTitle(linkedNoteTitle);

            if (optLinkedNote.isPresent()) {
                Note linkedNote = optLinkedNote.get();
                // Replace [[Any text]] with a URL to the note
                // notes are accessible by http://<server>/notes/<noteId>
                String noteLink = String.format("<a href=\"http://localhost:8080/notes/%d\">%s</a>", linkedNote.getId(), linkedNoteTitle);
                matcher.appendReplacement(sb, noteLink);
            } else {
                // replace [[Title]] with a "Note not found" message
                String notFoundMessage = String.format("[[%s could not be found]]", linkedNoteTitle);
                matcher.appendReplacement(sb, notFoundMessage);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    /**
     * This method fetches and returns all distinct notes associated with any of the specified tags.
     *
     * It uses the tag names provided in the input list to search for corresponding Tag entities in the repository.
     * The method then fetches all Note entities associated with these tags, taking care to ensure that the
     * returned list of notes doesn't contain any duplicates.
     *
     * @param tagNames A list of tag names for which associated notes are to be found.
     * @return List<Note> This returns a list of distinct Note entities associated with any of the specified tags.
     */
    public List<Note> findNotesByTags(List<String> tagNames) {
        return noteRepository.findDistinctByTagsNameIn(tagNames);
    }
}
