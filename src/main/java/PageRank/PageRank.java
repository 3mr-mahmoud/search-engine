package PageRank;
import DB.*;

import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import com.panforge.robotstxt.RobotsTxt;

import javax.swing.text.DocumentFilter;

public class PageRank
{
    private Mongo DB;
    private Vector<Double> nextRank;
    FindIterable<Document> crawledDocs;
    public Document selectDocumentAtIndex(FindIterable<Document> iterable, int index) {
        return iterable.skip(index).limit(1).first();
    }
    PageRank()
    {
        this.DB = new Mongo();
        this.nextRank = new Vector<Double>();
    }

    void Rank(int N)
    {
        for(int i = 0; i < N; i++)
        {
            if(i == 0)
            {
                DB.InitializeRank();
                crawledDocs = DB.findDocumentsWithFilter(new Document(), "URL", "links", "_id", "rank");
                continue;
            }
            RankIteration();
            int j = 0;
            for(Document page : crawledDocs)
            {
                page.put("rank", nextRank.elementAt(j));
                j++;
            }
            nextRank.clear();
        }
        for(int i=0;i<nextRank.size();i++)
        {
            DB.updateRank(i, nextRank.elementAt(i));
        }
    }

    void RankIteration()
    {
        for(int i=0;i<DB.CountCrawled();i++)
        {
            Document doc = new Document();
            doc = selectDocumentAtIndex(crawledDocs ,i);
            String URL = doc.getString("URL");
            double rank = 0;
            for (Document page : crawledDocs)
            {
                if(page.getList("links", String.class).contains(URL))
                    rank += page.getDouble("rank") / page.getList("links",String.class).size();
            }
            nextRank.add(rank);
        }
    }
}
