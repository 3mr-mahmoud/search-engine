package PageRank;

import DB.*;

import com.mongodb.client.FindIterable;
import org.bson.Document;

import java.util.Iterator;
import java.util.Vector;

public class PageRank {
    private Mongo DB;
    private Vector<Double> curRank;
    private Vector<Double> nextRank;
    FindIterable<Document> crawledDocs;
    Iterator<Document> iterator;

    PageRank() {
        this.DB = new Mongo();
        this.curRank = new Vector<Double>();
        this.nextRank = new Vector<Double>();
        this.crawledDocs = DB.findDocumentsWithFilter(new Document(), "URL", "links", "_id");
    }

    void Rank(int N) {
        for (int i = 0; i < DB.CountCrawled(); i++) {
            curRank.add(1.0 / DB.CountCrawled());
        }
        for (int i = 0; i < N; i++) {
            if (i == 0) {
                DB.InitializeRank();
                continue;
            }

            iterator = crawledDocs.iterator();
            RankIteration();
            curRank.clear();
            for (int j = 0; j < nextRank.size(); j++) {
                curRank.add(nextRank.elementAt(j));
            }
            nextRank.clear();
        }
        for (int i = 0; i < curRank.size(); i++) {
            DB.updateRank(i, curRank.elementAt(i));
        }
    }

    void RankIteration() {
        for (int i = 0; i < DB.CountCrawled(); i++) {
            Document doc = new Document();
            if (iterator.hasNext())
                doc = iterator.next();
            String URL = doc.getString("URL");
            double rank = 0;
            for (Document page : crawledDocs) {
                if (page.getInteger("_id") == doc.getInteger("_id"))
                    continue;
                // System.out.println(page.toJson());
                if (page.getList("links", String.class).contains(URL))
                    rank += curRank.elementAt(page.getInteger("_id")) / page.getList("links", String.class).size();
            }
            nextRank.add(rank);
        }
    }
}
