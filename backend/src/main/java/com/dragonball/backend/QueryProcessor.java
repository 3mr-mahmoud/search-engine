package com.dragonball.backend;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.bson.Document;

import ca.rmen.porterstemmer.PorterStemmer;


@Service
public class QueryProcessor {
    private String query;

    @Autowired
    private IndexerRepository repository;

    public ArrayList<IndexerDocument> search(String[] stems, boolean isQoutes, String ifQuotes) {
        ArrayList<IndexerDocument> ret = new ArrayList<>();
        if (isQoutes) {
            for (String word : stems) {
                // Retrieve documents containing statements for each word
                List<IndexerModel> result = repository.findByWord(word);

                if (result.isEmpty())
                    continue;
                Double df = 1.0 / (double) result.get(0).getDocumentCount();
                // Search within the "statements" field of each document for the actual sentence
                // that was sent in the query
                for (IndexerModel doc : result) {
                    List<IndexerDocument> docs = (ArrayList<IndexerDocument>) doc.getDocuments();
                    for (IndexerDocument doc1 : docs) {
                        List<String> statements = (ArrayList<String>) doc1.getStatements();
                        List<String> newStatements = new ArrayList<>();
                        if (statements != null) {
                            boolean containsAllWords = false; // Initialize flag for each document
                            for (String statement : statements) {
                                // Check if all words in the query are present in the statement
                                if (checkPhrase(statement, ifQuotes)) {
                                    statement = highlight(statement, ifQuotes,true);
                                    newStatements.add(statement);
                                    containsAllWords = true;
                                }
                            }
                            Collections.sort(newStatements, Comparator.comparingInt(str -> ((String) str).length()).reversed());
                            doc1.setStatements(newStatements);
                            if (containsAllWords) {
                                doc1.setTfIdf(df * doc1.getTf());
                                ret.add(doc1); // Add the document to the set if it contains all words
                            }
                        }
                    }
                }
            }

        } else {
            for (String word : stems) {
                List<IndexerModel> result = repository.findByWord(word);
                if (result.isEmpty())
                    continue;
                Double df = 1.0 / (double) result.get(0).getDocumentCount();
                for (IndexerModel doc : result) {
                    List<IndexerDocument> docs = (ArrayList<IndexerDocument>) doc.getDocuments();
                    for (IndexerDocument doc1 : docs) {
                        List<String> statements = (ArrayList<String>) doc1.getStatements();
                        List<String> newStatements = new ArrayList<>();
                        for (String statement : statements) {
                            // Check if all words in the query are present in the statement
                            statement = highlight(statement, ifQuotes, false);
                            newStatements.add(statement);
                        }
                        Collections.sort(newStatements, Comparator.comparingInt(str -> ((String) str).length()).reversed());
                        doc1.setStatements(newStatements);
                        doc1.setTfIdf(df * doc1.getTf());
                        ret.add(doc1);
                    }
                }
            }

        }
        return ret;
    }

    private static String highlight(String input, String word, boolean exactMatch) {
        // Regular expression pattern to find "GitHub" ignoring case
        String Regex = "(?i)" + word;
        if(exactMatch) {
            Regex = "\\b(?i)" + word + "\\b";
        }
        Pattern pattern = Pattern.compile(Regex);
        Matcher matcher = pattern.matcher(input);

        // Replace each occurrence of "GitHub" with "<b>GitHub</b>"
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "<b>" + matcher.group() + "</b>");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static Boolean checkPhrase(String input, String word) {
        // Regular expression pattern to find "GitHub" ignoring case
        Pattern pattern = Pattern.compile("\\b(?i)" + word + "\\b");
        Matcher matcher = pattern.matcher(input);

        return matcher.find();
    }

    public QueryProcessor() {

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

    public ArrayList<IndexerDocument> searchly(String query) {
        boolean isQoutes = isEnclosedInQuotes(query);
        ArrayList<String> operations = new ArrayList<>(Arrays.asList("AND", "OR", "NOT"));
        ArrayList<ArrayList<String>> parsedQuery = parseQuery(query, operations);
        ArrayList<IndexerDocument> documents = null;
        if(parsedQuery.get(0).isEmpty()) {

            String cleanWord = query.replaceAll("^\"|\"$", "");
            String ss = cleanWord;
            if (isQoutes) {
                System.out.println("isQoutes");
                System.out.println(cleanWord);
            }
            String[] words = preprocess(cleanWord);
            String[] stems = stemWords(words);

            printWords(stems);
            documents = (ArrayList<IndexerDocument>) searchIndex(stems, isQoutes, ss);
        }else{
            System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiimmmmmmmmm");
            String[] words;
            String[] stems;
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
                documents=(ArrayList<IndexerDocument>) searchIndex(stems,isQoutes,cleanWords.get(0)+" "+cleanWords.get(1));
            }else if(Ops.get(0).contains("or")){
                System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiimmmmmmmmm");
                words=preprocess(cleanWords.get(0));
                stems = stemWords(words);
                printWords(stems);
                documents=(ArrayList<IndexerDocument>) searchIndex(stems,isQoutes,cleanWords.get(0));
                words=preprocess(cleanWords.get(1));
                stems = stemWords(words);
                printWords(stems);
                documents.addAll((ArrayList<IndexerDocument>) searchIndex(stems,isQoutes,cleanWords.get(1)));
            }else if(Ops.get(0).contains("not")){
                words=preprocess(cleanWords.get(0));
                stems = stemWords(words);
                printWords(stems);
                documents=(ArrayList<IndexerDocument>) searchIndex(stems,isQoutes,cleanWords.get(0));
            }
        }

        ArrayList<IndexerDocument> FinalQuery = mergeDocuments(documents);
        addrankFD(FinalQuery);
        return FinalQuery;
    }

    private String[] preprocess(String query) {
        // Tokenize and preprocess the query
        return query.toLowerCase().split("\\s+");
    }

    public ArrayList<IndexerDocument> mergeDocuments(ArrayList<IndexerDocument> documents) {
        // Map to store documents grouped by URL
        Map<String, IndexerDocument> mergedDocsMap = new HashMap<>();

        // Iterate over each document
        for (IndexerDocument doc : documents) {
            String url = doc.getUrl();
            double idfTf = doc.getTfIdf();

            // If the URL is already present in the map, merge idf-tf values
            if (mergedDocsMap.containsKey(url)) {
                IndexerDocument mergedDoc = mergedDocsMap.get(url);
                mergedDoc.setTfIdf(mergedDoc.getTfIdf() + idfTf);
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
            String stemmedWord = stemmer.stemWord(word);
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

    private List<IndexerDocument> searchIndex(String[] stems, boolean isQoutes, String ifqoutes) {
        // Search the index for documents containing words with the same stems
        return search(stems, isQoutes, ifqoutes);
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

    private void addrankFD(ArrayList<IndexerDocument> documents) {
        for (IndexerDocument doc : documents) {
            Double a = 0.0;
            if (doc.getInHead())
                a = 1.0;
            if (doc.getInTitle())
                a += 0.5;
            doc.setRank(doc.getRank() + doc.getTfIdf() + a);
        }
        Collections.sort(documents, Comparator.comparingDouble(doc -> ((IndexerDocument) doc).getRank()).reversed());
    }
}



