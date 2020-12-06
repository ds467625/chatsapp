package com.example.chatsapp.Model;

public class Chatlist {

    private String id;


    public Chatlist(String id,String sid) {
        this.id = id;
    }

    public Chatlist() {
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
