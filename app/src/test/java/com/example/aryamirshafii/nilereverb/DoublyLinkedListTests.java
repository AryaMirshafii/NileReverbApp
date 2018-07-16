import com.example.aryamirshafii.nilereverb.DoublyLinkedList;
import com.example.aryamirshafii.nilereverb.LinkedListInterface;
import com.example.aryamirshafii.nilereverb.LinkedListNode;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class DoublyLinkedListTests {
    private LinkedListInterface<String> list;
    private String[] ideal;

    private static final int TIMEOUT = 200;

    @Before
    public void setUp() {

        list = new DoublyLinkedList<>();
    }

    @Test (timeout = TIMEOUT)
    public void addAtIndex() {
        ideal = new String[]{"om", "za"};
        assertNull(list.getHead());
        list.addAtIndex(0, "za");
        assertEquals(list.getHead(), list.getHead().getNext());
        list.addAtIndex(0, "om");
        assertArrayEquals(ideal, list.toArray());
        ideal = new String[]{"om", "za", "bam"};
        list.addAtIndex(2, "bam");
        assertArrayEquals(ideal, list.toArray());
    }

    @Test (timeout = TIMEOUT)
    public void addAtIndex2() {
        list.addAtIndex(0, "a");
        list.addAtIndex(1, "b");
        list.addAtIndex(2, "c");
        list.addAtIndex(3, "d");
        list.addAtIndex(4, "e");
        list.addAtIndex(2, "f");
        list.addAtIndex(0, "g");
        list.addAtIndex(7, "h");
        list.addAtIndex(4, "i");
        ideal = new String[]{"g", "a", "b", "f", "i", "c", "d", "e", "h"};
        for (int i = 0; i < 9; i++) {
            assertEquals(list.get(i), ideal[i]);
        }
        assertArrayEquals(ideal, list.toArray());
        assertEquals(9, list.size());
    }

    @Test (timeout = TIMEOUT)
    public void addAtIndexLarger() {
        ideal = new String[20];
        for (int i = 19; i >= 0; i--) {
            list.addAtIndex(19 - i, "" + i);
            ideal[19 - i] = "" + i;
        }
        assertEquals(20, list.size());
        assertArrayEquals(ideal, list.toArray());
    }

    @Test (timeout = TIMEOUT, expected = IllegalArgumentException.class)
    public void addAtIndexNull() {
        list.addAtIndex(0, "a");
        list.addAtIndex(1, "b");
        list.addAtIndex(0, null);
    }

    @Test (timeout = TIMEOUT, expected = IndexOutOfBoundsException.class)
    public void addAtIndexLow() {
        list.addAtIndex(0, "a");
        list.addAtIndex(1, "b");
        list.addAtIndex(-1 , "c");
    }

    @Test (timeout = TIMEOUT, expected = IndexOutOfBoundsException.class)
    public void addAtIndexHigh() {
        list.addAtIndex(0, "a");
        list.addAtIndex(1, "b");
        list.addAtIndex(3, "c");
    }

    @Test (timeout = TIMEOUT)
    public void addToFront() {
        assertArrayEquals(new String[0], list.toArray());
        list.addToFront("Did");
        list.addToFront("you");
        list.addToFront("ever");
        list.addToFront("hear");
        list.addToFront("the");
        list.addToFront("tragedy");
        list.addToFront("of");
        list.addToFront("Darth");
        list.addToFront("Plagueis");
        list.addToFront("the");
        list.addToFront("Wise?");
        assertArrayEquals(new String[]{"Wise?", "the", "Plagueis", "Darth",
                        "of", "tragedy", "the", "hear", "ever", "you", "Did"},
                list.toArray());
    }

    @Test (timeout = TIMEOUT, expected = IllegalArgumentException.class)
    public void addToFrontNull() {
        list.addToFront("a");
        list.addToFront("b");
        list.addToFront(null);
    }

    @Test (timeout = TIMEOUT)
    public void addToBack() {
        assertArrayEquals(new String[0], list.toArray());
        list.addToBack("I");
        list.addToBack("don't");
        list.addToBack("like");
        list.addToBack("sand.");
        assertArrayEquals(new String[]{"I", "don't", "like", "sand."}, list
                .toArray());
    }

    @Test (timeout = TIMEOUT, expected = IllegalArgumentException.class)
    public void addToBackNull() {
        list.addToBack("a");
        list.addToBack("b");
        list.addToBack(null);
    }

    @Test (timeout = TIMEOUT)
    public void removeAtIndex() {
        assertArrayEquals(new String[0], list.toArray());
        list.addToFront("Did");
        list.addToFront("you");
        list.addToFront("ever");
        list.addToFront("hear");
        list.addToFront("the");
        list.addToFront("tragedy");
        list.addToFront("of");
        list.addToFront("Darth");
        list.addToFront("Plagueis");
        list.addToFront("the");
        list.addToFront("Wise?");

        assertEquals("tragedy", list.removeAtIndex(5));
        assertArrayEquals(new String[]{"Wise?", "the", "Plagueis", "Darth",
                "of", "the", "hear", "ever", "you", "Did"}, list.toArray());
        assertEquals("Wise?", list.removeAtIndex(0));
        assertArrayEquals(new String[]{"the", "Plagueis", "Darth",
                "of", "the", "hear", "ever", "you", "Did"}, list.toArray());
        assertEquals("of", list.removeAtIndex(3));
        assertArrayEquals(new String[]{"the", "Plagueis", "Darth",
                "the", "hear", "ever", "you", "Did"}, list.toArray());
        assertEquals("Did", list.removeAtIndex(7));
        assertArrayEquals(new String[]{"the", "Plagueis", "Darth",
                "the", "hear", "ever", "you"}, list.toArray());
        assertEquals("the", list.removeAtIndex(3));
        assertArrayEquals(new String[]{"the", "Plagueis", "Darth",
                "hear", "ever", "you"}, list.toArray());
    }

    @Test (timeout = TIMEOUT, expected = IndexOutOfBoundsException.class)
    public void removeAtIndexLow() {
        list.addAtIndex(0, "a");
        list.addAtIndex(1, "b");
        list.addAtIndex(-1, "c");
    }

    @Test (timeout = TIMEOUT, expected = IndexOutOfBoundsException.class)
    public void removeAtIndexHigh() {
        list.addAtIndex(0, "a");
        list.addAtIndex(1, "b");
        list.removeAtIndex(2);
    }

    @Test (timeout = TIMEOUT)
    public void removeFromFront() {
        ideal = new String[30];
        for (int i = 0; i < 45; i++) {
            list.addToFront("" + i);
        }
        for (int i = 0; i < 30; i++) {
            ideal[29 - i] = "" + i;
        }
        for (int i = 44; i >= 30; i--) {
            assertEquals("" + i, list.removeFromFront());
            assertEquals(i, list.size());
        }
        assertArrayEquals(ideal, list.toArray());
    }

    @Test (timeout = TIMEOUT)
    public void removeFromFrontNull() {
        list.addToBack("a");
        assertEquals("a", list.removeFromFront());
        assertNull(list.removeFromFront());
    }

    @Test (timeout = TIMEOUT)
    public void removeFromBack() {
        list.addAtIndex(0, "Take");
        list.addAtIndex(0, "a");
        list.addAtIndex(0, "seat");
        assertEquals(3, list.size());
        assertEquals("Take", list.removeFromBack());
        assertEquals(2, list.size());
        assertEquals("a", list.removeFromBack());
        assertEquals("seat", list.removeFromBack());
        assertEquals(0, list.size());
    }

    @Test (timeout = TIMEOUT)
    public void removeFromBackNull() {
        list.addToFront("a");
        assertEquals("a", list.removeFromBack());
        assertNull(list.removeFromBack());
    }





    @Test (timeout = TIMEOUT)
    public void get() {
        list.addToBack("It's");
        list.addToBack("coarse");
        list.addToBack("and");
        list.addToBack("rough");
        list.addToBack("and");
        String sand = "irritating.";
        list.addToBack(sand);
        assertEquals("It's", list.get(0));
        assertSame(sand, list.get(5));
        list.removeFromFront();
        list.removeFromFront();
        assertEquals("and", list.get(0));
    }

    @Test (timeout = TIMEOUT)
    public void getCircular() {
        list.addToBack("It's");
        list.addToBack("coarse");
        list.addToBack("and");
        list.addToBack("rough");
        list.addToBack("and");

        //testing circular linking
        LinkedListNode<String> travNode = list.getHead();
        for (int i = 0; i < 4; i++) {
            assertSame(travNode.getNext().getData(), list.get(i + 1));
            travNode = travNode.getNext();
        }
        assertSame(list.getHead(), travNode.getNext());
    }

    @Test (timeout = TIMEOUT)
    public void testDoubley(){
        list.clear();
        list.addToFront("It's");
        list.addToFront("coarse");
        list.addToFront("and");
        list.addToFront("rough");
        list.addToFront("and");
        LinkedListNode<String> currentNode = list.getHead();
        for (int i = 0; i < 2; i++) {

            currentNode = currentNode.getNext();
            System.out.println("The current node is" + currentNode.getData());
        }

        assertEquals(currentNode.getPrevious().getData(), "rough");


    }


    @Test (timeout = TIMEOUT)
    public void testGetNext(){
        list.clear();
        list.addToFront("It's");
        list.addToFront("coarse");
        list.addToFront("andOne");
        list.addToFront("rough");
        list.addToFront("and");
        //Test that the current node is initialized at head
        assertEquals( "and",list.getHead().getData());
        assertEquals("It's", list.getCurrent());
        //Test that the current node can be moved using next
        list.getNext();
        assertEquals("and", list.getCurrent());

        //Keep testing the enxt function

        list.getNext();
        assertEquals("rough", list.getCurrent());

        list.getNext();
        assertEquals("andOne", list.getCurrent());


        list.getNext();
        assertEquals("coarse", list.getCurrent());

        list.getNext();
        assertEquals("It's", list.getCurrent());


        list.getNext();
        assertEquals("and", list.getCurrent());

    }






    @Test (timeout = TIMEOUT)
    public void testGetPrevious(){
        list.clear();
        list.addToFront("It's");
        list.addToFront("coarse");
        list.addToFront("andOne");
        list.addToFront("rough");
        list.addToFront("and");
        //Test that the current node is initialized at head
        assertEquals( "and",list.getHead().getData());
        assertEquals("It's", list.getCurrent());
        //Test that the current node can be moved using next
        list.getPrevious();
        assertEquals("coarse", list.getCurrent());

        //Keep testing the previous function

        list.getPrevious();
        assertEquals("andOne", list.getCurrent());


        list.getPrevious();
        assertEquals("rough", list.getCurrent());


        list.getPrevious();
        assertEquals("and", list.getCurrent());

        list.getPrevious();
        assertEquals("It's", list.getCurrent());


        list.getPrevious();
        assertEquals("coarse", list.getCurrent());

    }

    @Test (timeout = TIMEOUT, expected = IndexOutOfBoundsException.class)
    public void getAtIndexLow() {
        list.addAtIndex(0, "a");
        list.addAtIndex(1, "b");
        list.get(-1);
    }

    @Test (timeout = TIMEOUT, expected = IndexOutOfBoundsException.class)
    public void getAtIndexHigh() {
        list.addAtIndex(0, "a");
        list.addAtIndex(1, "b");
        list.get(2);
    }

    @Test (timeout = TIMEOUT)
    public void toArray() {
        assertArrayEquals(new String[0], list.toArray());
        list.addAtIndex(0, "Yo");
        assertArrayEquals(new String[]{"Yo"}, list.toArray());
        list.removeFromBack();
        assertArrayEquals(new String[0], list.toArray());
    }

    @Test (timeout = TIMEOUT)
    public void isEmpty() {
        assertTrue(list.isEmpty());
        list.addAtIndex(0, "");
        assertFalse(list.isEmpty());
    }

    @Test (timeout = TIMEOUT)
    public void clear() {
        list.addToFront("4");
        list.addToFront("8");
        list.addToFront("15");
        list.clear();
        assertNull(list.getHead());
        assertEquals(0, list.size());
    }

    @Test (timeout = TIMEOUT)
    public void size() {
        assertEquals(0, list.size());
        list.addToFront("asdf");
        assertEquals(1, list.size());
        list.addToBack("fdas");
        assertEquals(2, list.size());
        list.removeFromFront();
        assertEquals(1, list.size());
        list.removeFromBack();
        assertEquals(0, list.size());
    }

    @Test (timeout = TIMEOUT)
    public void getHead() {
        assertNull(list.getHead());
        list.addAtIndex(0, "boop");
        assertNotNull(list.getHead());
    }

    @Test (timeout = TIMEOUT)
    public void testShuffle() {
        list.clear();
        list.addToFront("It's");
        list.addToFront("coarse");
        list.addToFront("andOne");
        list.addToFront("rough");
        list.addToFront("and");

        list.shuffleCurrent();
        assertNotNull(list.getCurrent());
    }

    @Test (timeout = TIMEOUT)
    public void testAddToBack(){
        list.clear();
        list.addToBack("It's");
        list.addToBack("coarse");
        list.addToBack("andOne");
        list.addToBack("rough");
        list.addToBack("and");
        assertEquals(list.getHead().getData(), list.getCurrent());

        list.getPrevious();
        assertEquals("and", list.getCurrent());
    }


    @Test (timeout = TIMEOUT)
    public void testOneItem(){
        list.clear();
        list.addToBack("arya");
        list.getNext();
        assertEquals("arya", list.getCurrent());

    }
}