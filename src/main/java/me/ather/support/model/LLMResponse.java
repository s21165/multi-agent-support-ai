package me.ather.support.model;

/**
 * Representing the unified response from Gemini 3 Flash.
 */
public record LLMResponse(String text, FunctionCall functionCall) {}