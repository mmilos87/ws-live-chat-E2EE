package com.ws01.websocket.messages;

public class PrivateResponseMessage extends BaseMessage{

    private final String from;
    private final String to;
    private final String publicKey;


    public PrivateResponseMessage(String from, String to, String publicKey) {
        super("PRIVATE_RESPONSE");
        this.from = from;
        this.to = to;
        this.publicKey = publicKey;

    }
}
