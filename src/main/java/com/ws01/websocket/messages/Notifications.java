package com.ws01.websocket.messages;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Notifications extends BaseMessage{

    private final List<Notification> notifications;

    public List<Notification> getNotifications() {
        return notifications;
    }

    public Notifications(List<Notification> notifications) {
        super("NOTIFICATION");
        this.notifications = notifications;
    }
    public static Notification getFromResultSet(ResultSet resultSet) throws SQLException {
        Long chatRoomId = resultSet.getLong("chat_room_id");
        Long count = resultSet.getLong("count");
        String roomName = resultSet.getString("room_name");
        boolean privateRoom = resultSet.getBoolean("private_room");
        return new Notification(chatRoomId,count,roomName,privateRoom);
    }
    public static class Notification {

        Long chatRoomId;
        Long count;
        String roomName;
        boolean privateRoom;

        public Notification(Long chatRoomId, Long count, String roomName, boolean privateRoom) {
            this.chatRoomId = chatRoomId;
            this.count = count;
            this.roomName=roomName;
            this.privateRoom=privateRoom;
        }
    }
}
