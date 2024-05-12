package com.dragonball.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public ArrayList<IndexerDocument> search(String[] stems, boolean isQoutes, ArrayList<String> phrases,
            ArrayList<String> operators) {
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
                        System.out.println("hee");
                        if (statements != null) {
                            // all false by default
                            boolean[] phrasesFound = new boolean[phrases.size()];
                            System.out.println(phrasesFound.toString());

                            for (String statement : statements) {

                                String highlightedStatement = statement;
                                for (int i = 0; i < phrases.size(); i++) {
                                    System.out.println("checking for phrase " + phrases.get(i));
                                    // Check if all words in the query are present in the statement
                                    if (checkPhrase(statement, phrases.get(i))) {
                                        System.out.println("found phrase " + phrases.get(i));

                                        highlightedStatement = highlight(highlightedStatement, phrases.get(i));

                                        phrasesFound[i] = true;
                                    }
                                }
                                if (!highlightedStatement.equals(statement)) {
                                    newStatements.add(highlightedStatement);
                                }

                            }

                            boolean containsAllWords = phrasesFound[0]; // Initialize flag for each document

                            for (int i = 0; i < operators.size(); i++) {
                                if (operators.get(i).toUpperCase().equals("AND")) {
                                    containsAllWords = containsAllWords && phrasesFound[i + 1];
                                } else if (operators.get(i).toUpperCase().equals("OR")) {
                                    containsAllWords = containsAllWords || phrasesFound[i + 1];
                                } else if (operators.get(i).toUpperCase().equals("NOT")) {
                                    containsAllWords = containsAllWords && !phrasesFound[i + 1];
                                }
                            }

                            Collections.sort(newStatements,
                                    Comparator.comparingInt(str -> ((String) str).length()).reversed());
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
                            String highlightedStatement = highlight(statement, phrases.get(0));
                            if (!highlightedStatement.equals(statement)) {
                                newStatements.add(highlightedStatement);
                            }
                        }
                        Collections.sort(newStatements,
                                Comparator.comparingInt(str -> ((String) str).length()).reversed());
                        doc1.setStatements(newStatements);
                        doc1.setTfIdf(df * doc1.getTf());
                        ret.add(doc1);
                    }
                }
            }

        }
        return ret;
    }

    private static String highlight(String input, String word) {
        // Regular expression pattern to find "GitHub" ignoring case
        String Regex = "\\b(?i)" + word + "\\b";
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

    public ArrayList<IndexerDocument> searchly(String query) {
        boolean isQoutes = isEnclosedInQuotes(query);

        String cleanWord = query.replaceAll("\"", "");
        String ss = query;
        if (isQoutes) {
            System.out.println("isQoutes");
            System.out.println(cleanWord);
        }
        String[] words = preprocess(cleanWord);
        String[] stems = stemWords(words);
        ArrayList<IndexerDocument> documents;
        printWords(stems);
        documents = (ArrayList<IndexerDocument>) searchIndex(stems, isQoutes, ss);
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
        ArrayList<String> phrases = new ArrayList<>();
        ArrayList<String> operators = new ArrayList<>();
        if (isQoutes) {
            Pattern pattern = Pattern.compile("\"([^\"]*)\"(?:\\s+(AND|OR|NOT))?"); // Modified pattern
            Matcher matcher = pattern.matcher(ifqoutes);

            // Extract quoted strings and operators
            while (matcher.find()) {
                phrases.add(matcher.group(1));
                System.out.println("Quoted string: " + matcher.group(1));
                if (matcher.group(2) != null) {
                    operators.add(matcher.group(2));
                    System.out.println("Operator: " + matcher.group(2));
                }
            }
        } else {
            phrases.add(ifqoutes);
        }

        return search(stems, isQoutes, phrases, operators);
    }

    private boolean isEnclosedInQuotes(String s) {
        // Check if the modified string starts and ends with double quotes
        return s.contains("\"");
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
