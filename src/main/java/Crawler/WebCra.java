package Crawler;

import DB.Mongo;

import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WebCra implements Runnable {
    private Mongo DB;
    private static Boolean done = new Boolean(false);   //Boolean to stop crawling when reach 6000 crawled pages
    private static Boolean stopSearch = new Boolean(false); //Boolean to stop inserting seeds when reach 15000 links

    public WebCra(Mongo DB) {
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
                }
            }

            if (done)
                return;
            org.jsoup.nodes.Document doc = GetDoc(seed);
            if (doc != null) {
                String body = doc.body().text().toString();    //convert text of body html element to string for compacting
                String hash = CompactSt(body);
                if (!DB.isCrawled(hash, seed)) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");
                    org.bson.Document newPage = new org.bson.Document()
                            .append("Page", doc.toString())
                            .append("URL", seed)
                            .append("Compact", hash)
                            .append("CrawlTime", LocalDateTime.now().format(formatter).toString())
                            .append("Count", 1);
                    if (done)
                        return;
                    DB.InsertPage(newPage);
                    if (!stopSearch) {
                        Elements links = doc.select("a[href]");
                        for (Element link : links) {
                            String URL = link.absUrl("href");
                            String linkNormal = URL.split("[?#]")[0];
                            if (CheckRobot(linkNormal))
                                DB.InsertSeed(new Document("URL", linkNormal));
                            if (stopSearch)
                                break;
                        }
                    }
                }
            }
            if (done)
                return;
        }

    }

    private org.jsoup.nodes.Document GetDoc(String url) {
        try {
            return Jsoup.connect(url).get();
        } catch (Exception e) {
            System.out.println("Error in connection URL: " + url + "by thread " + Thread.currentThread().getName() + " " + e.getMessage());
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
            System.out.println("Error in compacting" + e.getMessage() + " with Thread " + Thread.currentThread().getName());
            return null;
        }
    }

    private boolean CheckRobot(String url) {
        try {
            URL link = new URL(url);
            //get link of robots.txt of this url
            String admin = link.getProtocol() + "://" + link.getHost() + "/robots.txt";
            //We specify user agents as some websites doesn't allow direct access to robots.txt file
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.71 Safari/537.36";
            org.jsoup.nodes.Document doc = Jsoup.connect(admin).userAgent(userAgent).ignoreContentType(true).followRedirects(false).get();
            String[] lines = doc.body().text().toLowerCase().split("user-agent: ");
            String[] global = null;
            for (String line : lines) {
                if (line.startsWith("* ")) {
                    global = line.split("disallow: ");
                }
            }
            for (int i = 1; i < global.length; i++) {
                int index = global[i].indexOf(" ");
                if (index != -1)
                    if (url.contains(global[i].substring(0, index)))
                        return false;
                    else if (url.contains(global[i]))
                        return false;
            }
        } catch (Exception e) {
            System.out.println("Error in check robots of link: " + url + " " + e.getMessage());
        }
        return true;
    }
}
