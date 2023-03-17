package com.ws01.websocket.messages;

public class BaseMessage {
    String messageType;

    public String getMessageType() {
        return messageType;
    }

    public BaseMessage(String messageType) {
        this.messageType = messageType;
    }
}
