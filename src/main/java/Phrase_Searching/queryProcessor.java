package Phrase_Searching;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import DB.Mongo;
import ca.rmen.porterstemmer.PorterStemmer;

class Index {
 
    public ArrayList<Document> search(String[] stems,boolean isQoutes,String ifQuotes) {
        Mongo DB = new Mongo();
       
       if(isQoutes)
        {
            Set<Document> uniqueDocuments = new HashSet<>();

            for (String word : stems) {
                // Retrieve documents containing statements for each word
                ArrayList<Document> result = DB.getDocumentsContainingWord(word, "Indexer");
    
                // Search within the "statements" field of each document for the actual sentence
                // that was sent in the query
                for (Document doc : result) {
                    List<Document> docs = (ArrayList<Document>) doc.get("documents");
                    for (Document doc1 : docs) {
                        List<Document> statements = (ArrayList<Document>) doc1.get("statements");
                        if (statements != null) {
                            boolean containsAllWords = true; // Initialize flag for each document
                            for (Document statement : statements) {
                                String state = statement.getString("state");
                                // Check if all words in the query are present in the statement
                                if (!state.contains(ifQuotes)) {
                                    containsAllWords = false;
                                    break; // Exit the inner loop as soon as one statement doesn't contain all words
                                }
                            }
                            if (containsAllWords) {
                                uniqueDocuments.add(doc1); // Add the document to the set if it contains all words
                            }
                        }
                    }
                }
            }

            // Convert the set of unique documents to an ArrayList if needed
            List<Document> ret = new ArrayList<>(uniqueDocuments);
            return (ArrayList<Document>) ret;
    }else{
        ArrayList<Document> ret = new ArrayList<>();
        for (String word : stems) {
            // Retrieve documents containing statements for each word
            ArrayList<Document> result = DB.getDocumentsContainingWord(word,"Indexer");

            // Search within the "statements" field of each document for the actual sentence
            // that was sent in the query
            for (Document doc : result) {
                List<Document> docs= (ArrayList<Document>) doc.get("documents");
                for(Document doc1:docs)
                 ret.add(doc1);
        }
    }          
    return ret;
    }
}
}

public class queryProcessor {
    private Index index=new Index();
    private String query;

    public queryProcessor(String query) {
        this.query=query;
    }

    public ArrayList<Document> searchly() {

        
        boolean isQoutes=isEnclosedInQuotes(query);
          String cleanWord = query.replaceAll("^\"|\"$", "");
          if(isQoutes)
          System.out.println("isQoutes");
          String[] words = preprocess(cleanWord);
          String[] stems = stemWords(words);
          ArrayList<Document> documents;
        if(isQoutes)
        {
            printWords(stems);
            documents=(ArrayList<Document>) searchIndex(stems,isQoutes,cleanWord);
      
        }else{
                printWords(stems);
               documents=(ArrayList<Document>) searchIndex(stems,isQoutes," ");
        }
        return documents;
    }



    private String[] preprocess(String query) {
        // Tokenize and preprocess the query
        return query.toLowerCase().split("\\s+");
    }
   
    private String[] stemWords(String[] words) {
        List<String> stemmedWordsList = new ArrayList<>();
        PorterStemmer stemmer = new PorterStemmer();

        for (String word : words) {
           
            // Stem the word
            String stemmedWord =  stemmer.stemWord(word);
            if (stemmedWord.isEmpty())
            continue;
            // Add the stemmed word to the list
            stemmedWordsList.add(stemmedWord);
        }
        // Convert the list to an array
        String[] stemmedWordsArray = new String[stemmedWordsList.size()];
        stemmedWordsArray = stemmedWordsList.toArray(stemmedWordsArray);

        return stemmedWordsArray;
    }

    private List<Document> searchIndex(String[] stems,boolean isQoutes,String ifqoutes) {
        // Search the index for documents containing words with the same stems
        return index.search(stems,isQoutes,ifqoutes);
    }

    public boolean isEnclosedInQuotes(String s) {
        // Check if the modified string starts and ends with double quotes
        return s.startsWith("\"") && s.endsWith("\"");
    }
    

    private void printWords(String[] words) {
        System.out.println("Words to search for:");
        for (String word : words) {
            System.out.println("- " + word);
        }
    }
    
    public void printDocuments(ArrayList<String> documents) {
        System.out.println("Documents:");
        for (String doc : documents) {
            System.out.println("- " + doc);
        }
    }

    
}


