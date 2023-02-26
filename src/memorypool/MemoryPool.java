package memorypool;

import main.Main;

import java.util.ArrayList;
import java.util.logging.Logger;

public class MemoryPool {
    // Size of Memory
    private int poolSize;
    // Size of Block
    private int blkSize;
    // Number of blocks allocated
    private int numAllocatedBlk;
    // Number of blocks remaining
    private int numRemainingBlk;
    // A counter to check if a block is full
    private int recordCounter = 0;
    // Size of a record in bytes
    private int recordSize;

    // A counter to check if a block worth of records has been deleted
    private int deletedRecords = 0;

    // Number of records per block
    private int recordsPerBlk;
    // Total number of records in the DB
    private int totalNumRecords;

    // A list containing all the allocated blocks
    private ArrayList<Block> blkList;
    // Current block that is being filled. Once this block is filled, it will pushed into the blkLis and will be reset
    private Block blk;
    private int numBlocksAccessed;

    static Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * initializes storing of database
     * @param poolSize
     * @param blkSize
     */
    public MemoryPool(int poolSize, int blkSize) {
        this.poolSize = poolSize;
        this.blkSize = blkSize;
        this.numAllocatedBlk = 1;
        this.numRemainingBlk = (int) Math.floor(poolSize/blkSize);
        this.recordSize = 4 + 4 + 9;
        this.recordsPerBlk = (int) Math.floor(blkSize/(4 + 4 + 9));
        this.blk = new Block();
        this.blkList = new ArrayList<Block>();
        this.totalNumRecords = 0;
        this.numBlocksAccessed = 0;
    }

    public int getNumBlocksAccessed() {
        return numBlocksAccessed;
    }

    /**
     * allocates a block for new record when previous block is full
     * @return boolean denoting whether allocation was successful
     */
    public boolean allocateBlock() {
        if (numRemainingBlk < 0) {
            System.out.println("MEMORY FULL");
            return false;
        }

        blkList.add(blk);
        blk = new Block();
        numAllocatedBlk++;
        numRemainingBlk--;
        return true;
    }

    /**
     *
     * @param rec
     */
    public void writeRecord(Record rec) {
        if (numRemainingBlk < 0) {
            System.out.println("No more space available!");
            return;
        }

        blk.getRecords().add(rec);
        recordCounter++;
        totalNumRecords++;

        if (recordCounter == recordsPerBlk) {
            recordCounter = 0;
            allocateBlock();
        }
    }

    public Block getBlock(){
        return this.blk;
    }

    /**
     * to brute force the database and get records within a range
     * @param minKey, lowest key searching
     * @param maxKey, highest key searching
     * @return, list of records that satisfy range
     */
    public ArrayList<Record> searchBlocks(int minKey, int maxKey) {
        numBlocksAccessed = 0;
        ArrayList<Record> records = new ArrayList<>();
        for (Block b : blkList) {
            numBlocksAccessed += 1;
            for (Record r : b.getRecords()) {
                int numVotes = r.getNumVotes();
                if (numVotes >= minKey && numVotes <= maxKey) {
                    records.add(r);
                }
            }
        }
        return records;
    }

    /**
     * to find average of average ratings for records within range
     * @param records, the records within a range of keys
     * @return, the average of average ratings
     */
    public float getAvgOfAvgRatings(ArrayList<Record> records) {
        if (records.size() == 0) return 0;
        float total = 0;
        for (Record r : records) {
            total += r.getAvgRating();
        }
        return (total / records.size());
    }

    public void deleteKey(int key) {
        numBlocksAccessed = 0;
        for (int i = blkList.size() - 1; i > -1; i--) {
            numBlocksAccessed += 1;
            for (int j = blkList.get(i).getRecords().size() - 1; j > -1; j--) {
                if (blkList.get(i).getRecords().get(j).getNumVotes() == key) {
                    blkList.get(i).getRecords().remove(j);
                }
            }
        }
    }

    /**
     * prints some statistics on the database
     */
    public void printStats() {
        logger.info("Number of records: " + totalNumRecords);
        logger.info("Size of a record: " + recordSize);
        logger.info("Number of records per block: " + recordsPerBlk);
        logger.info("Number of blocks: " + numAllocatedBlk);
    }
}
