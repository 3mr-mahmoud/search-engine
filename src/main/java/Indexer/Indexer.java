package Indexer;

import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import DB.Mongo;

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
            if (DB.isIndexed(url)) {
                System.out.println("Page already indexed");
                return;
            }

            org.jsoup.nodes.Document pageDoc = Jsoup.parse(doc.getString("Page"));
            Elements pageElements = pageDoc.getAllElements();
            String elementTag, elementText;
            for (Element element : pageElements) {
                elementTag = element.nodeName();
                // skip links and empty elements in page
                elementText = element.ownText();
                if (elementTag.equals("a") || element.ownText().isEmpty()) {
                    continue;
                }

            }
        }
    }
}
