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
    public int recordsPerBlk;
    // Total number of records in the DB
    public int totalNumRecords = 0;

    // A list containing all the allocated blocks
    private ArrayList<Block> blkList;
    // Current block that is being filled. Once this block is filled, it will pushed into the blkLis and will be reset
    private Block blk;

    static Logger logger = Logger.getLogger(Main .class.getName());

    /**
     * initializes storing of database
     * @param poolSize
     * @param blkSize
     */
    public MemoryPool(int poolSize, int blkSize) {
        this.poolSize = poolSize;
        this.blkSize = blkSize;
        this.numAllocatedBlk = 0;
        this.numRemainingBlk = (int) Math.floor(poolSize/blkSize);
        this.recordSize = 4 + 4 + 9;
        this.recordsPerBlk = (int) Math.floor(blkSize/(4 + 4 + 9));
        this.blk = new Block();
        this.blkList = new ArrayList<Block>();
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

    public boolean deleteRecord(Record rec) {
        if (totalNumRecords <= 0) {
            System.out.println("DATABASE IS EMPTY");
            return false;
        }

        totalNumRecords--;
        deletedRecords++;

        if (deletedRecords == recordsPerBlk) {
            numAllocatedBlk--;
            numRemainingBlk++;
            deletedRecords = 0;
        }
        return true;
    }

    public void printStats() {
        int currBlkSize = blk.getRecords().size();
        if (currBlkSize > 0) {
            totalNumRecords += currBlkSize;
            numAllocatedBlk++;
            numRemainingBlk--;
        }
        logger.info("Number of records: " + totalNumRecords);
        logger.info("Size of a record: " + recordSize);
        logger.info("Number of records per block: " + recordsPerBlk);
        logger.info("Number of blocks: " + numAllocatedBlk);
    }
}
