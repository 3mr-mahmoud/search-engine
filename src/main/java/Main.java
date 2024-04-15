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
        String html = "<div>Computers*<p> he is An a she <a href='http://example.com/'>coMput...</a> computing.</p></div>";
        Document doc = Jsoup.parse(html);
        Elements elements = doc.getAllElements();
        PorterStemmer porterStemmer = new PorterStemmer();
        StopWordsChecker checker = new StopWordsChecker();
        Elements titleTags = doc.select("title, h1, h2, h3, h4, h5, h6");
        HashMap<String, Integer> wordCountMap = new HashMap<>();
        long wordsCount = 0;

        for (Element element : elements) {
            if (element.ownText().isEmpty()) {
                continue;
            }

            String[] words = element.ownText().split("\\s+");

            for (String word : words) {

                if(checker.isStopWord(word)) {
                    continue;
                }

                wordsCount++;


                // clean it
                String cleanWord = word.replaceAll("[^A-Za-z0-9]","");
                // stem it
                String stemmedWord = porterStemmer.stemWord(cleanWord);

                if (wordCountMap.containsKey(stemmedWord)) {
                    wordCountMap.put(stemmedWord, wordCountMap.get(stemmedWord) + 1);
                } else {
                    wordCountMap.put(stemmedWord, 1);
                }







            }
        }
        System.out.println("Word Count:"+wordsCount);
        for (String word : wordCountMap.keySet()) {
            int count = wordCountMap.get(word);
            System.out.println(word + ": " + count);
        }
    }
}