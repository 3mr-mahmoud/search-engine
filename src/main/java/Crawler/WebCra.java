package Crawler;

import DB.Mongo;

import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Vector;


public class WebCra implements Runnable{
    private Mongo DB;
    private static boolean done = false;
    private static boolean stopSearch = false;
    public WebCra(Mongo DB){
        this.DB = DB;

    }

    public void run(){
        try{
            Crawl();
        }catch (Exception e){
            System.out.println("Error in crawling in thread " + Thread.currentThread().getName() + e.getMessage());
        }
    }
    public void Crawl(){

        String seed = "";
        long foundCrawled = DB.CountCrawled();
        long foundSeeded = DB.CountSeeded();
        if(foundCrawled >= DB.MAX_PAGES ){
            done = true;
        }else if(foundCrawled + foundSeeded >= DB.MORE_PAGES){
            stopSearch = true;
            seed = DB.GetSeed();
        }else{
            seed = DB.GetSeed();
        }
        if(!done){
            org.jsoup.nodes.Document doc = GetDoc(seed);
            if(doc != null){
                String body = doc.body().toString();
                String hash = CompactSt(body);
                if(!DB.isCrawled(hash,seed)){
                    String title = doc.title();
                    if(title == null){
                        title = seed;
                    }
                    org.bson.Document newPage = new org.bson.Document()
                            .append("Title",title)
                            .append("Body",body)
                            .append("URL",seed)
                            .append("Compact",hash);
                    DB.InsertPage(newPage);
                    if(!stopSearch) {
                        Elements links = doc.select("a[href]");
                        Vector<Document> URLs = new Vector<>();
                        for (Element link : links) {
                            String URL = link.absUrl("href");
                            String linkNormal = URL.split("[?#]")[0];
                            URLs.addElement(new Document().append("URL", linkNormal));
                        }
                        DB.InsertSeeds(URLs);
                    }
                }
            }
            Crawl();
        }

    }

    private org.jsoup.nodes.Document GetDoc(String url){
        try{
            return Jsoup.connect(url).get();
        }catch (Exception e){
            System.out.println("Error in connection URL: " + url + "by thread " + Thread.currentThread().getName() + " " + e.getMessage());
             return null;
        }
    }
    private String CompactSt(String body){
        try {
            String[] splitted = body.split(" ");
            StringBuilder comp = new StringBuilder();
            for (String ele : splitted){
                if(ele != null && ele.compareTo("") != 0)
                    comp.append(ele.charAt(0));
            }
            return comp.toString();
        }catch (Exception e){
            System.out.println("Error in compacting" + e.getMessage() + " with Thread " + Thread.currentThread().getName());
            return null;
        }
    }

}
