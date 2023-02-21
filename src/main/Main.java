package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import memorypool.Record;
import memorypool.MemoryPool;
import memorypool.RecordBlock;
import bptree.BPTree;

public class Main {
    public static void main(String[] args) {
        // we assume node size is same as block size
        final int BLOCKSIZE = 200;
        Logger logger = Logger.getLogger(Main.class.getName());
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
        BPTree tree = new BPTree(200 / 8);

        File inputFile = new File(localDir + "/data/data.tsv");
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
                System.out.println("6: Quit");

                Scanner sc2 = new Scanner(System.in);
                int choice = sc2.nextInt();
                switch (choice) {
                    case 1:
                        logger.info("\nStarting experiment 1...");
                        db.printStats();
                        break;
                    case 2:
                        logger.info("Starting experiment 2...");
                        tree.printExperiment2();
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
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


    }
}
