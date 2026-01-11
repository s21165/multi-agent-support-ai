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
        // Matches your logic: '404' triggers the 'error' key in knowledgeBase
        String result = documentService.findRelevantContext("I have a 404 error");

        assertNotNull(result);
        // Your service adds a "- " prefix to snippets
        assertTrue(result.contains("Reset' for 5s"), "Result should contain the 404 fix");
    }

    @Test
    @DisplayName("Should retrieve API details when webhooks are mentioned")
    void shouldReturnWebhookInfo() {
        // Matches your logic: 'webhook' triggers the 'webhook' key
        String result = documentService.findRelevantContext("Tell me about webhooks");

        assertNotNull(result);
        assertTrue(result.contains("POST requests"), "Result should explain webhook mechanics");
    }

    @Test
    @DisplayName("Should return specific fallback message for unknown topics")
    void shouldHandleUnknownKeywords() {
        // Testing the "No specific documentation found" response
        String result = documentService.findRelevantContext("How is the weather?");

        assertTrue(result.contains("No specific documentation found"), "Should use the defined fallback string");
    }

    @Test
    @DisplayName("Should combine multiple snippets for complex queries")
    void shouldAggregateContext() {
        // Testing your StringBuilder loop with two keywords: 'battery' (specs) and 'install' (setup)
        String result = documentService.findRelevantContext("battery install");

        assertTrue(result.contains("CR2032") && result.contains("mobile app"),
                "Should aggregate both Hardware Specs and Setup info");
    }
}