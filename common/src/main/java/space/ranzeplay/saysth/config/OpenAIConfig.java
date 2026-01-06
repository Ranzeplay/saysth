package space.ranzeplay.saysth.config;

public class OpenAIConfig extends AbstractOpenAICompatibleConfig {
    @Override
    String getChatCompletionEndpoint() {
        return "https://api.openai.com/v1/chat/completions";
    }

    @Override
    String buildAuthCredentials() {
        if (authCredentials == null || authCredentials.trim().isEmpty()) {
            throw new IllegalStateException("Auth credentials cannot be null or empty");
        }
        return authCredentials.startsWith("Bearer ") ? authCredentials : "Bearer " + authCredentials;
    }
}
