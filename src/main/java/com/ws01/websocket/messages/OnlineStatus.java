package com.ws01.websocket.messages;


public class OnlineStatus extends BaseMessage{
    private String userId;
    private boolean status;
    public OnlineStatus(String userId, boolean status) {
        super("ONLINE_STATUS");
        this.userId =userId;
        this.status = status;

    }

}
