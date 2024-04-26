package com.dragonball.backend;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import javax.management.Query;
import java.util.List;
@CrossOrigin(origins = "http://127.0.0.1:5173")
@RestController

@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private QueryProcessor queryProcessor;
    @GetMapping
    public ResponseEntity<List<IndexerDocument>> search(@RequestParam String keyword) {
        List<IndexerDocument> results = queryProcessor.searchly(keyword);
        return new ResponseEntity<List<IndexerDocument>>(results, HttpStatus.OK);
    }
}
