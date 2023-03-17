package com.ws01.websocket.messages;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class ChatRoomMessage extends BaseMessage{

    private Long messageId;

    private String senderId;

    private String senderNickname;

    private Long chatRoomId;

    private String message;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private MessagesInfo messageInfo;

    private PskUserMessage pskUserMessage;

    public MessagesInfo getMessageInfo() {
        return messageInfo;
    }

    public Long getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public Long getChatRoomId() {
        return chatRoomId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public ChatRoomMessage(PskUserMessage pskUserMessage) {
        super(pskUserMessage.messageType);
        this.pskUserMessage=pskUserMessage;
    }

    public PskUserMessage getPskUserMessage() {
        return pskUserMessage;
    }

    public ChatRoomMessage(ChatRoomMessage chatRoomMessage, Set<MessagesInfo.Info> messageInfo) {
        super("MESSAGE");
        this.messageId = chatRoomMessage.messageId;
        this.chatRoomId = chatRoomMessage.chatRoomId;
        this.senderId = chatRoomMessage.senderId;
        this.message = chatRoomMessage.message;
        this.createdAt = chatRoomMessage.createdAt;
        this.updatedAt = chatRoomMessage.updatedAt;
        this.deletedAt = chatRoomMessage.deletedAt;
        this.messageInfo = new MessagesInfo(messageInfo);
        this.senderNickname = chatRoomMessage.senderNickname;
    }

    public ChatRoomMessage(Map.Entry<ChatRoomMessage ,Set<MessagesInfo.Info>> entry) {
        this(entry.getKey(),entry.getValue());
    }

    public ChatRoomMessage(){
        super("MESSAGE");

    }

    private ChatRoomMessage(Builder builder) {
        super("MESSAGE");
        this.messageId = builder.messageId;
        this.chatRoomId = builder.chatRoomId;
        this.senderId = builder.senderId;
        this.message = builder.message;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.deletedAt = builder.deletedAt;
        this.messageInfo =new MessagesInfo(builder.info);
        this.senderNickname = builder.senderNickname;

    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatRoomMessage)) return false;
        ChatRoomMessage that = (ChatRoomMessage) o;
        return getMessageId().equals(that.getMessageId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMessageId(), getSenderId(), getChatRoomId(),
                getMessage(), getCreatedAt(), getUpdatedAt(), getDeletedAt());
    }


    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long messageId;
        private String senderId;
        private Long chatRoomId;
        private String senderNickname;
        private String message;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
        private List<MessagesInfo.Info> info = new ArrayList<>();

        private Builder() { }

        public Builder  messageId(Long messageId) {
            this.messageId=messageId;
            return this;
        }
        public Builder  messageInfo(List<MessagesInfo.Info> info) {
            this.info = info;
            return this;
        }


        public Builder senderId(String senderId) {
            this.senderId = senderId;
            return this;
        }

        public Builder chatRoomId(Long chatRoomId) {
            this.chatRoomId = chatRoomId;
            return this;
        }
        public Builder message(String message) {
            if (Objects.nonNull(message)) this.message = message;
            return this;
        }
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder deletedAt(LocalDateTime deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }
        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder createdAt(Timestamp createdAt) {
            if (Objects.nonNull(createdAt)) this.createdAt = createdAt.toLocalDateTime();
            return this;
        }
        public Builder deletedAt(Timestamp deletedAt) {
            if (Objects.nonNull(deletedAt)) this.deletedAt = deletedAt.toLocalDateTime();
            return this;
        }
        public Builder updatedAt(Timestamp updatedAt) {
            if (Objects.nonNull(updatedAt)) this.updatedAt = updatedAt.toLocalDateTime();
            return this;
        }

        public ChatRoomMessage build() {
            return new ChatRoomMessage(this);
        }
        public Builder senderNickname(String senderNickname) {
            this.senderNickname = senderNickname;
            return this;
        }
    }
    public static List<ChatRoomMessage> extractChatRoomMessages(ResultSet resultSet) throws SQLException {
        List<ChatRoomMessage> chatRoomMessages= new ArrayList<>();
        while (resultSet.next()) {
            chatRoomMessages.add(getFromResultSet(resultSet));
        }
        return chatRoomMessages;
    }

    public static ChatRoomMessage getFromResultSet(ResultSet resultSet) throws SQLException {
        return ChatRoomMessage.builder()
                .chatRoomId(resultSet.getLong("chat_room_id"))
                .messageId(resultSet.getLong("id"))
                .senderId(resultSet.getString("sender_id"))
                .message(resultSet.getString("message"))
                .createdAt(resultSet.getTimestamp("created_at"))
                .updatedAt(resultSet.getTimestamp("updated_at"))
                .deletedAt(resultSet.getTimestamp("deleted_at"))
                .senderNickname(resultSet.getString("sender_nickname"))
                .build();
    }


}