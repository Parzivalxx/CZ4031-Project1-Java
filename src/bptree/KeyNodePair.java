package bptree;

public class KeyNodePair {
    private int key;
    private Node node;

    public KeyNodePair(int key, Node node) {
        this.key = key;
        this.node = node;
    }

    public int getKey() {
        return key;
    }

    public Node getNode() {
        return node;
    }
}
