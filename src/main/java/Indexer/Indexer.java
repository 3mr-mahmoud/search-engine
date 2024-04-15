package Indexer;

import ca.rmen.porterstemmer.PorterStemmer;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import DB.Mongo;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class Indexer implements Runnable {


    /*
     * todo
     * 1. fetch pages from crawler collection
     * 2. loop on each document and get every word
     * 3. Parse stopWords.txt to ignore some words
     * 4. Get document words
     * 4.0 stem each word
     * 4.1 make a hash map and add every unique word to it <word ,
     * countOfOccurences>
     * 4.2 count all words
     * 4.3 update the indexed documents count
     * 4.4 update the word document frequency
     * 5.
     */

    private Mongo DB;

    private int chunkSize = 10;
    private int currentChunkIndex = 0;

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
        // initialize chunkIndex
        int indexedUrls = (int) DB.Count("IndexedUrls");
        currentChunkIndex = (indexedUrls / chunkSize) - 1;
        int localChunkIndex = -1;
        synchronized (this) {
            currentChunkIndex++;
            localChunkIndex = currentChunkIndex;
        }

        // get my chunk of documents
        MongoCursor<Document> cursor = DB.getCollection("Crawler").find()
                .skip(localChunkIndex * chunkSize)
                .limit(chunkSize)
                .iterator();
        // process my chunk
        while (cursor.hasNext()) {
            Document doc = cursor.next();

            String url = doc.getString("URL");
            System.out.println(url);
            if (DB.isIndexed(url)) {
                System.out.println("Page already indexed");
                return;
            }

            org.jsoup.nodes.Document pageDoc = Jsoup.parse(doc.getString("Page"));
            Elements elements = pageDoc.getAllElements();
            PorterStemmer porterStemmer = new PorterStemmer();
            StopWordsChecker checker = new StopWordsChecker();
            Elements headingTags = pageDoc.select("h1, h2, h3, h4, h5, h6");
            Elements titleTags = pageDoc.select("title");
            HashMap<String, wordInfo> wordCountMap = new HashMap<>();
            long wordsCount = 0;

            for (Element element : elements) {
                if (element.ownText().isEmpty()) {
                    continue;
                }

                String[] words = element.ownText().split("\\s+");
                for (String word : words) {
                    boolean inHead = false;
                    if(element.nodeName().equals("h1")||element.nodeName().equals("h5")||element.nodeName().equals("h2")||element.nodeName().equals("h3")||element.nodeName().equals("h4")||element.nodeName().equals("h6"))
                        inHead = true;
                    String S = pageDoc.getElementsByTag("title").text().toLowerCase();
                    boolean inTitle = (element.nodeName().equals("title"));


                    if(checker.isStopWord(word)) {
                        continue;
                    }
                    wordsCount++;

                    // clean it
                    String cleanWord = word.replaceAll("[^A-Za-z0-9]","");
                    // stem it
                    String stemmedWord = porterStemmer.stemWord(cleanWord);
                    if(stemmedWord.isEmpty())
                        continue;
                    if (wordCountMap.containsKey(stemmedWord)) {
                        wordCountMap.get(stemmedWord).count++;
                        if(inTitle)
                            wordCountMap.get(stemmedWord).inTitle = inTitle;
                        if(inHead)
                            wordCountMap.get(stemmedWord).inHead = inHead;

                    } else {
                        wordInfo I = new wordInfo();
                        wordCountMap.put(stemmedWord, I);
                        wordCountMap.get(stemmedWord).count = 1;
                        if(inTitle)
                        {
                            wordCountMap.get(stemmedWord).inTitle = inTitle;}
                        if(inHead)

                        {wordCountMap.get(stemmedWord).inHead = inHead;}
                    }
                }
            }
//            System.out.println("Word Count:"+wordsCount);
//             {
//                wordInfo info = wordCountMap.get(word);
//                System.out.println(word + ": " + info.count + " inHead: " + wordCountMap.get(word).inHead + " inTitle: " + wordCountMap.get(word).inTitle);
            for (String word : wordCountMap.keySet()) {
                if (!DB.isWordIndexed(word))
                {
                    org.bson.Document doc1 = new org.bson.Document();
                    doc1.append("url", url);
                    wordInfo info = wordCountMap.get(word);

                    doc1.append("inHead", info.inHead);
                    doc1.append("inTitle", info.inTitle);
                    doc1.append("count", info.count);
                    doc1.append("tf", (float)info.count/(float) wordsCount);

                    List<Document> documents = Arrays.asList(doc1);

                    org.bson.Document newPage = new org.bson.Document()
                            .append("word", word)
                            .append("documentCount", 1)
                            .append("documents", documents);
                    DB.InsertWordIndexer(newPage);
                }
                else
                {
                    //Update page
                    Document page = DB.GetIndexedWord(word);
                    org.bson.Document doc1 = new org.bson.Document();
                    wordInfo info = wordCountMap.get(word);

                    doc1.append("url", url);
                    doc1.append("inHead", info.inHead);
                    doc1.append("inTitle", info.inTitle);
                    doc1.append("count", info.count);
                    doc1.append("tf", (float)info.count/(float) wordsCount);

                    List<Document> documents = page.get("documents", List.class);
                    Integer count = page.get("documentCount", Integer.class);

                    count++;
                    documents.add(doc1);
                    page.put("documentCount", count);
                    page.put("documents", documents);
                    DB.UpdateIndexWord(word,page);
                }

            }
        }
        }
    }

