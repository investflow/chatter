package ru.investflow.android.chat;

public class ChatMessage {

    private long millis;
    private String user;
    private String text;

    // Required default constructor for Firebase object mapping
    @SuppressWarnings("unused")
    private ChatMessage() {
    }

    public ChatMessage(long millis) {
        this.millis = millis;
    }

    ChatMessage(String message, String author, long millis) {
        this.text = message;
        this.user = author;
        this.millis = millis;
    }

    public long getMillis() {
        return millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
