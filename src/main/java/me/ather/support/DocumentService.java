package me.ather.support;

import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for providing technical context from local documentation.
 * Acts as a simple Knowledge Base for Agent A.
 */
public class DocumentService {
    private final Map<String, String> knowledgeBase = new HashMap<>();

    public DocumentService() {
        // Technical fault references
        knowledgeBase.put("error", "Error Code 404: Sensor offline. Solution: Press 'Reset' for 5s. Error Code 501: Database sync failure.");

        // Integration and connectivity protocols
        knowledgeBase.put("api", "Webhooks are available via /v2/webhooks. Authentication requires an OAuth2 Bearer Token.");
        knowledgeBase.put("webhook", "Webhooks send POST requests with JSON payload to your configured endpoint.");

        // Hardware and environmental specs (including power source)
        knowledgeBase.put("specs", "The Hub-V3 uses Zigbee 3.0 protocol. Range: 50m indoors. Battery: CR2032 (3-year life).");

        // Provisioning and pairing procedures
        knowledgeBase.put("setup", "To pair a new device, hold the 'Pair' button until the LED flashes blue. Initial setup requires the mobile app.");
        knowledgeBase.put("pair", "Pairing mode is active for 60 seconds. If the LED stops flashing, press the button again.");
    }

    /**
     * Scans the user query for keywords and returns matching documentation snippets.
     * * @param userQuery The raw input from the user.
     * @return String containing relevant context or a default message if no match is found.
     */
    public String findRelevantContext(String userQuery) {
        StringBuilder context = new StringBuilder();
        String queryLower = userQuery.toLowerCase();

        // Mapping domain-specific terminology to knowledge base keys
        Map<String, String[]> keywordIndex = new HashMap<>();
        keywordIndex.put("error", new String[]{"error", "404", "501", "failure", "code", "database", "sync"});
        keywordIndex.put("specs", new String[]{"specs", "range", "distance", "zigbee", "meters", "battery", "power", "cr2032", "life"});
        keywordIndex.put("setup", new String[]{"setup", "install", "led", "flashing", "blue", "mobile app"});
        keywordIndex.put("pair", new String[]{"pair", "pairing", "button", "connect"});
        keywordIndex.put("api", new String[]{"api", "token", "auth", "integration", "bearer"});
        keywordIndex.put("webhook", new String[]{"webhook", "endpoint", "post", "payload"});

        for (Map.Entry<String, String[]> entry : keywordIndex.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (queryLower.contains(keyword)) {
                    String info = knowledgeBase.get(entry.getKey());
                    // Ensure unique snippets are added to context
                    if (info != null && !context.toString().contains(info)) {
                        context.append("- ").append(info).append("\n");
                    }
                    break;
                }
            }
        }

        return !context.isEmpty()
                ? context.toString().trim()
                : "No specific documentation found for this query. Ask the user for more technical details or an error code.";
    }
}