package com.triageagent.s3docs.service;

import com.triageagent.s3docs.s3.S3DocumentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {

    private final S3DocumentRepository repo;
    private final ObjectMapper mapper;

    public DocumentService(S3DocumentRepository repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public String upsert(String key, JsonNode doc) {
        try {
            byte[] json = mapper.writeValueAsBytes(doc);
            return repo.putJson(key, json);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON payload", e);
        }
    }

    public byte[] get(String key) {
        return repo.getJson(key);
    }

    public void delete(String key) {
        repo.delete(key);
    }

    public S3DocumentRepository.Page list(String startsWith, Integer maxKeys, String continuationToken) {
        return repo.list(startsWith, maxKeys, continuationToken);
    }
}
