package Crawler;

import DB.Mongo;

import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

        String seed = "";
        long foundCrawled = DB.Count("Crawler");
        long foundSeeded = DB.Count("Seeds");
        // is here synchronization????
        synchronized (this.done) {
            if (foundCrawled >= DB.MAX_PAGES) {
                done = true;
            } else if (foundCrawled + foundSeeded >= DB.MORE_PAGES) {
                stopSearch = true;
                seed = DB.GetSeed();
            } else {
                seed = DB.GetSeed();
            }
        }

        if (!done) {
            org.jsoup.nodes.Document doc = GetDoc(seed);
            if (doc != null) {
                String body = doc.body().text().toString(); // convert body html element to string for compacting
                String hash = CompactSt(body);
                if (!DB.isCrawled(hash, seed)) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");
                    org.bson.Document newPage = new org.bson.Document()
                            .append("Page", doc.toString())
                            .append("URL", seed)
                            .append("Compact", hash)
                            .append("CrawlTime", LocalDateTime.now().format(formatter).toString());
                    DB.InsertPage(newPage);
                    if (!stopSearch) {
                        Elements links = doc.select("a[href]");
                        for (Element link : links) {
                            String URL = link.absUrl("href");
                            String linkNormal = URL.split("[?#]")[0];
                            DB.InsertSeed(new Document().append("URL", linkNormal));
                        }
                    }
                }
            }
            Crawl();
        }

    }

    private org.jsoup.nodes.Document GetDoc(String url) {
        try {
            return Jsoup.connect(url).get();
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

}