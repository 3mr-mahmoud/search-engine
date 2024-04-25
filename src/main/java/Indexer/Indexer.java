package Indexer;

import ca.rmen.porterstemmer.PorterStemmer;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import DB.Mongo;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Indexer implements Runnable {

    private Mongo DB;
    private int indexedUrls;
    public static int currentChunkIndex;

    public Indexer(Mongo DB) {
        this.DB = DB;
        indexedUrls = (int) DB.Count("IndexedUrls");
    }

    public void run() {
        try {
            index();
        } catch (Exception e) {
            System.out.println("Error in indexing in thread " + Thread.currentThread().getName() + e.getMessage());
        }
    }

    public void index() {
        int localChunkIndex = -1;

        while (DB.Count("IndexedUrls") < DB.Count("Crawler")) {
            synchronized (DB) {
                currentChunkIndex++;
                localChunkIndex = currentChunkIndex;
            }

            // get my document
            Document doc = DB.getCollection("Crawler").find().skip(localChunkIndex).first();
            // process my chunk
            if (doc != null) {
                String url = doc.getString("URL");
                System.out.println(url);

                if (DB.isIndexed(url)) {
                    System.out.println("Page already indexed");
                    continue;
                }

                // mark the page as indexed
                DB.insertIndexedUrl(url);

                org.jsoup.nodes.Document pageDoc = Jsoup.parse(doc.getString("Page"));
                Elements elements = pageDoc.getAllElements();
                PorterStemmer porterStemmer = new PorterStemmer();
                StopWordsChecker checker = new StopWordsChecker();
                HashMap<String, wordInfo> wordCountMap = new HashMap<>();
                long wordsCount = 0;

                for (Element element : elements) {
                    if (element.ownText().isEmpty()) {
                        continue;
                    }

                    String[] words = element.ownText().split("\\s+");
                    for (String word : words) {
                        boolean inHead = false;
                        if (element.nodeName().equals("h1") || element.nodeName().equals("h5")
                                || element.nodeName().equals("h2") || element.nodeName().equals("h3")
                                || element.nodeName().equals("h4") || element.nodeName().equals("h6"))
                            inHead = true;
                        String S = pageDoc.getElementsByTag("title").text().toLowerCase();
                        boolean inTitle = (element.nodeName().equals("title"));

                        if (checker.isStopWord(word)) {
                            continue;
                        }
                        wordsCount++;

                        // clean it
                        String cleanWord = word.replaceAll("[^A-Za-z0-9]", "");
                        // stem it
                        String stemmedWord = porterStemmer.stemWord(cleanWord);
                        if (stemmedWord.isEmpty())
                            continue;
                        if (wordCountMap.containsKey(stemmedWord)) {
                            wordCountMap.get(stemmedWord).count++;
                            if (inTitle)
                                wordCountMap.get(stemmedWord).inTitle = inTitle;
                            if (inHead)
                                wordCountMap.get(stemmedWord).inHead = inHead;

                        } else {
                            wordInfo I = new wordInfo();
                            wordCountMap.put(stemmedWord, I);
                            wordCountMap.get(stemmedWord).count = 1;
                            if (inTitle) {
                                wordCountMap.get(stemmedWord).inTitle = inTitle;
                            }
                            if (inHead)

                            {
                                wordCountMap.get(stemmedWord).inHead = inHead;
                            }
                        }
                    }
                }

                for (String word : wordCountMap.keySet()) {
                    if (!DB.isWordIndexed(word)) {
                        org.bson.Document doc1 = new org.bson.Document();
                        doc1.append("url", url);
                        wordInfo info = wordCountMap.get(word);

                        doc1.append("inHead", info.inHead);
                        doc1.append("inTitle", info.inTitle);
                        doc1.append("count", info.count);
                        doc1.append("tf", (float) info.count / (float) wordsCount);

                        // Extract statements where the word appears
                        Elements statementElements = pageDoc.getElementsContainingOwnText(word);
                        List<Document> statements = extractStatements(statementElements);

                        doc1.append("statements", statements);

                        List<Document> documents = Arrays.asList(doc1);

                        org.bson.Document newPage = new org.bson.Document()
                                .append("word", word)
                                .append("documentCount", 1)
                                .append("documents", documents);
                        DB.InsertWordIndexer(newPage);
                    } else {
                        // Update page
                        Document page = DB.GetIndexedWord(word);
                        org.bson.Document doc1 = new org.bson.Document();
                        wordInfo info = wordCountMap.get(word);

                        doc1.append("url", url);
                        doc1.append("inHead", info.inHead);
                        doc1.append("inTitle", info.inTitle);
                        doc1.append("count", info.count);
                        doc1.append("tf", (float) info.count / (float) wordsCount);

                        // Extract statements where the word appears
                        Elements statementElements = pageDoc.getElementsContainingOwnText(word);
                        List<Document> statements = extractStatements(statementElements);

                        doc1.append("statements", statements);

                        List<Document> documents = page.get("documents", List.class);
                        Integer count = page.get("documentCount", Integer.class);

                        count++;
                        documents.add(doc1);
                        page.put("documentCount", count);
                        page.put("documents", documents);
                        DB.UpdateIndexWord(word, page);
                    }

                }
            }

        }
    }

    // Function to extract statements where a word appears
    private List<Document> extractStatements(Elements statementElements) {
        List<Document> statements = new ArrayList<>();
        for (Element statementElement : statementElements) {
            String statementText = statementElement.ownText();
            Document statementDoc = new Document("state", statementText);
            statements.add(statementDoc);
        }
        return statements;
    }
}
