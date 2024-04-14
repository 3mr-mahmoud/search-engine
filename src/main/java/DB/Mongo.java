package DB;

import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import org.bson.Document;

import javax.print.Doc;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class Mongo {
    public final int MORE_PAGES = 15000;
    public final int MAX_PAGES = 6000;
    private Integer id = new Integer(0);
    private MongoClient client;
    private MongoDatabase DB;
    private MongoCollection<Document> seedCollection;
    private MongoCollection<Document> crawlerCollection;

    public Mongo() {
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
        client = MongoClients.create(connectionString);
        DB = client.getDatabase("Engine");
        seedCollection = DB.getCollection("Seeds");
        crawlerCollection = DB.getCollection("Crawler");
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

    public boolean isCrawled(String hash, String URL) {
        //check if the page is duplicated and update the counter if found
        try {
            boolean isDup1, isDup2;
            Document found1, found2;
            synchronized (this) {
                isDup1 = (found1 = crawlerCollection.find(new Document().append("Compact", hash)).first()) != null;
                isDup2 = (found2 = crawlerCollection.find(new Document().append("URL", URL)).first()) != null;
                if (found2 != null) {
                    int count = found1.getInteger("Count");
                    Document filter = new Document("URL", URL);
                    Document update = new Document("$set", new Document("Count", ++count));
                    crawlerCollection.updateOne(filter, update);
                } else if (found1 != null) {
                    int count = found1.getInteger("Count");
                    Document filter = new Document("Compact", hash);
                    Document update = new Document("$set", new Document("Count", ++count));
                    crawlerCollection.updateOne(filter, update);
                }
            }
            return isDup1 || isDup2;
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

}
