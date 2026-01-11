package me.ather.support;

import java.util.ArrayList;
import java.util.List;

/**
 * The heart of the system. Manages agent selection and ensures factual responses.
 */
public class Orchestrator {
    private final LLMClient llmClient = new LLMClient();
    private final DocumentService docService = new DocumentService();
    private final BillingService billingService = new BillingService();
    private final List<ChatMessage> conversationHistory = new ArrayList<>();

    public String handleUserQuery(String userQuery) {
        conversationHistory.add(new ChatMessage("user", userQuery));

        String intent = classifyIntent();
        String response;

        if (intent.contains("TECHNICAL")) {
            response = callTechnicalAgent();
        } else if (intent.contains("BILLING")) {
            response = callBillingAgent();

            // INTERCEPTOR: Logic to execute actual backend methods based on AI response
            if (response.contains("[ACTION:INITIATE_REFUND]")) {
                String refundTicket = billingService.initiateRefund("Automated refund request");
                response += "\n\n[System Update: " + refundTicket + "]";
            }
        } else {
            response = callGeneralAgent();
        }

        conversationHistory.add(new ChatMessage("assistant", response));
        return response;
    }

    private String classifyIntent() {
        // High-priority instruction to ensure proper routing
        String prompt = "Review the entire conversation. Classify the user's latest message intent as: " +
                "'TECHNICAL' (for hardware, errors, API, setup), " +
                "'BILLING' (for payments, refunds, pricing), or 'OTHER'. " +
                "Output ONLY the word.";

        List<ChatMessage> context = new ArrayList<>(conversationHistory);
        context.add(0, new ChatMessage("system", prompt));

        return llmClient.getCompletion(context).toUpperCase();
    }

    private String callTechnicalAgent() {
        // Retrieve the last user query to find relevant documentation
        String lastQuery = conversationHistory.get(conversationHistory.size() - 1).content();
        String context = docService.findRelevantContext(lastQuery);

        // STRICT PROMPT: Force the model to use the provided snippets or admit lack of knowledge.
        String systemPrompt = "You are Agent A - Technical Specialist. " +
                "STRICT RULES:\n" +
                "1. Answer ONLY using the PROVIDED DOCUMENTATION below.\n" +
                "2. If the documentation does not contain the answer, say: 'I'm sorry, I don't have information on this in my records.'\n" +
                "3. DO NOT use your own knowledge to troubleshoot (e.g., do not suggest incognito mode if not in docs).\n" +
                "4. Be concise and factual.\n\n" +
                "DOCUMENTATION SNIPPETS:\n" + context;

        return executeAgent(systemPrompt);
    }

    private String callBillingAgent() {
        String billingData = billingService.getBillingContext();

        // Define specific capabilities as requested in the task
        String systemPrompt = "You are Agent B - Billing Specialist.\n" +
                "CAPABILITIES: [PRICING_INFO, REFUND_POLICY, INITIATE_REFUND].\n\n" +
                "KNOWLEDGE BASE:\n" + billingData + "\n\n" +
                "INSTRUCTIONS:\n" +
                "1. For pricing, quote exact values from the Knowledge Base.\n" +
                "2. For refunds, check the 14-day policy. If eligible, provide the support link and " +
                "MUST include the tag [ACTION:INITIATE_REFUND] for the system logs.\n" +
                "3. Do not offer discounts not mentioned in the context.";

        return executeAgent(systemPrompt);
    }

    private String callGeneralAgent() {
        String systemPrompt = "You are a helpful general support assistant. " +
                "If the user asks technical or billing questions, guide them to provide details so you can transfer them to a specialist.";
        return executeAgent(systemPrompt);
    }

    private String executeAgent(String systemInstruction) {
        // We create a fresh list for the API call including the system instruction
        List<ChatMessage> fullContext = new ArrayList<>();
        fullContext.add(new ChatMessage("system", systemInstruction));
        fullContext.addAll(conversationHistory);

        return llmClient.getCompletion(fullContext);
    }
}