package memorypool;

import java.util.ArrayList;

public class Block {
    private ArrayList<Record> records;

    public Block() {
        this.records = new ArrayList<>();
    }

    public ArrayList<Record> getRecords() {
        return records;
    }
}
