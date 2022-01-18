package com.example.chatbot;

public class MsgModel {
    private String chatBotReply;

    public MsgModel(String chatBotReply) {
        this.chatBotReply = chatBotReply;
    }

    public String getCnt() {
        return chatBotReply;
    }

    public void setchatBotReply(String chatBotReply) {
        this.chatBotReply = chatBotReply;
    }

    //to get the message
}
