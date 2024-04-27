package com.dragonball.backend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "searchHistory")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchHistory {
    @Id
    private String id;
    private String keyword;

    // Constructors, getters, setters...
}
