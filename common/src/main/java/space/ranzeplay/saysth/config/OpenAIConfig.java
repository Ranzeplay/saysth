package space.ranzeplay.saysth.config;

public class OpenAIConfig extends AbstractOpenAICompatibleConfig {
    @Override
    String getChatCompletionEndpoint() {
        return "https://api.openai.com/v1/chat/completions";
    }
}
