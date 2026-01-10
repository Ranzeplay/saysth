package space.ranzeplay.saysth.config;

public class OpenAICompatibleConfig extends AbstractOpenAICompatibleConfig {
    String chatCompletionEndpoint;

    @Override
    String getChatCompletionEndpoint() {
        if (chatCompletionEndpoint == null || chatCompletionEndpoint.trim().isEmpty()) {
            throw new IllegalStateException("Chat completion endpoint cannot be null or empty");
        }
        return chatCompletionEndpoint;
    }

    @Override
    String buildAuthCredentials() {
        if (authCredentials == null) {
            return "";
        }
        return authCredentials;
    }
}
