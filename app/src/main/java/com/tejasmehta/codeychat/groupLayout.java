package com.tejasmehta.codeychat;

/**
 * Created by tejasmehta on 3/1/18.
 */

public class groupLayout {

    private String name;
    private String lastMsg;

    public groupLayout(String name, String lastMsg) {
        this.name = name;
        this.lastMsg = lastMsg;
    }

    public String Name() {
        return name;
    }

    public String LastMsg() {
        return lastMsg;
    }



}
