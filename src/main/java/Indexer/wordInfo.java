package Indexer;

import java.util.ArrayList;
import java.util.List;

public class wordInfo {
    public String word;
    public boolean inTitle;
    public boolean inHead;
    public long count;

    public List<String> statements;
    public wordInfo()
    {
        count = 0;
        word = "";
        inHead = false;
        inTitle = false;
        statements = new ArrayList<>();
    }

    // You can also add a constructor, getters and setters if needed
}

