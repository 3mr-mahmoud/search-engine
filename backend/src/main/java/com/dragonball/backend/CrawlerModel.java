package com.dragonball.backend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Crawler")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrawlerModel {
    @Id
    private Integer id;
    private String Page;
    private String URL;
    private Double rank;
    private String title;
}