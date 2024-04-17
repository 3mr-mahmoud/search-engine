package Indexer;

import org.bson.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class StopWordsChecker {
    HashMap<String,Boolean> stopWords = new HashMap<>();

    public StopWordsChecker(){
        try {
            File file = new File(".//stopWords.txt");
            Scanner scanner = new Scanner(file);

            while (scanner.hasNext()) {
                String word = scanner.next();
                stopWords.put(word, true);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public boolean isStopWord(String word) {
        return  stopWords.containsKey(word.toLowerCase());
    }

}
