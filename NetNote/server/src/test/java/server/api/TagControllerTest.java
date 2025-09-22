package server.api;

import commons.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import server.database.TagRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TagControllerTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagController tagController;

    private Tag tag1;
    private Tag tag2;

    @BeforeEach
    void setUp() {
        tag1 = new Tag("Tag1", "Red");
        tag1.setId(1L);
        tag2 = new Tag("Tag2", "Blue");
        tag2.setId(2L);
    }

    @Test
    void testGetAllTags() {
        List<Tag> tags = List.of(tag1, tag2);
        when(tagRepository.findAll()).thenReturn(tags);

        ResponseEntity<List<Tag>> response = tagController.getAllTags();

        verify(tagRepository, times(1)).findAll();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tags, response.getBody());
    }

    @Test
    void testGetTagById_Success() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag1));

        ResponseEntity<Tag> response = tagController.getTagById(1L);

        verify(tagRepository, times(1)).findById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tag1, response.getBody());
    }

    @Test
    void testGetTagById_NotFound() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Tag> response = tagController.getTagById(99L);

        verify(tagRepository, times(1)).findById(99L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testCreateTag() {
        when(tagRepository.save(tag1)).thenReturn(tag1);

        ResponseEntity<Tag> response = tagController.createTag(tag1);

        verify(tagRepository, times(1)).save(tag1);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(tag1, response.getBody());
    }

    @Test
    void testUpdateTag_Success() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag1));
        when(tagRepository.save(tag1)).thenReturn(tag1);

        Tag updatedDetails = new Tag("Updated Tag", "Yellow");
        ResponseEntity<Tag> response = tagController.updateTag(1L, updatedDetails);

        verify(tagRepository, times(1)).findById(1L);
        verify(tagRepository, times(1)).save(tag1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Tag", tag1.getName());
        assertEquals("Yellow", tag1.getColor());
    }

    @Test
    void testUpdateTag_NotFound() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        Tag updatedDetails = new Tag("Updated Tag", "Yellow");
        ResponseEntity<Tag> response = tagController.updateTag(99L, updatedDetails);

        verify(tagRepository, times(1)).findById(99L);
        verify(tagRepository, times(0)).save(any(Tag.class));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testDeleteTag_Success() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag1));
        doNothing().when(tagRepository).delete(tag1);

        ResponseEntity<Void> response = tagController.deleteTag(1L);

        verify(tagRepository, times(1)).findById(1L);
        verify(tagRepository, times(1)).delete(tag1);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testDeleteTag_NotFound() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = tagController.deleteTag(99L);

        verify(tagRepository, times(1)).findById(99L);
        verify(tagRepository, times(0)).delete(any(Tag.class));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
