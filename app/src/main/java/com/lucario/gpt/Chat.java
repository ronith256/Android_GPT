package com.lucario.gpt;

import java.io.File;
import java.io.Serializable;

public class Chat implements Serializable {
    private int mProfileLogo;
    private String chatName;
    private String mLatestChat;
    private int chatId;
    File chatArray;
    public Chat(int chatId, int profileLogo, String profileName, String latestChat, File chatArray) {
        this.chatId = chatId;
        this.mProfileLogo = profileLogo;
        this.chatName = profileName;
        this.mLatestChat = latestChat;
        this.chatArray = chatArray;
    }

    public int getProfileLogo() {
        return mProfileLogo;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String name){
        chatName = name;
    }

    public String getLatestChat() {
        return mLatestChat;
    }

    public void setLatestChat(String chat){mLatestChat = chat;}
    public int getChatId(){
        return chatId;
    }

    public File getChatArray(){
        return chatArray;
    }


}
