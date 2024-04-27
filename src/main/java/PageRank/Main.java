package PageRank;

import DB.Mongo;

public class Main {
    public static void main(String[] args)
    {
        PageRank Ranker = new PageRank();
        Ranker.Rank(20);
    }
}
