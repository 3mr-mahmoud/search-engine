package com.dragonball.backend;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndexerRepository extends MongoRepository<IndexerModel, ObjectId> {
    List<IndexerModel> findByWord(String word);
}