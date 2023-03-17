package com.ws01.websocket.messages;

public class PrivateRequestMessage{

    private final String senderEphemeral;
    private final String senderIdentity;
    private final String receiverEphemeral;
    private final String receiverIdentity;

    public PrivateRequestMessage(String senderEphemeral, String senderIdentity,
                                 String receiverEphemeral, String receiverIdentity) {
        this.senderEphemeral = senderEphemeral;
        this.senderIdentity = senderIdentity;
        this.receiverEphemeral = receiverEphemeral;
        this.receiverIdentity = receiverIdentity;
    }
}
