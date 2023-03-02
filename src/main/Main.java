package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import memorypool.Record;
import memorypool.MemoryPool;
import memorypool.RecordBlock;
import bptree.BPTree;

public class Main {

    public static Logger logger = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) {
        // we assume node size is same as block size
        final int BLOCKSIZE = 200;
        FileHandler fh;
        String localDir = System.getProperty("user.dir");
        try {
            // This block configure the logger with handler and formatter
            fh = new FileHandler(localDir + "\\output\\output.log");

            logger.addHandler(fh);
            System.setProperty("java.util.logging.SimpleFormatter.format",
                    "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            // the following statement is used to log any messages
            logger.info("Starting application");

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MemoryPool db = new MemoryPool(500000000, BLOCKSIZE);
        BPTree tree = new BPTree(25);

        File inputFile = new File(localDir + "/data/data.tsv");
//        File inputFile = new File(localDir + "/data/data_test.tsv");
        try {
            Scanner sc = new Scanner(inputFile);
            sc.nextLine();
            int numRecords = 0;


            while(sc.hasNextLine()) {
                numRecords++;
                if (numRecords % 200000 == 0) {
                    logger.info("Read " + numRecords + " lines...");
                }
                String newLine = sc.nextLine();
                String[] record = newLine.split("\t");

                Record rec = new Record(record[0], Float.parseFloat(String.valueOf(record[1])), Integer.parseInt(String.valueOf(record[2])));
                db.writeRecord(rec);
                RecordBlock rb = new RecordBlock(rec, db.getBlock());
                int key = rec.getNumVotes();
                tree.insertKey(key, rb);
            }
            sc.close();
            boolean exit = false;
            while (!exit) {
                System.out.println("\nRun experiment:");
                System.out.println("1: Experiment 1");
                System.out.println("2: Experiment 2");
                System.out.println("3: Experiment 3");
                System.out.println("4: Experiment 4");
                System.out.println("5: Experiment 5");
                System.out.println("6: Print tree contents");
                System.out.println("7: Quit");

                Scanner sc2 = new Scanner(System.in);
                int choice = sc2.nextInt();
                switch (choice) {
                    case 1:
                        logger.info("Starting experiment 1...");
                        db.printStats();
                        break;
                    case 2:
                        logger.info("Starting experiment 2...");
                        printExperiment2(tree);
                        break;
                    case 3:
                        logger.info("Starting experiment 3...");
                        printRetrievalExperiment(tree, db, 500, 500);
                        break;
                    case 4:
                        logger.info("Starting experiment 4...");
                        printRetrievalExperiment(tree, db, 30000, 40000);
                        break;
                    case 5:
                        logger.info("Starting experiment 5...");
                        logger.info("Enter key to delete: ");
                        int numToDelete = sc2.nextInt();
                        logger.info("Deleting key: " + numToDelete);
                        printExperiment5(tree, db, numToDelete);
                        break;
                    case 6:
                        logger.info("Printing tree contents...");
                        tree.printTree();
                        break;
                    case 7:
                        logger.info("Quitting...");
                        exit = true;
                        sc2.close();
                        break;
                    default:
                        logger.warning("Invalid input, please try again");
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + inputFile.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            for (Handler h : logger.getHandlers())
                h.close();
        }
    }

    public static void printExperiment2(BPTree tree) {
        logger.info("Capacity n: " + tree.getCapacity());
        logger.info("Number of nodes: " + tree.getNumNodes());
        logger.info("Number of levels: " + tree.getNumLevels());
        logger.info(tree.getRootContent());
        return;
    }

    public static void printRetrievalExperiment(BPTree tree, MemoryPool db, int minKey, int maxKey) {
        long start1 = System.nanoTime();
        ArrayList<RecordBlock> accessedRecords = tree.searchNodes(minKey, maxKey);
        long end1 = System.nanoTime();
        logger.info("Number of index nodes accessed (bptree): " + tree.getNumNodesAccessed());
        logger.info("Number of data blocks accessed (bptree): " + accessedRecords.size());
        logger.info("Average of average ratings (bptree): " + String.format("%.5f", tree.getAvgOfAvgRatings(accessedRecords)));
        logger.info("Time taken (bptree): " + (end1 - start1));
        long start2 = System.nanoTime();
        ArrayList<Record> records = db.searchBlocks(minKey, maxKey);
        long end2 = System.nanoTime();
        logger.info("Number of data blocks accessed (brute force): " + db.getNumBlocksAccessed());
        logger.info("Average of average ratings (brute force): " + String.format("%.5f", db.getAvgOfAvgRatings(records)));
        logger.info("Time taken (brute force): " + (end2 - start2));
    }

    public static void printExperiment5(BPTree tree, MemoryPool db, int key) {
        long start1 = System.nanoTime();
        tree.findAndDeleteKey(key);
        long end1 = System.nanoTime();
        logger.info("Number of nodes (bptree): " + tree.getNumNodes());
        logger.info("Number of levels (bptree): " + tree.getNumLevels());
        logger.info("Content of root node (bptree): " + tree.getRootContent());
        logger.info("Time taken (bptree): " + (end1 - start1));
        long start2 = System.nanoTime();
        db.deleteKey(key);
        long end2 = System.nanoTime();
        logger.info("Number of data blocks accessed (brute force): " + db.getNumBlocksAccessed());
        logger.info("Time taken (brute force): " + (end2 - start2));
    }
}
