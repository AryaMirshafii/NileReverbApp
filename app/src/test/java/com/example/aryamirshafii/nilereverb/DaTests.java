package com.example.aryamirshafii.nilereverb;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Set;


public class DaTests {
    public final static long TIMEOUT = 200L;
    private HashMap<String, Integer> map;

    @Before
    public void setup() {
        map = new HashMap<String, Integer>();
    }

    @Test(timeout = TIMEOUT)
    public void testPut() {

        for (char c = '!'; c <= 126; c++) {
            map.put(String.valueOf(c), Integer.valueOf(c));
        }
        for (char c = '!'; c <= 126; c++) {
            int hash = String.valueOf(c).hashCode() % map.getTable().length;
            assertEquals(String.valueOf(c), map.getTable()[hash].getKey());
        }
    }

    @Test(timeout = TIMEOUT)
    public void testPutChain() {
        HashMap<Integer, Integer> m = new HashMap<>();
        for (int i = 13; i < 13*9; i += 13) {
            m.put(i, i);
        }
        MapEntry<Integer, Integer> e = m.getTable()[0];
        for (int i = 13*8; i > 0; i -= 13) {
            assertEquals(i, (int) e.getValue());
            assertEquals(i, (int) e.getKey());
            e = e.getNext();
        }
    }
    @Test(timeout = TIMEOUT)
    public void testPutResize() {
        for (char c = 'a'; c < 'i'; c++) {
            map.put(String.valueOf(c), Integer.valueOf(c));
        }
        assertEquals('i' - 'a', map.size());
        int current = map.getTable().length;
        map.put(String.valueOf('j'), Integer.valueOf('j'));
        assertEquals(current * 2 + 1, map.getTable().length);
    }

    @Test(timeout = TIMEOUT, expected = IllegalArgumentException.class)
    public void testPutExceptionKey() {
        map.put(null, 0);
    }

    @Test(timeout = TIMEOUT, expected = IllegalArgumentException.class)
    public void testPutExceptionVal() {
        map.put("hello", null);
    }

    @Test(timeout = TIMEOUT)
    public void testRemove() {

        map.put("c", 1337);
        assertEquals(1, map.size());
        assertEquals(1337, (int)map.remove("c"));
        assertEquals(0, map.size());
    }

    @Test(timeout = TIMEOUT)
    public void testRemoveChain() {

        HashMap<Integer, Integer> m = new HashMap<>();

        for (int i = 13; i < 13*9; i += 13) {
            m.put(i, i);
        }

        for (int i = 13*8; i > 0; i -= 13) {
            assertEquals(i, (int) m.remove(i));
            if (i > 13)
                assertEquals(i - 13, (int) m.getTable()[0].getValue());
        }
    }

    @Test(timeout = TIMEOUT, expected = IllegalArgumentException.class)
    public void testRemoveNull() {
        map.remove(null);
    }

    @Test(timeout = TIMEOUT, expected = java.util.NoSuchElementException.class)
    public void testRemoveNotHere() {
        map.put("efqefqe", 30131);
        map.remove("AAAAA");
    }

    @Test(timeout = TIMEOUT)
    public void testGet() {

        map.put("31", 0);
        assertEquals(0, (int) map.get("31"));

    }

    @Test(timeout = TIMEOUT, expected = IllegalArgumentException.class)
    public void testGetNull() {
        map.get(null);
    }

    @Test(timeout = TIMEOUT, expected = java.util.NoSuchElementException.class)
    public void testGetNotHere() {
        map.get("1332");
    }
    @Test(timeout = TIMEOUT)
    public void testContainsKey() {
        for (char c = 1; c < 128; c++) {
            map.put(String.valueOf(c), Integer.valueOf(c));
        }

        for (char c = 127; c >= 1; c--) {
            assertTrue(map.containsKey(String.valueOf(c)));
        }
    }

    @Test(timeout = TIMEOUT, expected = IllegalArgumentException.class)
    public void testContainsKeyException() {
        map.put("hello", 1337);
        map.containsKey(null);
    }

    @Test(timeout = TIMEOUT)
    public void testContainsValueEq() {
        map.put("hello", 1337);
        assertTrue(map.containsKey(new String("hello")));
    }
    
    @Test(timeout = TIMEOUT)
    public void testClear() {
        assertEquals(0, map.size());
        for (char c = 'a'; c < 'z'; c++) {
            map.put(String.valueOf(c), Integer.valueOf(c));
        }
        assertEquals('z' - 'a', map.size());
        map.clear();
        assertEquals(0, map.size());
        assertEquals(HashMapInterface.INITIAL_CAPACITY, map.getTable().length);
    }

    @Test(timeout = TIMEOUT)
    public void testResize() {
        for (char c = 'a'; c < 'e'; c++) {
            map.put(String.valueOf(c), Integer.valueOf(c));
        }
        assertEquals(HashMapInterface.INITIAL_CAPACITY, map.getTable().length);
        map.resizeBackingTable(99);
        assertEquals(99, map.getTable().length);
    }

    @Test(timeout = TIMEOUT, expected = IllegalArgumentException.class)
    public void testResizeExNeg() {
        
        map.resizeBackingTable(-1);
    }

    @Test(timeout = TIMEOUT, expected = IllegalArgumentException.class)
    public void testResizeExSmall() {
    
        for (char c = 'a'; c < 'z'; c++) {
            map.put(String.valueOf(c), Integer.valueOf(c));
        }
        map.resizeBackingTable(13);
    }

    @Test(timeout = TIMEOUT)
    public void testKeySet() {

        HashMap<Integer, Integer> m = new HashMap<>();
        for (int i = 0; i < 999; i++) {
            m.put(i, 0);
        }

        Set<Integer> l = m.keySet();
        for (int i = 0; i < 999; i++) {
            assertTrue(l.contains(i));
        }
    }

    @Test(timeout = TIMEOUT)
    public void testKeySetEmpty() {
        assertEquals(0, map.keySet().size());
    }

    @Test(timeout = TIMEOUT)
    public void testValues() {

        HashMap<Integer, Integer> map = new HashMap<>();
        map.put(0, 0xdeadbeef);
        map.put(13, 0x1337);
        map.put(1, 1);

        java.util.List<Integer> l = map.values();
        assertEquals(0x1337, (int) l.get(0));
        assertEquals(0xdeadbeef, (int) l.get(1));
        assertEquals(1, (int) l.get(2));
    }

    @Test(timeout = TIMEOUT)
    public void testValuesEmpty() {
        assertEquals(0, map.values().size());
    }


    private void printMap(HashMapInterface m) {

        MapEntry<String, Integer>[] table = m.getTable();
        for (int i = 0; i < table.length; i++) {
            MapEntry<String, Integer> current = table[i];
            if (current == null) {
                System.out.println("null");
                continue;
            }
            String line = current.toString();

           while (current.getNext() != null) {
                current = current.getNext();

                line += "  |  ";
                line += current.toString();
            } 
           System.out.println(line);
        }
    }

}