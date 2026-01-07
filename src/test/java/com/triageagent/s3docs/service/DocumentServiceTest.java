package com.triageagent.s3docs.service;

import com.triageagent.s3docs.s3.S3DocumentRepository;
import com.triageagent.s3docs.web.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentServiceTest {

    @Test
    void upsert_serializesJson_andCallsRepo() {
        S3DocumentRepository repo = mock(S3DocumentRepository.class);
        DocumentService service = new DocumentService(repo, new ObjectMapper());

        ObjectNode body = new ObjectMapper().createObjectNode().put("hello", "world");
        when(repo.putJson(eq("abc"), any())).thenReturn("etag123");

        String etag = service.upsert("abc", body);

        assertEquals("etag123", etag);
        verify(repo).putJson(eq("abc"), any());
    }

    @Test
    void get_propagatesNotFound() {
        S3DocumentRepository repo = mock(S3DocumentRepository.class);
        DocumentService service = new DocumentService(repo, new ObjectMapper());

        when(repo.getJson("missing")).thenThrow(new NotFoundException("Document not found: missing"));

        assertThrows(NotFoundException.class, () -> service.get("missing"));
    }
}
