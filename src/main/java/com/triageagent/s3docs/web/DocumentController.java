package com.triageagent.s3docs.web;

import com.triageagent.s3docs.service.DocumentService;
import com.triageagent.s3docs.s3.S3DocumentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/docs")
public class DocumentController {

    // Simple “safe key” constraint for S3 object names.
    private static final String KEY_REGEX = "^[a-zA-Z0-9._-]{1,128}$";

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PutMapping(value = "/{key}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> upsert(
            @PathVariable @Pattern(regexp = KEY_REGEX, message = "Invalid key") String key,
            @RequestBody JsonNode body
    ) {
        String eTag = service.upsert(key, body);
        return ResponseEntity.noContent()
                .header("ETag", eTag == null ? "" : eTag)
                .build();
    }

    @GetMapping(value = "/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> get(
            @PathVariable @Pattern(regexp = KEY_REGEX, message = "Invalid key") String key
    ) {
        byte[] json = service.get(key);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(
            @PathVariable @Pattern(regexp = KEY_REGEX, message = "Invalid key") String key
    ) {
        service.delete(key);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public Map<String, Object> list(
            @RequestParam(required = false) String startsWith,
            @RequestParam(required = false) Integer maxKeys,
            @RequestParam(required = false) String continuationToken
    ) {
        var page = service.list(startsWith, maxKeys, continuationToken);

        Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("items", page.items());
        resp.put("nextContinuationToken", page.nextContinuationToken()); // can be null (OK)
        return resp;
    }

}

