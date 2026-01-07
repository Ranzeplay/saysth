package space.ranzeplay.saysth.chat;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

public class Message {
    public Message(ChatRole role, String content) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
        this.role = role.toString();
        this.content = content;
    }

    @SerializedName("role")
    private final String role;
    @Getter
    @SerializedName("content")
    private String content;

    public ChatRole getRole() {
        return ChatRole.fromString(role);
    }
}
