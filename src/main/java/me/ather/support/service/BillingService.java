package me.ather.support.service;

/**
 * Service handling billing policies, pricing, and refund processes.
 * Acts as the primary data source for Agent B (Billing Specialist).
 */
public class BillingService {

    /**
     * Provides the main policy context for the LLM.
     * This is injected into the system prompt to ensure the Agent follows company rules.
     */
    public String getBillingContext() {
        return "REFUND POLICY: Full refunds allowed within 14 days of purchase. Processing takes 3-5 business days.\n" +
                "PRICING PLANS: \n" +
                "- Basic Plan: 0 PLN/mo (Limited features)\n" +
                "- Pro Plan: 49 PLN/mo (All features included)\n" +
                "- Enterprise: Custom pricing for large teams.\n" +
                "REFUND PROCEDURE: If a refund is requested, inform the user a ticket is being opened and provide a support link.";
    }

    /**
     * Logic for pricing information retrieval.
     */
    public String getPricingInfo() {
        return "Available plans: Basic (0 PLN), Pro (49 PLN/month), Enterprise (custom). All plans include 24/7 basic support.";
    }

    /**
     * Logic for refund policy details.
     */
    public String getRefundPolicy() {
        return "Refund Policy: You can request a full refund within 14 days of purchase. " +
                "Processing time is 3-5 business days depending on your bank.";
    }

    /**
     * Action: Initiates a refund ticket in the system.
     * @param reason The reason provided by the user.
     * @return A message containing the ticket ID and final instructions.
     */
    public String initiateRefund(String reason) {
        String ticketId = "REF-" + (int)(Math.random() * 10000);
        return "I have initiated a refund request (Ticket ID: " + ticketId + "). " +
                "Reason provided: " + (reason == null || reason.isEmpty() ? "Not specified" : reason) + ". " +
                "Please finalize the process here: https://support.example.com/refund-form";
    }
}