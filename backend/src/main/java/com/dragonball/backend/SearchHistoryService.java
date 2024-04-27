package com.dragonball.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchHistoryService {

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    public void saveKeyword(String keyword) {
        if (!searchHistoryRepository.existsByKeyword(keyword)) {
            SearchHistory searchHistory = new SearchHistory();
            searchHistory.setKeyword(keyword);
            searchHistoryRepository.save(searchHistory);
        }
    }

    public List<SearchHistory> findSuggestions(String partialString) {
        return searchHistoryRepository.findDistinctByKeywordStartingWithIgnoreCaseOrderByIdDesc(partialString);
    }
}