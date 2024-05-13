package DB;

import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import com.mongodb.client.model.Projections;

public class Mongo {
    public final int MORE_PAGES = 20000;
    public final int MAX_PAGES = 6000;
    private Integer id = new Integer(0);
    private MongoClient client;
    private MongoDatabase DB;
    private MongoCollection<Document> seedCollection;
    private MongoCollection<Document> crawlerCollection;

    private MongoCollection<Document> indexerCollection;
    private MongoCollection<Document> indexedUrlsCollection;
    private MongoCollection<Document> pageRankCollection;

    public Mongo() {
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27018");
        client = MongoClients.create(connectionString);
        DB = client.getDatabase("Engine");
        seedCollection = DB.getCollection("Seeds");
        crawlerCollection = DB.getCollection("Crawler");
        indexerCollection = DB.getCollection("Indexer");
        indexedUrlsCollection = DB.getCollection("IndexedUrls");
        pageRankCollection = DB.getCollection("PageRank");
    }

    public boolean isIndexed(String URL) {
        try {
            boolean isDup;
            synchronized (this) {
                isDup = indexedUrlsCollection.find(new Document().append("URL", URL)).first() != null;
            }
            return isDup;
        } catch (Exception e) {
            System.out.println("Error in checking existance the content of page " + e.getMessage());
            return true;
        }
    }

    public void insertIndexedUrl(String Url) {
        try {
            synchronized (this) {
                indexedUrlsCollection.insertOne(new Document().append("URL", Url));
            }
        } catch (Exception e) {
            System.out.println("Error in inserting new crawled page " + e.getMessage());
        }
    }

    public void InitialSeed() {
        if (seedCollection.countDocuments() == 0) {
            try {
                String path = ".//seed.txt";
                BufferedReader reader = new BufferedReader(new FileReader(path));
                Vector<Document> seeds = new Vector<>();
                String link;
                while ((link = reader.readLine()) != null) {
                    String linkNormal = link.split("[?#]")[0];
                    seeds.addElement(new Document().append("URL", linkNormal));
                }
                seedCollection.insertMany(seeds);
            } catch (IOException e) {
                System.out.println("Error in initializing seedCollection " + e.getMessage());
            }
        }
    }

    public void Close() {
        client.close();
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return DB.getCollection(collectionName);
    }

    public long Count(String collectionName) {
        try {
            long size = 0;
            synchronized (this) {
                size = DB.getCollection(collectionName).countDocuments();
            }
            return size;
        } catch (Exception e) {
            System.out.println("Error in counting " + collectionName + " documents " + e.getMessage());
            return 0;
        }
    }

    public long CountCrawled() {
        try {
            long size = 0;
            synchronized (this) {
                size = crawlerCollection.countDocuments();
            }
            return size;
        } catch (Exception e) {
            System.out.println("Error in counting crawled pages " + e.getMessage());

            return 0;
        }
    }

    public long CountSeeded() {
        try {
            long size = 0;
            synchronized (this) {
                size = seedCollection.countDocuments();
            }
            return size;
        } catch (Exception e) {
            System.out.println("Error in counting seeds " + e.getMessage());
            return 0;
        }
    }
    // Inside your Mongo class
    // Other methods...

    

    public String GetSeed() {
        try {
            Document doc;
            synchronized (this) {
                while ((doc = seedCollection.findOneAndDelete(new Document())) == null)
                    this.wait();
            }
            return doc.get("URL").toString();
        } catch (Exception e) {
            System.out.println("Error in getting url link from seedCollection " + e.getMessage());
            return null;
        }

    }

