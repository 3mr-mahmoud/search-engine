package com.dragonball.backend;

import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/api/search")
public class SearchController {
    @GetMapping
    public String search(@RequestParam String keyword) {
        return "Hello, "+keyword+"!";
    }
}
