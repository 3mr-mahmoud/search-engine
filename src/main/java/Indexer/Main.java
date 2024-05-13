package Indexer;

import DB.Mongo;

public class Main {


    public static void main(String[] args) {
        Mongo DB = new Mongo(); //create mongo data base
        /*          MultiThreading                */
        int numOfThreads = 16;
        Thread[] threads = new Thread[numOfThreads];
        int i = 0;
        Indexer.currentChunkIndex = (DB.Count("IndexedUrls")) - 1;

        System.out.println("Main"+Indexer.currentChunkIndex);

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
