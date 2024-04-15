package org.example;
import DB.*;
import Crawler.*;

import org.jsoup.Jsoup;

import java.net.URL;

public class Main {
    public static void main(String[] args){
        Mongo DB = new Mongo(); //create mongo data base
        DB.InitialSeed();   //insert base seeds if there is on seeds
        /*          MultiThreading                */
        int numOfThreads = 50;
        Thread[] threads = new Thread[numOfThreads];
        int i = 0;
        for (Thread ele : threads){
            ele = new Thread(new WebCrawler(DB));
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
