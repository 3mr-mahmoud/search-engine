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

    private  final Mongo DB;
    public static long currentChunkIndex;

    public Indexer(Mongo DB) {
        this.DB = DB;
    }

    public void run() {
        try {
            index();
        } catch (Exception e) {
            System.out.println("Error in indexing in thread " + Thread.currentThread().getName() + e.getMessage());
        }
    }

    public void index() {
        long localChunkIndex = -1;
        long crawlerPages = DB.Count("Crawler");
        while ( DB.Count("IndexedUrls") < DB.Count("Crawler") && currentChunkIndex < crawlerPages) {
            synchronized (DB) {
                currentChunkIndex++;
                localChunkIndex = currentChunkIndex;
            }

            System.out.println(localChunkIndex);

            // get my document
            Document doc = DB.getCollection("Crawler").find(new Document().append("_id",localChunkIndex)).first();
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
                    String elementText = element.ownText();
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

                        wordInfo info = wordCountMap.getOrDefault(stemmedWord, new wordInfo());

                        info.count++;

                        if (inTitle)
                            info.inTitle = inTitle;
                        if (inHead)
                            info.inHead = inHead;

                        if (elementText.length() > 10 && elementText.length() <= 580)
                            info.statements.add(elementText);

                        wordCountMap.put(stemmedWord, info);
                    }
                }

                for (String word : wordCountMap.keySet()) {
                    Document wordDoc = DB.GetIndexedWord(word);
                    if (wordDoc == null) {
                        org.bson.Document doc1 = new org.bson.Document();
                        doc1.append("url", url);
                        wordInfo info = wordCountMap.get(word);

                        doc1.append("inHead", info.inHead);
                        doc1.append("inTitle", info.inTitle);
                        doc1.append("count", info.count);
                        doc1.append("tf", (float) info.count / (float) wordsCount);
                        doc1.append("rank", doc.getDouble("rank"));
                        doc1.append("title", pageDoc.title());
                        Elements metaDescriptions = pageDoc.select("meta[name=description], meta[property=og:description]");

                        for (Element metaDescription : metaDescriptions) {
                            // Get the content attribute value
                            String descriptionContent = metaDescription.attr("content");
                            if(!descriptionContent.isEmpty()) {
                                doc1.append("description", descriptionContent);
                                break;
                            }
                        }
                        // Extract statements where the word appears
                        doc1.append("statements", info.statements);

                        List<Document> documents = Arrays.asList(doc1);

                        org.bson.Document newPage = new org.bson.Document()
                                .append("word", word)
                                .append("documentCount", 1)
                                .append("documents", documents);
                        DB.InsertWordIndexer(newPage);

                    } else {
                        // Update page
                        Document page = wordDoc;
                        org.bson.Document doc1 = new org.bson.Document();
                        wordInfo info = wordCountMap.get(word);

                        doc1.append("url", url);
                        doc1.append("inHead", info.inHead);
                        doc1.append("inTitle", info.inTitle);

                        doc1.append("count", info.count);
                        doc1.append("tf", (float) info.count / (float) wordsCount);
                        doc1.append("rank", doc.getDouble("rank"));
                        doc1.append("title", pageDoc.title());

                        Elements metaDescriptions = pageDoc.select("meta[name=description], meta[property=og:description]");

                        for (Element metaDescription : metaDescriptions) {
                            // Get the content attribute value
                            String descriptionContent = metaDescription.attr("content");
                            if(!descriptionContent.isEmpty()) {
                                doc1.append("description", descriptionContent);
                                break;
                            }
                        }
                        // Extract statements where the word appears
                        doc1.append("statements", info.statements);

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
//    private List<String> extractStatements(Elements statementElements) {
//        List<String> statements = new ArrayList<>();
//        for (Element statementElement : statementElements) {
//            String text = statementElement.ownText();
//            if (text.length() > 10 && text.length() <= 580)
//                statements.add(text);
//        }
//        return statements;
//    }
}