    public void test () {
            MongoCollection<Document> collection = crawlerCollection;

            // Define the aggregation pipeline
            List<Document> pipeline = Arrays.asList(
                    new Document("$group", new Document("_id", "$URL")
                            .append("count", new Document("$sum", 1))
                            .append("ids", new Document("$push", "$_id"))),
                    new Document("$match", new Document("count", new Document("$gt", 1))),
        new Document("$project", new Document("_id", 0)
                .append("url", "$_id").append("ids", "$ids"))
            );

            // Execute the aggregation pipeline
            List<Document> duplicates = collection.aggregate(pipeline).into(new ArrayList<>());

            // Iterate over the duplicates and format the IDs as comma-separated strings
            List<String> result = new ArrayList<>();
        for (Document duplicate : duplicates) {
                String url = duplicate.getString("_id");
                List<Integer> ids = duplicate.getList("ids", Integer.class);
                ids.remove(0);
            for (Integer id : ids) {
                result.add(id.toString());
            }
                String idsAsString = String.join(", ", ids.toString());
                System.out.println("URL: " + url);
                System.out.println("IDs: " + idsAsString);
                System.out.println("--------------------------------------------");
            }

        System.out.println("IDs: " + String.join(", ", result.toString()));
    }
    public boolean isCrawled(String hash, String URL) {
        try {
            boolean isDup1, isDup2;
            Document found1, found2;
            synchronized (this) {
                isDup2 = (found2 = crawlerCollection.find(new Document().append("URL", URL)).first()) != null;
                if (isDup2) {
                    int count = found2.getInteger("Count");
                    Document filter = new Document("_id", found2.getInteger("_id"));
                    Document update = new Document("$set", new Document("Count", ++count));
                    crawlerCollection.updateOne(filter, update);
                }
            }
            return isDup2;
        } catch (Exception e) {
            System.out.println("Error in checking existance the content of page " + e.getMessage());
            return true;
        }
    }

    public void InsertPage(org.bson.Document doc) {
        try {
            synchronized (this) {
                crawlerCollection.insertOne(doc.append("_id", id++));
            }
        } catch (Exception e) {
            System.out.println("Error in inserting new crawled page " + e.getMessage());
        }
    }

    public void updateLinks(String URL, Vector<String> links) {
        try {
            synchronized (this) {
                Document filter = new Document("URL", URL);
                Document update = new Document("$set", new Document("links", links));
                crawlerCollection.updateOne(filter, update);
            }
        } catch (Exception e) {
            System.out.println("Error in updating links of page " + e.getMessage());
        }
    }

    public void InsertSeed(Document seeds) {
        try {
            synchronized (this) {
                seedCollection.insertOne(seeds);
                this.notifyAll();
            }
        } catch (Exception e) {
            System.out.println("Error in inserting new crawled page " + e.getMessage());
        }
    }


    public void InsertWordIndexer(org.bson.Document doc) {
        try {
            //synchronized (this) {
                indexerCollection.insertOne(doc);
            //}
        } catch (Exception e) {
            System.out.println("Error in inserting new indexer page " + e.getMessage());
        }
    }

    public Document GetIndexedWord(String word) {
        return indexerCollection.find(new Document().append("word", word)).first();
    }

    public void UpdateIndexWord(String word, Document doc) {
        try {
            //synchronized (this) {
                indexerCollection.findOneAndDelete(new Document().append("word", word));
                InsertWordIndexer(doc);
            //}
        } catch (Exception e) {
            System.out.println("Error in inserting new indexer page " + e.getMessage());
        }
    }
    public ArrayList<Document> getDocumentsContainingWord(String word, String collectionName) {
        ArrayList<Document> documents = new ArrayList<>();
        try {
            synchronized (this) {
                FindIterable<Document> iterable = DB.getCollection(collectionName).find(new Document("word", word));
                iterable.forEach(documents::add);
            }
        } catch (Exception e) {
            System.out.println("Error in retrieving documents containing the word " + word + " from collection " + collectionName + ": " + e.getMessage());
        }
        return documents;
    }
    

    public Document findDocumentInCrawler(int id) {
        return crawlerCollection.find(new Document().append("_id", id)).first();
    }

    public FindIterable<Document> findDocumentsWithFilter(Document filter, String... keys) {
        return crawlerCollection.find(filter).projection(Projections.include(keys));
    }

    public void InitializeRank() {
        Document filter = new Document();
        // Define the update operation
        Document update = new Document("$set", new Document("rank", 1-0.85 + 0.85 * (1.0 / CountCrawled())));
        // Update multiple documents that match the filter criteria
        crawlerCollection.updateMany(filter, update);
    }

    public void updateRank(int id, Double rank) {
        Document filter = new Document("_id", id);
        // Define the update operation
        Document update = new Document("$set", new Document("rank", rank));
        // Update multiple documents that match the filter criteria
        crawlerCollection.updateOne(filter, update);
    }
}
