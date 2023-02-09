package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class Main {
    public static void main(String[] args) {
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

        File inputFile = new File(localDir + "/data/data.tsv");
        try {
            Scanner scanner = new Scanner(inputFile);
            scanner.nextLine();
            int numRecords = 0;
            while (scanner.hasNextLine()) {
                if (numRecords == 5) break;
                String line = scanner.nextLine();
                logger.info(line);
                numRecords++;
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + inputFile.toString());
        }
    }
}
