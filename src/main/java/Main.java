import Crawler.WebCrawler;
import DB.Mongo;
import Indexer.Indexer;
import Indexer.StopWordsChecker;
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import  ca.rmen.porterstemmer.PorterStemmer;

import java.util.HashMap;
import java.util.List;

public class Main {


    public static void main(String[] args) {
        Mongo DB = new Mongo(); //create mongo data base
        /*          MultiThreading                */
        int numOfThreads = 1;
        Thread[] threads = new Thread[numOfThreads];
        int i = 0;
        for (Thread ele : threads){
            ele = new Thread(new Indexer(DB));
            ele.setName(Integer.toString(i++));
            ele.start();
        }
        for (Thread ele : threads) {
            try {
                if (ele != null)
                    ele.join();
            } catch (InterruptedException e) {
                System.out.println("Error in finishing thread with id = " + ele.getId() + e.getMessage());
            }
        }

    }
}