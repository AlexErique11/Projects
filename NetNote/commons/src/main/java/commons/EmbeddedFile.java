package commons;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
public class EmbeddedFile implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonView(Views.Public.class)
    private Long id;

    @JsonView(Views.Public.class)
    private String name; // File name

    @JsonView(Views.Public.class)
    private String type; // MIME type (e.g., image/png, application/pdf)

    @JsonView(Views.Detailed.class)
    @Lob // Large object for storing binary data
    private byte[] content; // File content

    /**
     * Gets the unique identifier of the file.
     * @return The ID of the file.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the file.
     * @param id The ID to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the name of the file.
     * @return The file name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the file.
     * @param name The file name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the MIME type of the file.
     * @return The MIME type of the file.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the MIME type of the file.
     * @param type The MIME type to set (e.g., "image/png").
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the binary content of the file.
     * @return The file content as a byte array.
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Sets the binary content of the file.
     * @param content The file content to set as a byte array.
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * Returns a string representation of the embedded file.
     * This does not include the file content to prevent large or sensitive data from being logged.
     *
     * @return A string containing the file's ID, name, and type.
     */
    @Override
    public String toString() {
        return "EmbeddedFile{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}