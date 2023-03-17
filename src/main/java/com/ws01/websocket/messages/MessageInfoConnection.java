package com.ws01.websocket.messages;

public class MessageInfoConnection {
    private String connectionId;
    private MessagesInfo.Info info;
    private ChatRoomMessage chatRoomMessage;

    public MessageInfoConnection(String connectionId, MessagesInfo.Info info, ChatRoomMessage chatRoomMessage) {
        this.connectionId = connectionId;
        this.info = info;
        this.chatRoomMessage = chatRoomMessage;
    }

    public ChatRoomMessage getChatRoomMessage() {
        return chatRoomMessage;
    }

    public MessageInfoConnection(String connectionId,  MessagesInfo.Info info) {
        this.connectionId = connectionId;
        this.info = info;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public MessagesInfo.Info getMessageInfo() {
        return info;
    }
}
