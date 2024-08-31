package space.ranzeplay.saysth.chat;

import com.github.pemistahl.lingua.api.Language;
import com.google.gson.Gson;
import space.ranzeplay.saysth.Main;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Translator {
    public static String translate(Language source, Language destination, String text) throws IOException, InterruptedException {
        var reqModel = new TranslateTextSource(text, source.name().toLowerCase(), destination.name().toLowerCase());

        var request = HttpRequest.newBuilder(URI.create("https://api.cloudflare.com/client/v4/accounts/" + Main.CONFIG_MANAGER.getConfig().getCloudflareUserId() + "/ai/run/@cf/meta/m2m100-1.2b"))
                .POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(reqModel)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .headers("Authorization", "Bearer " + Main.CONFIG_MANAGER.getConfig().getCloudflareApiKey())
                .build();
        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return new Gson().fromJson(response.body(), TranslateTextResponse.class).getResult().getTranslatedText();
    }
}
