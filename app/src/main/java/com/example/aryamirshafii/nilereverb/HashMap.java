package com.example.aryamirshafii.nilereverb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;


public class HashMap<K, V> implements HashMapInterface<K, V> {

    // Do not make any new instance variables.
    private MapEntry<K, V>[] table;
    private int size;

    /**
     * Create a hash map with no entries. The backing array has an initial
     * capacity of {@code INITIAL_CAPACITY}.
     *
     * Do not use magic numbers!
     *
     * Use constructor chaining.
     */
    public HashMap() {
        this(INITIAL_CAPACITY);
    }

    /**
     * Create a hash map with no entries. The backing array has an initial
     * capacity of {@code initialCapacity}.
     *
     * You may assume {@code initialCapacity} will always be positive.
     *
     * @param initialCapacity initial capacity of the backing array
     */
    public HashMap(int initialCapacity) {
        this.table = (MapEntry<K, V>[]) new MapEntry[initialCapacity];
        this.size = 0;
    }



    @Override
    public V put(K key, V value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Error,key is null");
        }
        V temppp;
        if (((double) (size + 1) / (double) (table.length)) > MAX_LOAD_FACTOR) {
            resizeBackingTable(table.length * 2 + 1);
        }

        int hashnumber = Math.abs(key.hashCode() % table.length);
        if (table[hashnumber] == null) {
            table[hashnumber] = new MapEntry<K, V>(key, value);
            size++;
            return null;
        } else {
            MapEntry<K, V> check = table[hashnumber];
            do {

                if (check.getKey().equals(key)) {
                    temppp = check.getValue();
                    check.setValue(value);

                    System.out.println("Already exists!");
                    return temppp;
                }
                if (check.getNext() != null) {
                    check = check.getNext();
                }
            } while (check.getNext() != null);
            if (check.getKey().equals(key)) {
                temppp = check.getValue();
                check.setValue(value);
                return temppp;
            }

            MapEntry<K, V> temp = new MapEntry(key, value, table[hashnumber]);
            table[hashnumber] = temp;
            size++;
            return null;
        }


    }





    @Override
    public V remove(K key) {
        MapEntry<K, V> tempone;
        int counter = 0;
        if (key == null) {
            throw new IllegalArgumentException("Error,key is null");
        }

        int hashnumber = Math.abs(key.hashCode() % table.length);
        if (table[hashnumber] == null) {
            throw new NoSuchElementException("error,key does not exist");
        } else {
            MapEntry<K, V> check = table[hashnumber];
            do {
                if (check.getKey().equals(key)) {
                    if (counter == 0) {
                        if (check.getNext() == null) {

                            table[hashnumber] = null;
                        } else {
                            table[hashnumber] = check.getNext();
                        }
                    } else {
                        tempone = table[hashnumber];
                        for (int i = 0; i < counter - 1; i++) {
                            tempone = tempone.getNext();
                        }
                        if (check.getNext() == null) {
                            tempone.setNext(null);
                        } else {
                            tempone.setNext(check.getNext());
                        }

                    }

                    size--;
                    return check.getValue();
                }
                if (check.getNext() != null) {
                    check = check.getNext();
                }
                counter++;
            } while (check.getNext() != null);
            if (check.getKey().equals(key)) {
                if (counter == 0) {
                    if (check.getNext() == null) {

                        table[hashnumber] = null;
                    } else {
                        table[hashnumber] = check.getNext();
                    }
                } else {
                    tempone = table[hashnumber];
                    for (int i = 0; i < counter - 1; i++) {
                        tempone = tempone.getNext();
                    }
                    if (check.getNext() == null) {
                        tempone.setNext(null);
                    } else {
                        tempone.setNext(check.getNext());
                    }

                }

                size--;
                return check.getValue();
            }

            throw new NoSuchElementException("error,key not exist");
        }




    }




    @Override
    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("The key cannot be null!");

        }
        int currentHashNumber = Math.abs(key.hashCode() % table.length);

        if (table[currentHashNumber] != null) {
            MapEntry<K, V> currentItem = table[currentHashNumber];
            while (currentItem != null) {
                if (currentItem.getKey().equals(key)) {
                    return currentItem.getValue();
                } else {
                    currentItem = currentItem.getNext();
                }
            }
        } else {
            throw new NoSuchElementException("The item at key: "
                    + key + " does not exist");
        }

        return null;
    }



    @Override
    public boolean containsKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("The key cannot be null!");

        }
        int currentHashNumber = Math.abs(key.hashCode() % table.length);
        if (table[currentHashNumber] == null) {
            return false;
        }

        if (table[currentHashNumber] != null) {
            MapEntry<K, V> currentItem = table[currentHashNumber];
            if (currentItem.getKey().equals(key)) {
                return true;
            } else {
                while (currentItem.getNext() != null) {
                    if (currentItem.getNext().getKey() != null
                            && currentItem.getNext().getKey().equals(key)) {
                        return true;
                    }

                    currentItem = currentItem.getNext();

                }
            }

        }
        return false;
    }

    @Override
    public void clear() {
        this.table = (MapEntry<K, V>[]) new MapEntry[INITIAL_CAPACITY];
        this.size = 0;
    }

    @Override
    public int size() {
        // DO NOT MODIFY THIS METHOD!
        return size;
    }

    @Override
    public Set<K> keySet() {
        HashSet<K> listToReturn = new HashSet<K>(size);
        for (int i = 0; i < table.length; i++) {
            if (table[i] != null) {
                MapEntry<K, V> tempMapEntry = table[i];
                listToReturn.add(tempMapEntry.getKey());
                while (tempMapEntry.getNext() != null) {
                    tempMapEntry = tempMapEntry.getNext();
                    listToReturn.add(tempMapEntry.getKey());
                }

            }
        }

        return listToReturn;
    }

    @Override
    public List<V> values() {
        ArrayList<V> listToReturn = new ArrayList<V>();
        for (int i = 0; i < table.length; i++) {
            if (table[i] != null) {
                MapEntry<K, V> tempMapEntry = table[i];
                listToReturn.add(tempMapEntry.getValue());
                while (tempMapEntry.getNext() != null) {
                    tempMapEntry = tempMapEntry.getNext();
                    listToReturn.add(tempMapEntry.getValue());
                }

            }
        }

        return listToReturn;
    }

    @
            Override
    public void resizeBackingTable(int length) {

        int tempsize = size;
        if (length <= 0 || length < size) {
            throw new IllegalArgumentException("error L<0 || L<size ");
        }
        MapEntry<K, V> temp;
        size = 0;
        MapEntry<K, V>[] temptable = table;
        table = (MapEntry<K, V>[]) new MapEntry[length];
        for (MapEntry<K, V> item: temptable) {
            size = 0;
            if (item != null) {
                put(item.getKey(), item.getValue());
                temp = item;
                while (temp.getNext() != null) {
                    put(temp.getKey(), temp.getValue());
                    temp = temp.getNext();
                }
                put(temp.getKey(), temp.getValue());
            }
        }
        size = tempsize;
    }




    @Override
    public MapEntry<K, V>[] getTable() {
        // DO NOT EDIT THIS METHOD!
        return table;
    }


    public  MapEntry<K, V> arrayFromKey(String key){
        if (key == null) {
            throw new IllegalArgumentException("The key cannot be null!");

        }

        int currentHashNumber = Math.abs(key.hashCode() % table.length);
        if (table[currentHashNumber] == null) {
            System.out.println("The item at key is null");
            throw new NoSuchElementException("The key does not have any elements associated ");
        }




        MapEntry<K, V> toReturn = table[currentHashNumber];

       return toReturn;

    }





}
