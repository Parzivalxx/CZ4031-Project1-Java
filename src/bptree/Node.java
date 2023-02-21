package bptree;

import java.util.ArrayList;

public class Node {
    private ArrayList<Integer> elements;
    private NonLeafNode parent;

    public Node() {
        this.elements = new ArrayList<>();
        this.parent = null;
    }

    public ArrayList<Integer> getElements() {
        return elements;
    }

    public NonLeafNode getParent() {
        return parent;
    }

    public void setElements(ArrayList<Integer> elements) {
        this.elements = elements;
    }

    public void setParent(NonLeafNode parent) {
        this.parent = parent;
    }
}
