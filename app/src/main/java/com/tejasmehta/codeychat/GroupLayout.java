package com.tejasmehta.codeychat;

/**
 * Created by tejasmehta on 3/1/18.
 */

public class GroupLayout {

    private String name;
    private String lastMsg;

    public GroupLayout(String name, String lastMsg) {
        this.name = name;
        this.lastMsg = lastMsg;
    }

    public String getName() {
        return name;
    }

    public String LastMsg() {
        return lastMsg;
    }



}
