package bptree;

import java.util.ArrayList;

public class NonLeafNode extends Node {
    private ArrayList<Node> children;

    public NonLeafNode() {
        this.children = new ArrayList<>();
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<Node> children) {
        this.children = children;
        return;
    }
}
