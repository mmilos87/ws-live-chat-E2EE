package com.ws01.websocket.confing.session;


import java.util.Objects;
import java.util.Set;

public class Acknowledge {
    private final String senderUserId;
    private final String receiverUserId;
    private final String encMsg;

    public String getSenderUserId() {
        return senderUserId;
    }

    public String getReceiverUserId() {
        return receiverUserId;
    }


    public boolean equals(String userId1, String userId2, String encMsg){
        return Set.of(userId1,userId2).containsAll(getUserIds()) && this.encMsg.equals(encMsg);
    }

    public Set<String> getUserIds() {
        return Set.of(senderUserId, receiverUserId);
    }

    public Acknowledge(String senderUserId, String receiverUserId, String encMsg) {
        this.senderUserId = senderUserId;
        this.receiverUserId = receiverUserId;
        this.encMsg = encMsg;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Acknowledge that)) return false;
        return Objects.equals(senderUserId, that.senderUserId) &&
                Objects.equals(receiverUserId, that.receiverUserId) && Objects.equals(encMsg, that.encMsg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderUserId, receiverUserId, encMsg);
    }
}
