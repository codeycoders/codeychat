package com.tejasmehta.codeychat;

/**
 * Created by tejasmehta on 2/21/18.
 */

public class ChatBubble {


        private String msg;
        private String date;
        private String msgType;

        public ChatBubble(String msg, String date, String msgType) {
            this.msg = msg;
            this.date = date;
            this.msgType = msgType;
        }

        public String Msg() {
            return msg;
        }

        public String Date() {
            return date;
        }

        public String MsgType() {
            return msgType;
        }

}
