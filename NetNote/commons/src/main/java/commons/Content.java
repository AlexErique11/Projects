package commons;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@JsonIdentityInfo
        (generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
@Entity
@Table(name = "Content")
public class Content implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long contentID;

    @Column(length = 500)
    private String text;

    @Column(name = "file_path", nullable = true, length = 255)
    private String filePath;

    @Column(name = "image_path", nullable = true, length = 255)
    private String imagePath;

    @ManyToMany
    @JoinTable(
            name = "Content_Tags",
            joinColumns = @JoinColumn(name = "content_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();


    /**
     * A default constructor - Make sure that the code compiles
     * after creating any other constructors for this entity!!!
     */
    public Content() {

    }
    /**
     * A constructor that initializes the text attribute of the Content entity.
     *
     * @param text The text content of the entity.
     */
    public Content(String text) {
        this.text = text;
    }

    /**
     * A constructor that initializes the text filePath and imagePath.
     *
     * @param text The text content of the entity.
     * @param filePath The file path of the entity.
     * @param imagePath The image path of the entity.
     */
    public Content(String text, String filePath, String imagePath) {
        this.text = text;
        this.filePath = filePath;
        this.imagePath = imagePath;
    }

    /**
     * A constructor that initializes the text and tags attributes of the Content entity.
     *
     * @param text The text content of the entity.
     * @param tags The set of tags associated with the entity.
     */
    public Content(String text, Set<Tag> tags) {
        this.text = text;
        this.tags = tags;
    }

    /**
     * A constructor that initializes the text, filePath,
     * imagePath, and tags attributes of the Content entity.
     *
     * @param text The text content of the entity.
     * @param filePath The file path of the entity.
     * @param imagePath The image path of the entity.
     * @param tags The set of tags associated with the entity.
     */
    public Content(String text, String filePath, String imagePath, Set<Tag> tags) {
        this.text = text;
        this.filePath = filePath;
        this.imagePath = imagePath;
        this.tags = tags;
    }



    /**
     * This method processes the input text and replaces all hashtags
     * with corresponding Markdown links.
     *
     * It first defines a regex pattern that matches hashtags in the text.
     * Then it goes through the text and replaces
     * each found hashtag with a Markdown link that leads to the "/tag/"
     * page followed by the name of the tag.
     *
     * @param text The input string containing hashtags.
     * @return String This returns the processed string with
     * hashtags replaced by Markdown links.
     */

    private String replaceTagsWithMarkdownLinks(String text) {
        Pattern pattern = Pattern.compile("(\\s|^)#(\\w+)");
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            // Matched hashtags are replaced by Markdown links
            matcher.
                    appendReplacement(sb, " [" + matcher.group(2) +
                                       "](/tag/" + matcher.group(2) + ")");
        }

        matcher.appendTail(sb);
        return sb.toString();
    }




    /**
     * This method retrieves the ID of the Content entity.
     *
     * @return Long This returns the ID of the Content entity.
     */
    public Long getContentID() {
        return contentID;
    }

    /**
     * This method sets the ID of the Content entity.
     *
     * @param contentID The ID of the Content entity.
     */
    public void setContentID(Long contentID) {
        this.contentID = contentID;
    }

    /**
     * This method retrieves the text content of the entity.
     *
     * @return String This returns the text content of the entity.
     */
    public String getText() {
        return text;
    }

    /**
     * This method retrieves the set of tags associated with the entity.
     *
     * @return Set<Tag> This returns the set of tags associated with the entity.
     */
    public Set<Tag> getTags() {
        return tags;
    }

    /**
     * This method sets the set of tags associated with the entity.
     *
     * @param tags The set of tags to be associated with the entity.
     */
    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    /**
     * This method adds a Tag entity to the set of tags associated with the entity.
     *
     * @param text The text
     */
    public void setText(String text) {
        this.text = replaceTagsWithMarkdownLinks(text);
    }


    /**
     * This method retrieves the file path of the entity.
     *
     * @return String This returns the file path of the entity.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * This method sets the file path of the entity.
     *
     * @param filePath The file path of the entity.
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * This method retrieves the image path of the entity.
     *
     * @return String This returns the image path of the entity.
     */

    public String getImagePath() {
        return imagePath;
    }

    /**
     * This method sets the image path of the entity.
     *
     * @param imagePath The image path of the entity.
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * This method retrieves the set of tags associated with the entity.
     *
     * @return Set<Tag> This returns the set of tags associated with the entity.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Content content = (Content) o;
        return Objects.equals(contentID, content.contentID);
    }

    /**
     * Hashcode
     * @return hashcode for the Content
     */
    @Override
    public int hashCode() {
        return Objects.hash(contentID);
    }

    /**
     * A to String method
     * @return a readable String representing the Content
     */
    @Override
    public String toString() {
        return "Content{" +
                "contentID=" + contentID +
                ", text='" + text + '\'' +
                ", filePath='" + filePath + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}

