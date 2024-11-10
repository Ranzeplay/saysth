package space.ranzeplay.saysth.config;

import java.net.http.HttpRequest;

public interface IApiEndpointConfig {
    HttpRequest buildHttpRequest();
}
