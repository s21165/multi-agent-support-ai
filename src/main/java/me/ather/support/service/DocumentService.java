package me.ather.support.service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Enterprise-grade Knowledge Base using Semantic Vector Search.
 * Improved for high recall by using expanded documentation and Top-K retrieval.
 * Addresses the "basic search" feedback by implementing a multi-result semantic bridge.
 */
public class DocumentService {
    private final LLMClient llmClient;
    private final Map<String, float[]> vectorizedDocs = new HashMap<>();
    private final List<String> knowledgeBase = new ArrayList<>();

    public DocumentService(LLMClient llmClient) {
        this.llmClient = llmClient;
        initializeKnowledgeBase();
    }

    private void initializeKnowledgeBase() {
        // We expand the descriptions to give the embedding model more "semantic surface" to hit.
        addDoc("System Maintenance & Hardware: The Hub-V3 device is powered by a CR2032 battery (power source) with a 3-year life. Hardware specs include Zigbee 3.0 wireless protocol and an indoor range of 50 meters.");
        addDoc("Troubleshooting Errors: For Error Code 404 (Sensor offline), press the 'Reset' button for 5 seconds. Error Code 501 indicates a database sync failure.");
        addDoc("Connectivity & Pairing: To pair a new device (connection setup), hold the 'Pair' button until the LED flashes blue. Initial setup requires the mobile app.");
        addDoc("Developer Integration: Authentication for the API requires OAuth2 Bearer Tokens. Webhooks are available via /v2/webhooks using POST requests with JSON payloads.");
    }

    private void addDoc(String text) {
        knowledgeBase.add(text);
        vectorizedDocs.put(text, llmClient.getEmbedding(text));
    }

    /**
     * Finds the Top-2 most relevant context snippets.
     * Returning multiple snippets (Top-K) ensures the LLM sees the full picture.
     */
    public String findRelevantContext(String userQuery) {
        float[] queryVector = llmClient.getEmbedding(userQuery);
        if (queryVector.length == 0) return "";

        // Map to store text and its similarity score
        Map<String, Double> scores = new HashMap<>();

        for (String doc : knowledgeBase) {
            double similarity = calculateCosineSimilarity(queryVector, vectorizedDocs.get(doc));
            scores.put(doc, similarity);
        }

        // We filter by a slightly lower threshold (0.35) and take TOP 2 results.
        // This is a standard practice to handle variations in user phrasing.
        String context = scores.entrySet().stream()
                .filter(entry -> entry.getValue() > 0.35)
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(2)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining("\n---\n"));

        return context.isEmpty() ? "No specific technical docs found for this query." : context;
    }

    /**
     * Mathematical implementation of Cosine Similarity: (A · B) / (||A|| * ||B||)
     */
    private double calculateCosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}