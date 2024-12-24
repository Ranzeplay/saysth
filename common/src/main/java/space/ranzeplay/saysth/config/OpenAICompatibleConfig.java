package space.ranzeplay.saysth.config;

public class OpenAICompatibleConfig extends AbstractOpenAICompatibleConfig {
    String chatCompletionEndpoint;

    @Override
    String getChatCompletionEndpoint() {
        return chatCompletionEndpoint;
    }

    @Override
    String buildAuthCredentials() {
        return authCredentials;
    }
}
