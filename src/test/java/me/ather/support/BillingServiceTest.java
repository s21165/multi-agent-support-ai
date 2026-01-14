package me.ather.support;

import me.ather.support.service.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BillingService.
 * This suite validates financial policies retrieval and the integrity
 * of the automated refund initiation process.
 */
class BillingServiceTest {

    private BillingService billingService;

    @BeforeEach
    void setUp() {
        billingService = new BillingService();
    }

    /**
     * Ensures that the billing context contains key policy information
     * required for Agent B to function correctly.
     */
    @Test
    @DisplayName("Should return complete billing context with policy details")
    void shouldReturnCompleteBillingContext() {
        // WHEN
        String context = billingService.getBillingContext();

        // THEN
        assertNotNull(context);
        assertTrue(context.contains("REFUND POLICY"), "Context must include refund terms.");
        assertTrue(context.contains("Pro Plan"), "Context must include pricing plans.");
    }

    /**
     * Validates that the pricing information is accessible and contains
     * currency details for the Polish market.
     */
    @Test
    @DisplayName("Should return accurate pricing information")
    void shouldReturnPricingInfo() {
        // WHEN
        String pricing = billingService.getPricingInfo();

        // THEN
        assertNotNull(pricing);
        assertTrue(pricing.contains("PLN"), "Pricing should be presented in PLN.");
        assertTrue(pricing.contains("49"), "Pro Plan price should be correct.");
    }

    /**
     * Validates the refund initiation logic.
     * This test ensures that a unique Ticket ID is generated and the
     * user's justification is correctly passed to the ticket.
     */
    @Test
    @DisplayName("Should generate a valid refund ticket with a unique ID")
    void shouldInitiateRefundWithCorrectFormat() {
        // GIVEN
        String reason = "Accidental purchase";

        // WHEN
        String result = billingService.initiateRefund(reason);

        // THEN
        assertNotNull(result);
        assertTrue(result.contains("REF-"), "The response should contain a generated Ticket ID starting with 'REF-'.");
        assertTrue(result.contains(reason), "The ticket must include the user's provided reason.");
        assertTrue(result.contains("https://"), "The response must include a finalization link.");
    }

    /**
     * Edge Case Test: Refund initiation without a reason.
     * Verifies that the system handles empty inputs gracefully without crashing.
     */
    @Test
    @DisplayName("Should handle null or empty refund reasons gracefully")
    void shouldHandleEmptyRefundReason() {
        // WHEN
        String resultNull = billingService.initiateRefund(null);
        String resultEmpty = billingService.initiateRefund("");

        // THEN
        assertTrue(resultNull.contains("Not specified"), "Should fallback to 'Not specified' for null input.");
        assertTrue(resultEmpty.contains("Not specified"), "Should fallback to 'Not specified' for empty input.");
    }
}