package me.ather.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class DocumentServiceTest {

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService();
    }

    @Test
    @DisplayName("Should retrieve technical help for 404 error code")
    void shouldReturnErrorInstructions() {
        // Technical lookup for error code patterns
        String result = documentService.findRelevantContext("I have a 404 error");

        assertNotNull(result);
        assertTrue(result.contains("Reset' for 5s"), "Result should contain the 404 fix");
    }

    @Test
    @DisplayName("Should retrieve API details when webhooks are mentioned")
    void shouldReturnWebhookInfo() {
        // Validation of keyword-based retrieval for API topics
        String result = documentService.findRelevantContext("Tell me about webhooks");

        assertNotNull(result);
        assertTrue(result.contains("POST requests"), "Result should explain webhook mechanics");
    }

    @Test
    @DisplayName("Should return specific fallback message for unknown topics")
    void shouldHandleUnknownKeywords() {
        // Verification of fallback behavior for out-of-scope queries
        String result = documentService.findRelevantContext("How is the weather?");

        assertTrue(result.contains("No specific documentation found"), "Should use the defined fallback string");
    }

    @Test
    @DisplayName("Should combine multiple snippets for complex queries")
    void shouldAggregateContext() {
        // Multi-source context aggregation test
        String result = documentService.findRelevantContext("battery install");

        assertTrue(result.contains("CR2032") && result.contains("mobile app"),
                "Should aggregate both Hardware Specs and Setup info");
    }
}