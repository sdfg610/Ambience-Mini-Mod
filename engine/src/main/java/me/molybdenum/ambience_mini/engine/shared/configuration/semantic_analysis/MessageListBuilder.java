package me.molybdenum.ambience_mini.engine.shared.configuration.semantic_analysis;

import me.molybdenum.ambience_mini.engine.shared.configuration.messages.Message;

import java.util.ArrayList;

public class MessageListBuilder {
    private final ArrayList<Message> elements;
    private boolean hasErrorOnNode = false;


    public MessageListBuilder() {
        elements = new ArrayList<>();
    }

    public MessageListBuilder(ArrayList<Message> elements) {
        this.elements = elements;
    }


    public void add(Message message) {
        elements.add(message);
        if (message.isError())
            hasErrorOnNode = true;
    }

    public MessageListBuilder detatch() {
        return new MessageListBuilder(elements);
    }


    public boolean hasErrorOnNode() {
        return hasErrorOnNode;
    }


    public ArrayList<Message> getList() {
        return elements;
    }
}
