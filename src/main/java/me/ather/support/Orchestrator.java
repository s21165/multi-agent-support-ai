package me.ather.support;

import me.ather.support.model.ChatMessage;
import me.ather.support.model.FunctionCall;
import me.ather.support.model.LLMResponse;
import me.ather.support.service.BillingService;
import me.ather.support.service.DocumentService;
import me.ather.support.service.LLMClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrator managing the flow between specialized agents.
 * Refactored to address architect's feedback on Tool Calling and Readability.
 */
public class Orchestrator {
    private final LLMClient llmClient = new LLMClient();
    private final DocumentService docService = new DocumentService(llmClient); // Pass client for embeddings
    private final BillingService billingService = new BillingService();
    private final List<ChatMessage> conversationHistory = new ArrayList<>();

    public String handleUserQuery(String userQuery) {
        conversationHistory.add(new ChatMessage("user", userQuery));

        // Step 1: Intent Classification
        String intent = classifyIntent();

        // Step 2: Route to specific Specialist
        LLMResponse response = intent.contains("BILLING") ? callBillingAgent() : callTechnicalAgent();

        // Step 3: Handle Native Function Call (Tool Calling)
        // This is a direct fix for the "missing tool calling mechanism" feedback.
        if (response.functionCall() != null) {
            FunctionCall call = response.functionCall();
            if ("initiateRefund".equals(call.name())) {
                String result = billingService.initiateRefund(call.reason());
                String systemMessage = "[System Action]: " + result;
                conversationHistory.add(new ChatMessage("assistant", systemMessage));
                return systemMessage;
            }
        }

        // Step 4: Normal text response handling
        String output = (response.text() != null) ? response.text() : "I'm sorry, I couldn't process that.";
        conversationHistory.add(new ChatMessage("assistant", output));
        return output;
    }

    private String classifyIntent() {
        String prompt = "Classify user intent as 'TECHNICAL' or 'BILLING'. Output one word only.";
        List<ChatMessage> context = List.of(new ChatMessage("system", prompt), new ChatMessage("user", historyLastMessage()));
        LLMResponse res = llmClient.getCompletion(context);
        return res.text() != null ? res.text().toUpperCase() : "TECHNICAL";
    }

    private LLMResponse callTechnicalAgent() {
        String query = historyLastMessage();
        String context = docService.findRelevantContext(query);
        String systemPrompt = "You are a Technical Specialist. Use provided docs only: " + context;
        return execute(systemPrompt);
    }

    private LLMResponse callBillingAgent() {
        String systemPrompt = "You are a Billing Specialist. Use the 'initiateRefund' tool if criteria are met.";
        return execute(systemPrompt);
    }

    private LLMResponse execute(String systemInstruction) {
        List<ChatMessage> fullContext = new ArrayList<>();
        fullContext.add(new ChatMessage("system", systemInstruction));
        fullContext.addAll(conversationHistory);
        return llmClient.getCompletion(fullContext);
    }

    private String historyLastMessage() {
        return conversationHistory.get(conversationHistory.size() - 1).content();
    }
}