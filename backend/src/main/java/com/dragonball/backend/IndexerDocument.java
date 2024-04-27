package com.dragonball.backend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexerDocument {
    private String url;
    private Double tf;
    private Double rank;
    private List<String> statements;
    private  Boolean inHead;
    private  Boolean inTitle;
    private  Integer count;
    private Double tfIdf;
    private String title;
}
