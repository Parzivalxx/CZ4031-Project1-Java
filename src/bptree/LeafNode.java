package bptree;

import java.util.ArrayList;
import memorypool.RecordBlock;

public class LeafNode extends Node {
    private ArrayList<ArrayList<RecordBlock>> recordBlocks;
    private Node prevNode;
    private Node nextNode;

    public LeafNode() {
        this.recordBlocks = new ArrayList<>();
        this.prevNode = null;
        this.nextNode = null;
    }

    public ArrayList<ArrayList<RecordBlock>> getRecordBlocks() {
        return recordBlocks;
    }

    public void setRecordBlocks(ArrayList<ArrayList<RecordBlock>> recordBlocks) {
        this.recordBlocks = recordBlocks;
        return;
    }

    public Node getPrevNode() {
        return prevNode;
    }

    public void setPrevNode(Node prevNode) {
        this.prevNode = prevNode;
        return;
    }

    public Node getNextNode() {
        return nextNode;
    }

    public void setNextNode(Node nextNode) {
        this.nextNode = nextNode;
    }
}
