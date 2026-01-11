package me.ather.support;

/**
 * Represents a single message within a conversation.
 * Supported roles: "system", "user", "assistant".
 */
public record ChatMessage(String role, String content) {
    // Standard immutable record for message data
}