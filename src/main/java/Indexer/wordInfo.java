package Indexer;

public class wordInfo {
    public String word;
    public boolean inTitle;
    public boolean inHead;
    public long count;
    public wordInfo()
    {
        count = 0;
        word = "";
        inHead = false;
        inTitle = false;
    }

    // You can also add a constructor, getters and setters if needed
}

