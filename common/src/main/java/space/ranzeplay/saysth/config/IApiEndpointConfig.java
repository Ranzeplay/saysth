package space.ranzeplay.saysth.config;

import space.ranzeplay.saysth.chat.Conversation;
import space.ranzeplay.saysth.chat.Tool;
import space.ranzeplay.saysth.chat.ToolResponse;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.Optional;

public interface IApiEndpointConfig {
    HttpRequest.Builder getPartialHttpRequest();
    Optional<String> sendConversationAndGetResponseText(Conversation conversation);
    Optional<ToolResponse> sendConversation(Conversation conversation, List<Tool> tools);
}
