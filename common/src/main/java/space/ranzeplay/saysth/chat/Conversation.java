package space.ranzeplay.saysth.chat;

import com.google.gson.Gson;

import java.util.ArrayList;

public class Conversation {
    public ArrayList<Message> messages;

    public Conversation(ArrayList<Message> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public void addMessage(Message message) {
        messages.add(message);
    }
}
