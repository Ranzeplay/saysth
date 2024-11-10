package space.ranzeplay.saysth.config;

import lombok.Data;

import java.net.URI;
import java.net.http.HttpRequest;

@Data
public class GeneralApiConfig implements IApiEndpointConfig {
    public String apiEndpointUrl;
    public String authCredentials;

    @Override
    public HttpRequest buildHttpRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create(apiEndpointUrl))
                .header("Authorization", authCredentials)
                .build();
    }
}
