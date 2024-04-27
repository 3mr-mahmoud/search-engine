package PageRank;

import DB.*;

import com.mongodb.client.FindIterable;
import org.bson.Document;

import java.util.Vector;
public class PageRank {
    private Mongo DB;
    private Vector<Double> curRank;
    private Vector<Double> nextRank;
    private Vector<Document> docs;

    PageRank() {
        this.DB = new Mongo();
        this.curRank = new Vector<Double>();
        this.nextRank = new Vector<Double>();
        this.docs = new Vector<Document>();
        FindIterable<Document> crawledDocs = DB.findDocumentsWithFilter(new Document(), "URL", "links", "_id");
        for (Document doc : crawledDocs) {
            docs.add(doc);
        }
    }

    void Rank(int N) {
        double startRank = (double) DB.CountCrawled();
        for (int i = 0; i < DB.CountCrawled(); i++) {
            curRank.add(startRank);
        }
        DB.InitializeRank();
        DB.Close();
        for (int i = 1; i < N; i++) {
            RankIteration((int) startRank);
            System.out.println("i "+i);
            curRank.clear();
            for (int j = 0; j < nextRank.size(); j++) {
                curRank.add(nextRank.elementAt(j));
            }
            nextRank.clear();
        }
        this.DB = new Mongo();
        for (int i = 0; i < curRank.size(); i++) {
            DB.updateRank(i, curRank.elementAt(i));
        }
    }

    void RankIteration(int size) {
        for (int i = 0; i < size; i++) {
            Document doc = docs.elementAt(i);
            String URL = doc.getString("URL");
            double rank = 0;
            for (Document page : docs) {
                if (page.getString("URL").equals(URL))
                    continue;
                // System.out.println(page.toJson());
                if (page.getList("links", String.class).contains(URL))
                    rank += curRank.elementAt(page.getInteger("_id")) / page.getList("links", String.class).size();
            }
            rank = (1-0.85)+ 0.85*rank;

            nextRank.add(rank);
        }
    }
}