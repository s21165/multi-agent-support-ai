package me.ather.support.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Defines the JSON structures for Gemini API tools and declarations.
 */
public class GeminiSchema {

    /**
     * Returns the tool definition for refund processing.
     * Addressing "missing tool calling mechanism" feedback.
     */
    public static JsonArray getRefundTools() {
        JsonObject refundTool = new JsonObject();
        refundTool.addProperty("name", "initiateRefund");
        refundTool.addProperty("description", "Starts the refund process for the user.");

        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "OBJECT");

        JsonObject properties = new JsonObject();
        JsonObject reasonProp = new JsonObject();
        reasonProp.addProperty("type", "STRING");
        reasonProp.addProperty("description", "Customer's reason for the refund.");
        properties.add("reason", reasonProp);

        parameters.add("properties", properties);
        parameters.add("required", createRequiredList("reason"));
        refundTool.add("parameters", parameters);

        JsonArray declarations = new JsonArray();
        declarations.add(refundTool);

        JsonObject toolWrapper = new JsonObject();
        toolWrapper.add("function_declarations", declarations);

        JsonArray tools = new JsonArray();
        tools.add(toolWrapper);
        return tools;
    }

    private static JsonArray createRequiredList(String... fields) {
        JsonArray required = new JsonArray();
        for (String f : fields) required.add(f);
        return required;
    }
}