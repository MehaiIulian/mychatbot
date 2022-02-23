package com.example.chatbot;

import android.widget.ImageView;

public class ChatsModel {

    private final String message;
    private final String sender;
    //to display in different views


    public ChatsModel(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }


    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    /*
    public void setMessage(String message) {
        this.message = message;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }*/
}
