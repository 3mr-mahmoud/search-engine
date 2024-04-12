package org.example;
import DB.*;
import Crawler.*;

public class Main {
    public static void main(String[] args){
        Mongo DB = new Mongo();
        DB.InitialSeed();
        /*          MultiThreading                */
        int numOfThreads = 50;
        Thread[] threads = new Thread[numOfThreads];
        int i = 0;
        for (Thread ele : threads){
            ele = new Thread(new WebCra(DB));
            ele.setName(Integer.toString(i++));
            ele.start();
        }
        for (Thread ele : threads){
            try{
                if(ele != null)
                    ele.join();
            }catch (InterruptedException e){
                System.out.println("Error in finishing thread with id = " + ele.getId() + e.getMessage());
            }
        }
    }
}