package com.example.farmacia.model;

public class ChatMessage {
    public static final int SENDER_USER = 0;
    public static final int SENDER_BOT = 1;

    private final int sender;
    private final String text;

    public ChatMessage(int sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    public int getSender() { return sender; }
    public String getText() { return text; }
}
