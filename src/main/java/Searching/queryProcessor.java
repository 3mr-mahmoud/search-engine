package Searching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import DB.Mongo;
import ca.rmen.porterstemmer.PorterStemmer;

class Index {
 
    public ArrayList<Document> search(String[] stems,boolean isQoutes,String ifQuotes) {
        Mongo DB = new Mongo();
        ArrayList<Document> ret = new ArrayList<>();
       if(isQoutes)
        {
            for (String word : stems) {
                // Retrieve documents containing statements for each word
                ArrayList<Document> result = DB.getDocumentsContainingWord(word, "Indexer");
                if(result.isEmpty())
                continue;
                Double df=1.0/(double)result.get(0).getInteger("documentCount");
                // Search within the "statements" field of each document for the actual sentence
                // that was sent in the query
                for (Document doc : result) {
                    List<Document> docs = (ArrayList<Document>) doc.get("documents");
                    for (Document doc1 : docs) {
                        List<Document> statements = (ArrayList<Document>) doc1.get("statements");
                        if (statements != null) {
                            boolean containsAllWords = false; // Initialize flag for each document
                            for (Document statement : statements) {
                                String state = statement.getString("state");
                                // Check if all words in the query are present in the statement
                                if (state.contains(ifQuotes)) {
                                    containsAllWords = true;
                                    break; // Exit the inner loop as soon as one statement doesn't contain all words
                                }
                            }
                            if (containsAllWords) {
                                doc1.append("idf-tf",df*doc1.getDouble("tf"));
                                ret.add(doc1); // Add the document to the set if it contains all words
                                
                            }
                        }
                    }
                }
            }

    }else{
        for (String word : stems) {
            ArrayList<Document> result = DB.getDocumentsContainingWord(word,"Indexer");
            if(result.isEmpty())
            continue;
            Double df=1.0/(double)result.get(0).getInteger("documentCount");
            for (Document doc : result) {
                List<Document> docs= (ArrayList<Document>) doc.get("documents");
                for(Document doc1:docs){
                 doc1.append("idf-tf",df*doc1.getDouble("tf"));
                 ret.add(doc1);
                }
        }
    }          
    
    }
    return  ret;
}
}


public class queryProcessor {
    private Index index;
    private String query;
    
    public queryProcessor(String query) {
        this.query=query;
        index=new Index();
    }

