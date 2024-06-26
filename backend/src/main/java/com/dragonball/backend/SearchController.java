package com.dragonball.backend;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import javax.management.Query;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController

@RequestMapping("/api")
public class SearchController {

    private static final int PER_PAGE = 10;
    @Autowired
    private QueryProcessor queryProcessor;

    @Autowired
    private SearchHistoryService searchHistoryService;

    @GetMapping("/search")
    public ResponseEntity<PaginatedIndexerDocument> search(@RequestParam String keyword,
            @RequestParam(name = "page", defaultValue = "1") int page) {

        long startTime = System.nanoTime();

        // Call your function here
        List<IndexerDocument> results = queryProcessor.searchly(keyword);

        long endTime = System.nanoTime();

        // Calculate elapsed time in seconds
        double elapsedTimeInSeconds = (endTime - startTime) / 1_000_000_000.0;


        if(page == 1) {
            searchHistoryService.saveKeyword(keyword);
        }

        PaginatedIndexerDocument paginatedIndexerDocument = new PaginatedIndexerDocument();
        paginatedIndexerDocument.setPagesAvailable((int) Math.ceil((double) results.size() / (double) PER_PAGE));
        paginatedIndexerDocument.setTotal(results.size());
        paginatedIndexerDocument.setElapsedTime(elapsedTimeInSeconds);
        paginatedIndexerDocument.setCurrentPage(page);
        int startIndex = (page - 1) * PER_PAGE;
        int endIndex = Math.min(startIndex + PER_PAGE, results.size());
        List<IndexerDocument> resultsChunk;
        if (startIndex >= results.size()) {
            // If startIndex exceeds the list size, return an empty list
            resultsChunk = new ArrayList<>();
        } else {
            resultsChunk = results.subList(startIndex, endIndex);
        }
        paginatedIndexerDocument.setResults(resultsChunk);
        return new ResponseEntity<PaginatedIndexerDocument>(paginatedIndexerDocument, HttpStatus.OK);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<SearchHistory>> getSuggestions(@RequestParam(name = "keyword", defaultValue = "") String partialString) {
        List<SearchHistory> results = searchHistoryService.findSuggestions(partialString);
        return new ResponseEntity<List<SearchHistory>>(results, HttpStatus.OK);
    }

}
