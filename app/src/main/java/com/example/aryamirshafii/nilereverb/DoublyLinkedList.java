package com.example.aryamirshafii.nilereverb;

import java.util.NoSuchElementException;
import java.util.Random;

/**
* Doubley linked list that is also circular
*/
public class DoublyLinkedList<T> implements LinkedListInterface<T> {
    // Do not add new instance variables.
    private LinkedListNode<T> head;
    private LinkedListNode<T> tail;
    private LinkedListNode<T> currentNode;
    private int size;

    @Override
    public void addAtIndex(int index,T data) {
        if (data == null) {
            throw new IllegalArgumentException("Error,data is null");
        }
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Error, index is negative" 
                + "or larger than size");
        }



        LinkedListNode<T> tempnode = new LinkedListNode(data);


        if (index == 0) {

            if (head == null) {
                head = tempnode;
                tail = tempnode;
                head.setNext(tail);

                tail.setNext(head);
                this.currentNode = head;
            } else {
                tempnode.setNext(head);
                tempnode.setPrevious(tail);
                head.setPrevious(tempnode);
                head = tempnode;
                tail.setNext(tempnode);
            }






        } else if (index == size) {

            tempnode.setNext(head);
            tempnode.setPrevious(tail);
            tail.setNext(tempnode);
            tail = tempnode;



        } else {

            LinkedListNode<T> current = head;
            for (int i = 0; i < index; i++) {
                current = current.getNext();
            }

            tempnode.setNext(current);
            tempnode.setPrevious(current.getPrevious());
            (current.getPrevious()).setNext(tempnode);
            current.setPrevious(tempnode);
        }
        size++;

    }

    @
            Override
    public void addToFront(T data) {
        if (data == null) {
            throw new IllegalArgumentException("Error,data is null");
        }
        LinkedListNode<T> tempnode = new LinkedListNode(data);

        if (head == null) {
            head = tempnode;
            tail = tempnode;
            this.currentNode = tempnode;
        } else {
            tempnode.setNext(head);
            tempnode.setPrevious(tail);
            head.setPrevious(tempnode);
            head = tempnode;
            tail.setNext(head);
        }
        size++;
    }

    @
    Override
    public void addToBack(T data) {
        if (data == null) {
            throw new IllegalArgumentException("Error,data is null");
        }
        LinkedListNode<T> tempnode = new LinkedListNode(data);
        if (tail == null) {
            head = tempnode;
            tail = tempnode;
            head.setNext(tail);
            tail.setNext(head);
            this.currentNode = tempnode;

        } else {


            tempnode.setNext(head);
            tempnode.setPrevious(tail);
            tail.setNext(tempnode);
            tail = tempnode;
            tail.setNext(head);
            head.setPrevious(tail);



        }
        size++;
    }

    @
    Override
    public T removeAtIndex(int index) {

        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Error, index is negative" 
                + "or larger than size");
        }

        if (index == 0) {
            LinkedListNode<T> current = head;


            if (size == 1) {
                head = null;
                tail = null;
            } else {
                head.getNext().setPrevious(tail);
                head = head.getNext();
                this.currentNode = head;
            }
            size--;
            return current.getData();
        } else if (index == size - 1) {
            LinkedListNode<T> current = tail;
            if (size == 1) {
                head = null;
                tail = null;
            } else {
                tail.getPrevious().setNext(head);
                tail = tail.getPrevious();
            }
            size--;
            return current.getData();


        } else {

            LinkedListNode<T> current = head;
            for (int i = 0; i < index; i++) {
                current = current.getNext();
            }

            current.getNext().setPrevious(current.getPrevious());
            current.getPrevious().setNext(current.getNext());
            size--;
            return current.getData();
        }

    }

    @
    Override
    public T removeFromFront() {
        if (size == 0) {
            return null;
        }
        LinkedListNode<T> current = head;

        if (size == 1) {
            head = null;
            tail = null;
        } else {
            head.getNext().setPrevious(null);
            head = head.getNext();
        }

        size--;
        return current.getData();
    }

    @Override
    public T removeFromBack() {
        if (size == 0) {
            return null;
        }

        LinkedListNode<T> current = tail;


        if (size == 1) {
            head = null;
            tail = null;
        } else {
            tail.getPrevious().setNext(null);
            tail = tail.getPrevious();
        }





        size--;
        return current.getData();
    }



    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Error, index is negative" 
                + "or larger than size");
        }
        LinkedListNode<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.getNext();
        }
        return current.getData();
    }

    @Override
    public Object[] toArray() {
        LinkedListNode<T> current = head;
        Object[] backingArray = (Object[]) new Object[size];
        for (int i = 0; i < size; i++) {
            backingArray[i] = current.getData();
            current = current.getNext();

        }
        return backingArray;
    }

    @Override
    public boolean isEmpty() {

        return size == 0;
    }

    @Override
    public int size() {

        return size;
    }

    @Override
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    @Override
    public LinkedListNode<T> getHead() {
        // DO NOT MODIFY!
        return head;
    }

    @Override
    public LinkedListNode<T> getTail() {
        // DO NOT MODIFY!
        return tail;
    }
    @Override
    public void getNext(){
        currentNode = currentNode.getNext();
    }


    @Override
    public void getPrevious(){
        currentNode = currentNode.getPrevious();
    }


    @Override
    public T getCurrent(){
        if(currentNode == null){
            throw new NoSuchElementException("The current node is null");
        }
        return currentNode.getData();
    }


    @Override
    public void shuffleCurrent(){
        Random countRandom = new Random();
        int randomNumber = countRandom.nextInt(size -1 );

        for(int i = 0; i < randomNumber; i++){
            currentNode = currentNode.getNext();
        }
    }


}