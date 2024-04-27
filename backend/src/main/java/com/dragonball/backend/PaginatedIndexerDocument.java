package com.dragonball.backend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedIndexerDocument {
    private Integer pagesAvailable;
    private Integer currentPage;
    private Integer total;
    private Double elapsedTime;
    private List<IndexerDocument> results;
}
