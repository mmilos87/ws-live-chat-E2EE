package com.ws01.websocket.messages;

public class ChatRoomMessageAndInfo {
    private ChatRoomMessage chatRoomMessage;
    private MessagesInfo.Info info;

    public ChatRoomMessage getChatRoomMessage() {
        return chatRoomMessage;
    }

    public MessagesInfo.Info getMessageInfo() {
        return info;
    }

    public ChatRoomMessageAndInfo(ChatRoomMessage chatRoomMessage, MessagesInfo.Info info) {
        this.chatRoomMessage = chatRoomMessage;
        this.info = info;
    }
}
