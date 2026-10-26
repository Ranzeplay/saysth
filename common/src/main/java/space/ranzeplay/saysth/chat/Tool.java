package space.ranzeplay.saysth.chat;

import com.google.gson.JsonObject;

public interface Tool {
    String getName();

    String getDescription();

    JsonObject getParameters();

    String execute(JsonObject arguments);
}
