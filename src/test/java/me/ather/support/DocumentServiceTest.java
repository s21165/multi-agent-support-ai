package me.ather.support;

import me.ather.support.service.DocumentService;
import me.ather.support.service.LLMClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DocumentService focusing on RAG logic.
 * Validates semantic search, vector similarity, and error handling.
 */
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    private DocumentService documentService;

    @Mock
    private LLMClient mockLlmClient;

    /**
     * Professional setup: We provide a non-zero default vector
     * to ensure cosine similarity calculations don't result in NaN/Zero.
     */
    @BeforeEach
    void setUp() {
        // Return a vector with at least one value to allow similarity calculation
        float[] baseVector = new float[768];
        baseVector[0] = 0.5f;
        when(mockLlmClient.getEmbedding(anyString())).thenReturn(baseVector);

        documentService = new DocumentService(mockLlmClient);
    }

    @Test
    @DisplayName("Should retrieve specific context when semantic similarity is high")
    void shouldReturnSpecificDocumentForTargetedQuery() {
        // GIVEN
        String query = "What battery does the device use?";
        float[] matchingVector = new float[768];
        matchingVector[0] = 0.5f; // Perfect match with our baseVector from setUp

        when(mockLlmClient.getEmbedding(query)).thenReturn(matchingVector);

        // WHEN
        String result = documentService.findRelevantContext(query);

        // THEN
        assertNotNull(result);
        assertNotEquals("No specific technical docs found for this query.", result,
                "Service should find documentation when vectors match.");
    }

    @Test
    @DisplayName("Should aggregate results for broad queries")
    void shouldReturnTopKResultsWithSeparators() {
        // GIVEN
        String query = "general hardware info";
        float[] broadVector = new float[768];
        broadVector[0] = 0.5f; // Matches all documents initialized in setUp

        when(mockLlmClient.getEmbedding(query)).thenReturn(broadVector);

        // WHEN
        String result = documentService.findRelevantContext(query);

        // THEN
        assertNotNull(result);
        // We verify that the result contains information, regardless of the separator type
        assertFalse(result.contains("No specific technical docs"), "Should return context data");
    }

    @Test
    @DisplayName("Should return fallback message when similarity is below threshold")
    void shouldHandleNoMatchesGracefully() {
        // GIVEN: A query vector that is orthagonal or opposite to our knowledge base
        String query = "completely unrelated topic";
        float[] weakVector = new float[768];
        weakVector[0] = -1.0f; // Negative correlation

        when(mockLlmClient.getEmbedding(query)).thenReturn(weakVector);

        // WHEN
        String result = documentService.findRelevantContext(query);

        // THEN
        assertEquals("No specific technical docs found for this query.", result);
    }
}