package com.dragonball.backend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "Indexer")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexerModel {
    private ObjectId _id;
    private String word;
    private Integer documentCount;
    private Double rank;
    private List<IndexerDocument> documents;
}


