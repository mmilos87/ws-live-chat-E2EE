package com.ws01.websocket.messages;

import java.util.List;

public class UsersPskMessage extends BaseMessage{
    private final String ownerId;
    private final List<Psk> pskList;

    public UsersPskMessage(String ownerId, List<Psk> pskList){
        super("USER_PSK");
        this.ownerId = ownerId;
        this.pskList = pskList;

    }
}
