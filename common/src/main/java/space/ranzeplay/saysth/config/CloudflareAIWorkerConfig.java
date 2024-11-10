package space.ranzeplay.saysth.config;

import lombok.Data;

import java.net.URI;
import java.net.http.HttpRequest;

@Data
public class CloudflareAIWorkerConfig implements IApiEndpointConfig {
    public String modelName;
    public String apiKey;
    public String accountId;

    @Override
    public HttpRequest buildHttpRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create("https://api.cloudflare.com/client/v4/accounts/" + accountId + "/ai/run/" + modelName))
                .header("Authorization", "Bearer " + apiKey)
                .build();
    }
}
