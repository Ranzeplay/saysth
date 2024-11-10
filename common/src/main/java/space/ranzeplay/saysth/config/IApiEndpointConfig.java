package space.ranzeplay.saysth.config;

import space.ranzeplay.saysth.chat.Conversation;

import java.net.http.HttpRequest;
import java.util.Optional;

public interface IApiEndpointConfig {
    HttpRequest.Builder getPartialHttpRequest();
    Optional<String> sendConversationAndGetResponseText(Conversation conversation);
}