    public ArrayList<Document> searchly() {
        boolean isQoutes=isEnclosedInQuotes(query);
        ArrayList<String> operations = new ArrayList<>(Arrays.asList("AND", "OR", "NOT"));
        ArrayList<ArrayList<String>> parsedQuery = parseQuery(query, operations);
        ArrayList<Document> documents = new ArrayList<>();
        if(parsedQuery.get(0).isEmpty()){
            String cleanWord = query.replaceAll("^\"|\"$", "");
            String ss=cleanWord;

            // Print the parsed query

            if(isQoutes){
             System.out.println("isQoutes");
             System.out.println(cleanWord);
            }
            String[] words = preprocess(cleanWord);
            String[] stems = stemWords(words);
            
            if(isQoutes)
            {
                printWords(stems);
                documents=(ArrayList<Document>) searchIndex(stems,isQoutes, ss);
            }else{
                printWords(stems);
                documents=(ArrayList<Document>) searchIndex(stems,isQoutes," ");
            }
        }
        else{
            System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiimmmmmmmmm");
            String[] words;
            String[] stems ;
            System.out.println("Operations: " + parsedQuery.get(0));
             System.out.println("Keywords: " + parsedQuery.get(1));
            ArrayList<String> cleanWords=new ArrayList<>();
            int i=0;
            operations=parsedQuery.get(0);
            ArrayList<String> Ops=new ArrayList<>();
            for(String ww:parsedQuery.get(1)){
                cleanWords.add(ww.replaceAll("^\"|\"$", ""));
                System.out.println("isQoutes");
                System.out.println(ww);
                if(operations.size()>i){
                    Ops.add(((String)operations.get(i)).toLowerCase().replaceAll(" ", ""));
                }
                i++;
            }
            System.out.println("Operations: " + Ops.get(0));
            if(Ops.get(0).contains("and")){
                words = preprocess(cleanWords.get(0)+" "+cleanWords.get(1));
                stems = stemWords(words);
                printWords(stems);
                documents=(ArrayList<Document>) searchIndex(stems,isQoutes,cleanWords.get(0)+" "+cleanWords.get(1));
            }else if(Ops.get(0).contains("or")){
                System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiimmmmmmmmm");
                words=preprocess(cleanWords.get(0));
                stems = stemWords(words);
                printWords(stems);
                documents=(ArrayList<Document>) searchIndex(stems,isQoutes,cleanWords.get(0));
                words=preprocess(cleanWords.get(1));
                stems = stemWords(words);
                printWords(stems);
                documents.addAll((ArrayList<Document>) searchIndex(stems,isQoutes,cleanWords.get(1)));
            }else if(Ops.get(0).contains("not")){
                words=preprocess(cleanWords.get(0));
                stems = stemWords(words);
                printWords(stems);
                documents=(ArrayList<Document>) searchIndex(stems,isQoutes,cleanWords.get(0));
            }
        }
        ArrayList<Document>FinalQuery=mergeDocuments(documents);
            addrankFD(FinalQuery);
            return FinalQuery;
    }
   
   
        public static ArrayList<ArrayList<String>> parseQuery(String query, List<String> operations) {
            ArrayList<String> parsedOperations = new ArrayList<>();
            ArrayList<String> statements = new ArrayList<>();
    
            // Use a Set for faster lookup
            Set<String> operationSet = new HashSet<>(operations);
    
            // Variables for handling parentheses
            boolean inQuotes = false;
            StringBuilder currentStatement = new StringBuilder();
    
            // Iterate over the characters of the query
            for (int i = 0; i < query.length(); i++) {
                char c = query.charAt(i);
    
                if (c == '"') {
                    inQuotes = !inQuotes;
                } else if (Character.isWhitespace(c) && !inQuotes) {
                    // Split the query into terms at whitespace outside quotes
                    String term = currentStatement.toString().trim();
                    if (!term.isEmpty()) {
                        if (operationSet.contains(term.toUpperCase())) {
                            parsedOperations.add(term.toUpperCase());
                        } else {
                            statements.add(term);
                        }
                    }
                    currentStatement.setLength(0); // Clear StringBuilder
                } else {
                    // Append the character to the current statement
                    currentStatement.append(c);
                }
            }
    
            // Add the last statement if any
            String lastTerm = currentStatement.toString().trim();
            if (!lastTerm.isEmpty()) {
                statements.add(lastTerm);
            }
    
            ArrayList<ArrayList<String>> parsedQuery = new ArrayList<>();
            parsedQuery.add(parsedOperations);
            parsedQuery.add(statements);
    
            return parsedQuery;
        }
    
    private String[] preprocess(String query) {
        // Tokenize and preprocess the query
        return query.toLowerCase().split("\\s+");
    }
    public ArrayList<Document> mergeDocuments(ArrayList<Document> documents) {
        // Map to store documents grouped by URL
        Map<String, Document> mergedDocsMap = new HashMap<>();

        // Iterate over each document
        for (Document doc : documents) {
            String url = doc.getString("url");
            double idfTf = doc.getDouble("idf-tf");

            // If the URL is already present in the map, merge idf-tf values
            if (mergedDocsMap.containsKey(url)) {
                Document mergedDoc = mergedDocsMap.get(url);
                mergedDoc.put("idf-tf",mergedDoc.getDouble("idf-tf") + idfTf);
            } else {
                // If the URL is not present, add the document to the map
                mergedDocsMap.put(url, doc);
            }
        }

        // Convert map values to ArrayList and return
        return new ArrayList<>(mergedDocsMap.values());
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

    private boolean isEnclosedInQuotes(String s) {
        // Check if the modified string starts and ends with double quotes
        return s.startsWith("\"") && s.endsWith("\"");
    }
    

    private void printWords(String[] words) {
        System.out.println("Words to search for:");
        for (String word : words) {
            System.out.println("- " + word);
        }
    }
    
    private void printDocuments(ArrayList<String> documents) {
        System.out.println("Documents:");
        for (String doc : documents) {
            System.out.println("- " + doc);
        }
    }
    private void addrankFD(ArrayList<Document>documents){
        for(Document doc:documents){
            Double a=0.0;
            if(doc.getBoolean("inHead"))
            a=1.0;
            if(doc.getBoolean("inHead"))
            a+=0.5;
            doc.put("rank", doc.getDouble("rank")+doc.getDouble("idf-tf")+a);
         }
         Collections.sort(documents, Comparator.comparingDouble(doc -> ((Document) doc).getDouble("rank")).reversed());
    }
}


