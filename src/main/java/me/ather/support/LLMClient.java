package me.ather.support;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Client for interacting with the Google Gemini API.
 */
public class LLMClient {
    // API handled via environment variables
    private final String apiKey = System.getenv("GEMINI_API_KEY");

    // Endpoint for Gemini 3 Flash Preview via v1beta
    private final String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + apiKey;

    private final HttpClient httpClient;
    private final Gson gson;

    public LLMClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("FATAL ERROR: Environment variable 'GEMINI_API_KEY' is missing!");
            System.exit(1);
        }
    }

    /**
     * Sends a list of messages to the model and returns the generated response.
     */
    public String getCompletion(List<ChatMessage> messages) {
        try {
            JsonObject payload = buildPayload(messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("DEBUG - Status: " + response.statusCode());
                System.out.println("DEBUG - Body: " + response.body());
                return "API ERROR: " + response.statusCode();
            }

            return extractTextFromResponse(response.body());

        } catch (Exception e) {
            return "CONNECTION ERROR: " + e.getMessage();
        }
    }

    /**
     * Constructs the JSON payload required by the Gemini API.
     * Manages role mapping and system instruction injection.
     */
    private JsonObject buildPayload(List<ChatMessage> messages) {
        JsonObject payload = new JsonObject();
        JsonArray contents = new JsonArray();

        String systemInstruction = "";

        for (ChatMessage msg : messages) {
            if ("system".equalsIgnoreCase(msg.role())) {
                systemInstruction = msg.content();
                continue;
            }

            JsonObject contentObj = new JsonObject();
            // Gemini API expects 'model' instead of 'assistant'
            String role = "assistant".equalsIgnoreCase(msg.role()) ? "model" : "user";
            contentObj.addProperty("role", role);

            JsonArray parts = new JsonArray();
            JsonObject textPart = new JsonObject();

            String text = msg.content();
            // Injecting system instruction into the first user message for context stability
            if ("user".equals(role) && !systemInstruction.isEmpty()) {
                text = "SYSTEM INSTRUCTION: " + systemInstruction + "\n\nUSER QUERY: " + text;
                systemInstruction = "";
            }

            textPart.addProperty("text", text);
            parts.add(textPart);
            contentObj.add("parts", parts);
            contents.add(contentObj);
        }

        payload.add("contents", contents);
        return payload;
    }

    private String extractTextFromResponse(String responseBody) {
        try {
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            return jsonResponse.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (Exception e) {
            return "RESPONSE ERROR: Failed to parse content from API response.";
        }
    }
}