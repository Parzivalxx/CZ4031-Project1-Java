package bptree;

import java.util.ArrayList;
import memorypool.RecordBlock;

public class LeafNode extends Node {
    private ArrayList<ArrayList<RecordBlock>> recordBlocks;

    public LeafNode() {
        this.recordBlocks = new ArrayList<>();
    }

    public ArrayList<ArrayList<RecordBlock>> getRecordBlocks() {
        return recordBlocks;
    }

    public void setRecordBlocks(ArrayList<ArrayList<RecordBlock>> recordBlocks) {
        this.recordBlocks = recordBlocks;
        return;
    }
}
