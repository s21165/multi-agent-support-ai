package me.ather.support.service;

import com.google.gson.*;
import me.ather.support.model.ChatMessage;
import me.ather.support.model.FunctionCall;
import me.ather.support.model.LLMResponse;

import java.net.URI;
import java.net.http.*;
import java.util.List;

/**
 * Enterprise-grade client for Google Gemini 3 Flash API.
 * Features robust JSON parsing, native Tool Calling, and Semantic Vectorization (RAG).
 */
public class LLMClient {
    private final String apiKey = System.getenv("GEMINI_API_KEY");

    // Using v1beta for early access to Gemini 3 Flash features
    private final String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + apiKey;
    private final String embeddingUrl = "https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent?key=" + apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    /**
     * Generates semantic embeddings using text-embedding-004.
     * @param text The input text to vectorize.
     * @return 768-dimension float array.
     */
    public float[] getEmbedding(String text) {
        try {
            JsonObject payload = new JsonObject();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject textPart = new JsonObject();
            textPart.addProperty("text", text);
            parts.add(textPart);
            content.add("parts", parts);
            payload.add("content", content);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(embeddingUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject json = gson.fromJson(response.body(), JsonObject.class);

            // Safely navigate the embedding response structure
            if (!json.has("embedding")) return new float[0];
            JsonArray values = json.getAsJsonObject("embedding").getAsJsonArray("values");

            float[] vector = new float[values.size()];
            for (int i = 0; i < values.size(); i++) vector[i] = values.get(i).getAsFloat();
            return vector;
        } catch (Exception e) {
            return new float[0];
        }
    }

    /**
     * Sends conversation history to Gemini 3 Flash and parses the multimodal response.
     */
    public LLMResponse getCompletion(List<ChatMessage> messages) {
        try {
            JsonObject payload = preparePayload(messages);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parseResponse(response.body());
        } catch (Exception e) {
            return new LLMResponse("Communication Error: " + e.getMessage(), null);
        }
    }

    /**
     * Constructs the payload with native Tool Calling definitions as requested.
     */
    private JsonObject preparePayload(List<ChatMessage> messages) {
        JsonObject payload = new JsonObject();
        JsonArray contents = new JsonArray();

        for (ChatMessage msg : messages) {
            JsonObject contentObj = new JsonObject();
            // Gemini requires 'model' role for assistant messages
            contentObj.addProperty("role", "assistant".equalsIgnoreCase(msg.role()) ? "model" : "user");
            JsonArray parts = new JsonArray();
            JsonObject textPart = new JsonObject();
            textPart.addProperty("text", msg.content());
            parts.add(textPart);
            contentObj.add("parts", parts);
            contents.add(contentObj);
        }
        payload.add("contents", contents);

        // Native Function Declaration for automated billing tasks
        JsonArray tools = new JsonArray();
        JsonObject functionDeclarations = new JsonObject();
        JsonArray declarations = new JsonArray();

        JsonObject refundTool = new JsonObject();
        refundTool.addProperty("name", "initiateRefund");
        refundTool.addProperty("description", "Automates refund ticket creation based on user justification.");

        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "OBJECT");
        JsonObject properties = new JsonObject();

        JsonObject reasonProp = new JsonObject();
        reasonProp.addProperty("type", "STRING");
        reasonProp.addProperty("description", "The semantic reason extracted from user input.");
        properties.add("reason", reasonProp);

        parameters.add("properties", properties);
        parameters.add("required", gson.toJsonTree(List.of("reason")));

        refundTool.add("parameters", parameters);
        declarations.add(refundTool);
        functionDeclarations.add("function_declarations", declarations);
        tools.add(functionDeclarations);
        payload.add("tools", tools);

        return payload;
    }

    /**
     * Safely parses the Gemini 3 Flash JSON response.
     * Handles both standard text and complex functionCall structures.
     */
    private LLMResponse parseResponse(String body) {
        try {
            JsonObject json = gson.fromJson(body, JsonObject.class);

            // Validation: Ensure candidates exist to avoid NullPointerException
            if (!json.has("candidates") || json.getAsJsonArray("candidates").isEmpty()) {
                return new LLMResponse("Error: No candidates returned from model.", null);
            }

            JsonObject candidate = json.getAsJsonArray("candidates").get(0).getAsJsonObject();
            JsonObject content = candidate.getAsJsonObject("content");
            JsonArray parts = content.getAsJsonArray("parts");

            if (parts == null || parts.isEmpty()) {
                return new LLMResponse("Error: Empty parts in model response.", null);
            }

            JsonObject part = parts.get(0).getAsJsonObject();

            // Handle Native Tool Calling / Function Call
            if (part.has("functionCall")) {
                JsonObject call = part.getAsJsonObject("functionCall");
                String functionName = call.get("name").getAsString();

                // Extract arguments safely
                JsonObject args = call.getAsJsonObject("args");
                String reason = args.has("reason") ? args.get("reason").getAsString() : "No justification provided.";

                return new LLMResponse(null, new FunctionCall(functionName, reason));
            }

            // Handle Standard Text Output
            if (part.has("text")) {
                return new LLMResponse(part.get("text").getAsString(), null);
            }

            return new LLMResponse("Error: Unexpected response format from API.", null);

        } catch (Exception e) {
            return new LLMResponse("Parsing Error: " + e.getMessage(), null);
        }
    }
}