package com.wilddog.conversation.bean;

public class Chat {

    private String message;
    private String author;
    private String uid;

    private Chat() {
    }

    public Chat(String message, String author, String uid) {
        this.message = message;
        this.author = author;
        this.uid = uid;
    }
    public Chat(String message, String author) {
        this.message = message;
        this.author = author;
    }

    public String getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }

    public String getUid() {
        return uid;
    }
}
