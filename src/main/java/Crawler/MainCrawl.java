package Crawler;

import DB.*;

public class MainCrawl {
    public static void main(String[] args) {
        Mongo DB = new Mongo(); //create mongo data base
        DB.InitialSeed();   //insert base seeds if there is on seeds
        /*          MultiThreading                */
        int numOfThreads = 100;
        Thread[] threads = new Thread[numOfThreads];
        for (int i = 0; i < numOfThreads; i++) {
            threads[i] = new Thread(new WebCrawler(DB));
            threads[i].setName(Integer.toString(i));
            threads[i].start();
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
