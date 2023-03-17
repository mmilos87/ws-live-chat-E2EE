package com.ws01.websocket.messages;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class MessagesInfo extends BaseMessage{
    private List<Info> info;

    public List<Info> getMessageInfo() {
        return info;
    }

    public MessagesInfo(Collection<Info> info) {
        super("INFO");
        this.info = new ArrayList<>(info);
    }

    public static Info getFromResultSet(ResultSet resultSet) throws SQLException {
        String userId = resultSet.getString("user_id");
        Long messageId = resultSet.getLong("chat_room_message_id");
        Timestamp receivedAt = resultSet.getTimestamp("received_at");
        Timestamp readAt = resultSet.getTimestamp("read_at");
        String username = resultSet.getString("user_nickname");
        return new Info(messageId,userId,receivedAt,readAt,username);
    }
    public static class Info {
        private Long messageId;
        private String userId;
        private String userNickname;
        private LocalDateTime receivedAt;
        private LocalDateTime readAt;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Info)) return false;
            Info info = (Info) o;
            return Objects.equals(messageId, info.messageId) && Objects.equals(userId, info.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(messageId, userId, receivedAt, readAt);
        }

        public Info(Long messageId, String userId, Timestamp receivedAt, Timestamp readAt,String userNickname) {
            this.messageId = messageId;
            this.userId = userId;
            if(Objects.nonNull(receivedAt))this.receivedAt = receivedAt.toLocalDateTime();
            if(Objects.nonNull(readAt))this.readAt = readAt.toLocalDateTime();
            this.userNickname = userNickname;
        }

    }

}
