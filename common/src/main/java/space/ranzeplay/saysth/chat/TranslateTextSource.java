package space.ranzeplay.saysth.chat;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TranslateTextSource {
    @SerializedName("text")
    String text;
    @SerializedName("source_lang")
    String sourceLang;
    @SerializedName("target_lang")
    String targetLang;
}
