package entity;

/**
 * A simple linked list implementation for storing the path.
 * Used to store the BFS path from Ghost to Pacman.
 */
public class PathLinkedList {
    private Node head;
    private Node tail;
    private int size;

    private class Node {
        char direction;
        Node next;

        Node(char direction) {
            this.direction = direction;
        }
    }

    public PathLinkedList() {
        head = null;
        tail = null;
        size = 0;
    }

    public void addLast(char direction) {
        Node newNode = new Node(direction);
        if (tail == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    public char removeFirst() {
        if (head == null) {
            return ' ';
        }
        char direction = head.direction;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        size--;
        return direction;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }
}
