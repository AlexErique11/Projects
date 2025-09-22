package commons;

import commons.Note;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NoteTest {

    @Test
    public void checkConstructor() {
        var n1 = new Note();
        var n2 = new Note();
        assertTrue(n1.equals(n2));
    }

    @Test
    public void equalsHashCode() {
        var a = new Note();
        var b = new Note();
        a.setId(2);
        b.setId(2);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        a.setTitle("TestCase");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void equalsTest(){
        var a = new Note();
        var b = new Note();
        a.setId(2);
        b.setId(2);
        assertEquals(a,b);
        assertEquals(a.hashCode(),b.hashCode());
        a.setTitle("TestCase");
        assertEquals(a,b);
    }

    @Test
    public void notEqualsHashCode() {
        var a = new Note();
        var b = new Note();
        a.setId(2);
        b.setId(1);
        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
        a.setTitle("TestCase");
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void toStringTest() {
        var n = new Note();
        n.setText("123");
        n.setTitle("123");
        assertEquals(n.toString(), "Note:\ntitle: 123\ntext: 123\n");
    }

    @Test
    public void toStringTest2(){
        var n = new Note();
        n.setText("1234");
        n.setTitle("123");
        assertNotEquals(n.toString(), "Note:\ntitle: 123\ntext: 123\n");
    }
}