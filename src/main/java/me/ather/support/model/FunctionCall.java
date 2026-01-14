package me.ather.support.model;

/**
 * Represents a structured request from the LLM to execute a specific tool.
 */
public record FunctionCall(String name, String reason) {}