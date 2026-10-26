package space.ranzeplay.saysth.chat;

import java.util.List;

public record ToolResponse(String content, List<ToolCall> toolCalls) {
    public ToolResponse {
        if (toolCalls == null) {
            throw new IllegalArgumentException("Tool calls cannot be null");
        }
    }

    public boolean hasToolCalls() {
        return !toolCalls.isEmpty();
    }
}
