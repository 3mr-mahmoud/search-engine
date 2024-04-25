package Phrase_Searching;

import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.text.Document;

    public class pcessesquery {
        public static void main(String[] args) {
            // Create a query processor

            Scanner scanner = new Scanner(System.in); // Create a Scanner object
        
           System.out.print("Enter your text search: ");
            String query = scanner.nextLine(); // Read user input as a string
            
            ArrayList<org.bson.Document> results = new ArrayList<>();
           
         //  if(isEnclosedInQuotes(query)){
            queryProcessor processor = new queryProcessor(query);
    
            // Search for the query
            results = processor.searchly();
           
            printDocumentExample(results);
            
            if(results.isEmpty()){
                System.out.println("nooooooooooooooooo");

            }else{
            // Display the results
            System.out.println("Search results for query: \"" + query + "\":");
          
        }
        }
        
            public static void printDocumentExample(ArrayList<org.bson.Document> results) {
                for(org.bson.Document result:results){
                System.out.println("Example Document:");
                System.out.println("URL: " + ((org.bson.Document) result).getString("url"));
                System.out.println("In Head: " + ((org.bson.Document) result).getBoolean("inHead"));
                System.out.println("In Title: " + ((org.bson.Document) result).getBoolean("inTitle"));
              //  System.out.println("Count: " + ((org.bson.Document) result).getInteger("count"));
            }}
            
        
    }