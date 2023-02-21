package memorypool;

public class RecordBlock {
    private Record record;
    private Block block;

    public RecordBlock(Record record, Block block) {
        this.record = record;
        this.block = block;
    }

    public Record getRecord() {
        return record;
    }

    public Block getBlock() {
        return block;
    }
}
