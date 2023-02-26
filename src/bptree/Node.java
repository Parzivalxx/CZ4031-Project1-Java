package bptree;

import java.util.ArrayList;

public class Node {
    private ArrayList<Integer> elements;
    private NonLeafNode parent;
    private Node prevNode;
    private Node nextNode;

    public Node() {
        this.elements = new ArrayList<>();
        this.parent = null;
        this.prevNode = null;
        this.nextNode = null;
    }

    public ArrayList<Integer> getElements() {
        return elements;
    }

    public NonLeafNode getParent() {
        return parent;
    }

    public Node getPrevNode() {
        return prevNode;
    }

    public void setPrevNode(Node prevNode) {
        this.prevNode = prevNode;
    }

    public Node getNextNode() {
        return nextNode;
    }

    public void setNextNode(Node nextNode) {
        this.nextNode = nextNode;
    }

    public void setElements(ArrayList<Integer> elements) {
        this.elements = elements;
    }

    public void setParent(NonLeafNode parent) {
        this.parent = parent;
    }

    public boolean equals(Node node) {
        if (node instanceof Node) {
            if (elements == node.getElements() && parent == node.getParent())
                return true;
        }
        return false;
    }
}
