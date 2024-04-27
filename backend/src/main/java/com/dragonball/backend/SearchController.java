package com.dragonball.backend;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import javax.management.Query;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://127.0.0.1:5173")
@RestController

@RequestMapping("/api/search")
public class SearchController {

    private static final int PER_PAGE = 1;
    @Autowired
    private QueryProcessor queryProcessor;

    @GetMapping
    public ResponseEntity<PaginatedIndexerDocument> search(@RequestParam String keyword, @RequestParam(name = "page", defaultValue = "1") int page) {
        List<IndexerDocument> results = queryProcessor.searchly(keyword);

        PaginatedIndexerDocument paginatedIndexerDocument = new PaginatedIndexerDocument();
        paginatedIndexerDocument.setPagesAvailable((int) Math.ceil((double) results.size() /(double) PER_PAGE));
        paginatedIndexerDocument.setTotal(results.size());
        paginatedIndexerDocument.setCurrentPage(page);
        int startIndex = (page - 1) * PER_PAGE;
        int endIndex = Math.min(startIndex + PER_PAGE, results.size());
        List<IndexerDocument> resultsChunk;
        if (startIndex >= results.size()) {
            // If startIndex exceeds the list size, return an empty list
            resultsChunk=  new ArrayList<>();
        } else {
            resultsChunk =  results.subList(startIndex, endIndex);
        }
        paginatedIndexerDocument.setResults(resultsChunk);
        return new ResponseEntity<PaginatedIndexerDocument>(paginatedIndexerDocument, HttpStatus.OK);
    }

}
