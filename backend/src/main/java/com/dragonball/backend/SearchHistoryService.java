package com.dragonball.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchHistoryService {

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    public void saveKeyword(String keyword) {
        if (!searchHistoryRepository.existsByKeywordIgnoreCase(keyword)) {
            SearchHistory searchHistory = new SearchHistory();
            searchHistory.setKeyword(keyword);
            searchHistoryRepository.save(searchHistory);
        } else {
            SearchHistory s = searchHistoryRepository.findFirstByKeywordIgnoreCase(keyword);
            s.setCount(s.getCount() + 1);
            searchHistoryRepository.save(s);
        }
    }

    public List<SearchHistory> findSuggestions(String partialString) {
        return searchHistoryRepository.findDistinctByKeywordStartingWithIgnoreCaseOrderByCountDesc(partialString);
    }
}