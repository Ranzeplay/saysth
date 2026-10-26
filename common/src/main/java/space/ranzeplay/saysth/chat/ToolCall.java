package space.ranzeplay.saysth.chat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public record ToolCall(String id, String name, JsonObject arguments) {
    public ToolCall {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Tool call ID cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tool name cannot be null or blank");
        }
        if (arguments == null) {
            throw new IllegalArgumentException("Tool arguments cannot be null");
        }
    }

    public static ToolCall fromJson(JsonObject json) {
        if (json == null || !json.has("id") || !json.has("function")) {
            throw new IllegalArgumentException("Invalid tool call: missing id or function");
        }

        var function = json.getAsJsonObject("function");
        if (!function.has("name")) {
            throw new IllegalArgumentException("Invalid tool call: missing function name");
        }

        var arguments = new JsonObject();
        if (function.has("arguments") && !function.get("arguments").isJsonNull()) {
            var rawArguments = function.get("arguments");
            arguments = rawArguments.isJsonObject()
                    ? rawArguments.getAsJsonObject()
                    : JsonParser.parseString(rawArguments.getAsString()).getAsJsonObject();
        }
        return new ToolCall(json.get("id").getAsString(), function.get("name").getAsString(), arguments);
    }

    public JsonObject toJson() {
        var function = new JsonObject();
        function.addProperty("name", name);
        function.addProperty("arguments", arguments.toString());

        var json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("type", "function");
        json.add("function", function);
        return json;
    }
}
