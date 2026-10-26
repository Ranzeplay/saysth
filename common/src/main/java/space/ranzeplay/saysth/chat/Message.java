package space.ranzeplay.saysth.chat;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.stream.Collectors;

public class Message {
    public Message(ChatRole role, String content) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        this.role = role.toString();
        this.content = content;
        this.toolCalls = null;
        this.toolCallId = null;
    }

    private Message(String role, String content, List<JsonObject> toolCalls, String toolCallId) {
        this.role = role;
        this.content = content;
        this.toolCalls = toolCalls;
        this.toolCallId = toolCallId;
    }

    public static Message assistantToolCalls(List<ToolCall> toolCalls) {
        if (toolCalls == null || toolCalls.isEmpty()) {
            throw new IllegalArgumentException("Tool calls cannot be null or empty");
        }
        return new Message(ChatRole.ASSISTANT.toString(), null,
                toolCalls.stream().map(ToolCall::toJson).collect(Collectors.toList()), null);
    }

    public static Message toolResult(ToolCall toolCall, String content) {
        if (toolCall == null) {
            throw new IllegalArgumentException("Tool call cannot be null");
        }
        if (content == null) {
            throw new IllegalArgumentException("Tool result cannot be null");
        }
        return new Message(ChatRole.TOOL.toString(), content, null, toolCall.id());
    }

    @SerializedName("role")
    private final String role;
    @Getter
    @SerializedName("content")
    private String content;
    @SerializedName("tool_calls")
    private final List<JsonObject> toolCalls;
    @SerializedName("tool_call_id")
    private final String toolCallId;

    public ChatRole getRole() {
        return ChatRole.fromString(role);
    }
}
