package com.habitu.app;

import com.google.firebase.Timestamp;

public class ChatMessage {
    private String senderId;
    private String senderName;
    private String text;
    private Timestamp timestamp;

    public ChatMessage() {}

    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getText() { return text; }
    public Timestamp getTimestamp() { return timestamp; }

    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
