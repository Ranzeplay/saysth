package space.ranzeplay.saysth.chat;

import com.google.gson.Gson;

import java.util.Arrays;

public class Conversation {
    public Message[] messages;

    public Conversation(Message[] messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
