package com.ws01.websocket.messages;

public class AcknowledgeMessage extends BaseMessage{
    private final String from;
    private final String to;
    private final boolean handshake;

    public AcknowledgeMessage(String from, String to, boolean handshake) {
        super("ACKNOWLEDGE");
        this.from = from;
        this.to = to;
        this.handshake = handshake;
    }
}
