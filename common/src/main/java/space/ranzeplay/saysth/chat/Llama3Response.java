package space.ranzeplay.saysth.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Llama3Response {
    @AllArgsConstructor
    @Getter
    public static class Result {
        String response;
    }
    Result result;

    boolean success;

    Object[] errors;
    Object[] messages;
}
