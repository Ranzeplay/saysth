package space.ranzeplay.saysth.chat;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class TranslateTextResponse {
    @Getter
    public class Result {
        @SerializedName("translated_text")
        public String translatedText;
    }

    Result result;
    boolean success;
    Object[] errors;
    Object[] messages;
}
