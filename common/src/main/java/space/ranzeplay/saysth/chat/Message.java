package space.ranzeplay.saysth.chat;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

public class Message {
    public Message(ChatRole role, String content) {
        this.role = role.toString();
        this.content = content;
    }

    @SerializedName("role")
    private final String role;
    @Getter
    @SerializedName("content")
    private String content;

    public ChatRole getRole() {
        return ChatRole.valueOf(role);
    }
}
