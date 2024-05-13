package com.dragonball.backend;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchHistoryRepository extends MongoRepository<SearchHistory, String> {

    // Custom query method to check if a keyword exists
    boolean existsByKeywordIgnoreCase(String keyword);

    // Custom query method to find distinct keywords starting with a partial string (case-insensitive)
    @Query(fields = "{ 'keyword' : 1 ,'count': 1}")
    List<SearchHistory> findDistinctByKeywordStartingWithIgnoreCaseOrderByCountDesc(String partialString);

    SearchHistory findFirstByKeywordIgnoreCase(String keyword);
}