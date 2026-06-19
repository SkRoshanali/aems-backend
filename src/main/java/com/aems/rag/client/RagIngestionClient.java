package com.aems.rag.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client for communicating with Python RAG service
 * Handles both document ingestion and RAG queries
 */
@Service
public class RagIngestionClient {
    
    @Value("${rag.service.url}")
    private String ragServiceUrl;

    @Value("${rag.internal.secret}")
    private String ragInternalSecret;
    
    private final RestTemplate restTemplate;
    
    public RagIngestionClient(RestTemplateBuilder builder) {
        // Configure timeouts for Render free tier cold starts
        this.restTemplate = builder
            .setConnectTimeout(java.time.Duration.ofSeconds(10))  // Connection timeout
            .setReadTimeout(java.time.Duration.ofSeconds(35))      // Read timeout (cold start can take 30s)
            .build();
    }
    
    /**
     * Ingest a document into the RAG vector database
     * 
     * @param content The text content to embed and store
     * @param metadata Key-value pairs for filtering (visibility, buyer_id, etc.)
     * @return true if successful, false otherwise
     */
    public boolean ingestDocument(String content, Map<String, String> metadata) {
        try {
            String url = ragServiceUrl + "/api/ingest/document";
            
            HttpHeaders headers = createJsonHeaders();
            
            Map<String, Object> request = new HashMap<>();
            request.put("content", content);
            request.put("metadata", metadata);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            // Log error but don't fail the business transaction
            System.err.println("Failed to ingest document to RAG: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Query RAG system with role-based filtering
     * 
     * SECURITY: Role is passed from Spring Boot (server-verified from JWT)
     * 
     * @param query User's question
     * @param role User's role (from JWT)
     * @param buyerId Buyer ID (if role is BUYER)
     * @param buyerStatus Buyer status (PENDING/ACCEPTED)
     * @return Map with answer and sources
     */
    public Map<String, Object> queryRag(String query, String role, String buyerId, String buyerStatus) {
        try {
            String url = ragServiceUrl + "/api/rag/query";
            
            HttpHeaders headers = createJsonHeaders();
            
            Map<String, Object> request = new HashMap<>();
            request.put("query", query);
            request.put("role", role);
            
            if ("BUYER".equals(role) && buyerId != null) {
                request.put("buyer_id", buyerId);
                request.put("buyer_status", buyerStatus);
            }
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new RuntimeException("RAG service returned error: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.err.println("Failed to query RAG: " + e.getMessage());
            throw new RuntimeException("RAG query failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Batch ingest multiple documents (for bulk operations)
     */
    public boolean ingestBatch(java.util.List<DocumentIngestionRequest> documents) {
        try {
            String url = ragServiceUrl + "/api/ingest/batch";
            
            HttpHeaders headers = createJsonHeaders();
            
            Map<String, Object> request = new HashMap<>();
            request.put("documents", documents);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            System.err.println("Failed to batch ingest documents to RAG: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete documents by metadata filter (e.g., when buyer is deleted)
     */
    public boolean deleteByMetadata(Map<String, String> metadata) {
        try {
            String url = ragServiceUrl + "/api/ingest/delete";
            
            HttpHeaders headers = createJsonHeaders();
            
            Map<String, Object> request = new HashMap<>();
            request.put("metadata", metadata);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.DELETE, entity, Map.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            System.err.println("Failed to delete documents from RAG: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Health check - ping Python RAG service
     * Used by warmup endpoint to wake Python service
     * 
     * Returns quickly, doesn't wait for full response
     */
    public void healthCheck() {
        try {
            String url = ragServiceUrl + "/health";
            restTemplate.getForEntity(url, Map.class);
        } catch (Exception e) {
            // Ignore - the request itself wakes the service
        }
    }

    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Secret", ragInternalSecret);
        return headers;
    }
    
    // Helper class for batch operations
    public static class DocumentIngestionRequest {
        private String content;
        private Map<String, String> metadata;
        
        public DocumentIngestionRequest(String content, Map<String, String> metadata) {
            this.content = content;
            this.metadata = metadata;
        }
        
        // Getters and setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Map<String, String> getMetadata() { return metadata; }
        public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    }
}
