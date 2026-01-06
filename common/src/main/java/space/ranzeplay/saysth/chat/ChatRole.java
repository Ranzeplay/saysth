package space.ranzeplay.saysth.chat;

public enum ChatRole {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant");

    private final String name;

    ChatRole(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static ChatRole fromString(String value) {
        for (ChatRole role : ChatRole.values()) {
            if (role.name.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No enum constant for: " + value);
    }
}
