package Crawler;

import DB.Mongo;
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

public class WebCrawler implements Runnable {
    private Mongo DB;
    private static Boolean done = new Boolean(false); // Boolean to stop crawling when reach 6000 crawled pages
    private static Boolean stopSearch = new Boolean(false); // Boolean to stop inserting seeds when reach 15000 links

    public WebCrawler(Mongo DB) {
        this.DB = DB;

    }

    public void run() {
        try {
            Crawl();
        } catch (Exception e) {
            System.out.println("Error in crawling in thread " + Thread.currentThread().getName() + e.getMessage());
        }
    }

    public void Crawl() {
        while (true) {
            String seed = "";
            long foundCrawled = DB.CountCrawled();
            long foundSeeded = DB.CountSeeded();
            // is here synchronization????
            synchronized (this.done) {
                if (foundCrawled >= DB.MAX_PAGES) {
                    done = true;
                    stopSearch = true;
                } else {
                    if (!done && foundSeeded <= 500) {
                        stopSearch = false;
                    } else if (foundCrawled + foundSeeded >= DB.MORE_PAGES) {
                        stopSearch = true;
                    }
                    seed = DB.GetSeed();
                    if (seed.endsWith("/")) {
                        seed = seed.substring(0, seed.length() - 1);
                    }

                }
            }

            if (done)
                return;
            org.jsoup.nodes.Document doc = GetDoc(seed);
            if (doc != null) {
                String body = doc.body().text().toString(); // convert text of body html element to string for
                                                            // compacting
                String hash = CompactSt(body);
                if (!DB.isCrawled(hash, seed)) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");
                    org.bson.Document newPage = new org.bson.Document()
                            .append("Page", doc.toString())
                            .append("URL", seed)
                            .append("Compact", hash)
                            .append("CrawlTime", LocalDateTime.now().format(formatter).toString())
                            .append("Count", 1)
                            .append("rank", 0)
                            .append("links", new Vector<String>());
                    DB.InsertPage(newPage);
                    Elements links = doc.select("a[href]");
                    Vector<String> listLink = new Vector<String>();
                    if (!stopSearch) {
                        for (Element link : links) {
                            String URL = link.absUrl("href");
                            String linkNormal = URL.split("[?#]")[0];
                            if (CheckRobot(linkNormal, "*")) {
                                if (!linkNormal.equals(seed)) {
                                    listLink.add(linkNormal);
                                    DB.InsertSeed(new Document("URL", linkNormal));
                                }
                            }
                            if (stopSearch)
                                break;
                        }
                    }
                    DB.updateLinks(seed, listLink);
                    if (done)
                        return;

                }
            }
            if (done)
                return;
        }
    }

    private org.jsoup.nodes.Document GetDoc(String url) {
        try {
            Connection.Response response = Jsoup.connect(url).execute();
            return response.parse();
        } catch (Exception e) {
            System.out.println("Error in connection URL: " + url + "by thread " + Thread.currentThread().getName() + " "
                    + e.getMessage());
            return null;
        }
    }

    private String CompactSt(String body) {
        try {
            String[] splitted = body.split(" ");
            StringBuilder comp = new StringBuilder();
            for (String ele : splitted) {
                if (ele != null && ele.compareTo("") != 0)
                    comp.append(ele.charAt(0));
            }
            return comp.toString();
        } catch (Exception e) {
            System.out.println(
                    "Error in compacting" + e.getMessage() + " with Thread " + Thread.currentThread().getName());
            return null;
        }
    }

    private boolean CheckRobot(String url, String userAgent) {
        try {
            URL link = new URL(url);
            Connection.Response response = Jsoup.connect(link.getProtocol() + "://" + link.getHost() + "/robots.txt")
                    .execute();
            String doc = response.body();
            RobotsTxt robotsTxt = RobotsTxt.read(new ByteArrayInputStream(doc.getBytes()));
            return robotsTxt.query(userAgent, url);
        } catch (Exception e) {
            System.out.println("Error in check robots of link: " + url + " " + e.getMessage());
        }
        return true;
    }
}
