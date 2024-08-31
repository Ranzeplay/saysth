package space.ranzeplay.saysth.chat;

import lombok.Getter;

public class Message {
    public Message(ChatRole role, String content) {
        this.role = role.toString();
        this.content = content;
    }

    private final String role;
    @Getter
    private String content;

    public ChatRole getRole() {
        return ChatRole.valueOf(role);
    }
}
