package com.ws01.websocket.facade;

import com.ws01.websocket.messages.ChatRoomMessage;
import com.ws01.websocket.messages.ChatRoomMessageAndInfo;
import com.ws01.websocket.messages.MessageInfoConnection;
import com.ws01.websocket.messages.MessagesInfo;

import java.sql.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatRoomMessageUserFacade {

    private static final String UPDATE_CHAT_ROOM_MESSAGE_USER_READ = """
                    WITH result AS (
                        UPDATE chat_room_message_user
                        SET read_at = CAST(? AS TIMESTAMP)
                        WHERE user_id = CAST(? AS UUID)
                            AND read_at IS NULL
                            AND chat_room_message_id = ANY (?)
                        RETURNING
                            chat_room_message_id
                            , user_id
                            , read_at
                            , received_at
                    
                    )
                    SELECT DISTINCT
                        crmu.chat_room_message_id
                        , crmu.user_id
                        , (
                            SELECT received_at
                            FROM result
                            WHERE chat_room_message_id = crmu.chat_room_message_id
                        ) AS received_at
                        , (
                            SELECT read_at
                            FROM result
                            LIMIT 1
                        ) AS read_at
                        , uc.connection_id
                        , cru.user_nickname
                    FROM chat_room_message_user crmu
                    INNER JOIN chat_room_user cru
                        ON cru.chat_room_id = (
                            SELECT crm.chat_room_id
                            FROM chat_room_message crm
                            WHERE crm.id = crmu.chat_room_message_id
                        )
                    INNER JOIN user_connection uc
                        ON (uc.user_id = cru.user_id)
                    WHERE crmu.chat_room_message_id IN (
                        SELECT chat_room_message_id
                        FROM result
                    ) AND crmu.user_id = (
                            SELECT user_id
                            FROM result
                            LIMIT 1
                    )""";


    private static final String GET_CHAT_ROOM_MESSAGES_USER = """
                    WITH cte_user AS(
                        SELECT CAST(? AS UUID) AS user_id
                    ), result_select AS (
                        SELECT crm.*
                        FROM chat_room_message crm
                        WHERE crm.chat_room_id = (
                            SELECT chat_room_id
                            FROM chat_room_user
                            WHERE user_id = (
                                SELECT user_id
                                FROM cte_user
                            )
                                AND chat_room_id = ?
                        ) AND crm.id <= ?
                        ORDER BY crm.id DESC LIMIT 20
                    ), result_update AS (
                        UPDATE chat_room_message_user crmu
                        SET received_at = CAST( ? AS TIMESTAMP)
                        WHERE received_at IS NULL
                            AND chat_room_message_id IN (
                                SELECT id
                                FROM result_select
                            )
                            AND user_id = (
                                SELECT user_id
                                FROM cte_user
                            )
                            RETURNING  chat_room_message_id
                    ), result_insert AS (
                        INSERT INTO chat_room_message_user(
                            chat_room_message_id
                            , user_id
                            , received_at
                        )
                        SELECT
                            rs.id AS  chat_room_message_id
                            , (
                                SELECT user_id
                                FROM cte_user
                            ) AS user_id
                            , now() AS  received_at
                        FROM result_select rs
                        WHERE rs.id NOT IN (
                            SELECT chat_room_message_id
                            FROM chat_room_message_user
                            WHERE user_id = (
                                SELECT user_id
                                FROM cte_user
                            )
                        )
                        RETURNING  chat_room_message_id
                    ), result_union AS (
                        SELECT chat_room_message_id
                        FROM result_update
                        UNION
                        SELECT chat_room_message_id
                        FROM result_insert
                    )
                    SELECT
                        rs.*
                        , crmu.*
                        , (
                            CASE WHEN rs.id is NOT NULL
                            THEN rs.id
                            END
                        ) AS chat_room_message_id
                        , (
                            SELECT user_nickname
                            FROM chat_room_user
                            WHERE user_id = rs.sender_id
                                AND chat_room_id = rs.chat_room_id
                        ) AS sender_nickname
                        , cru.user_nickname
                    FROM result_union ru
                    RIGHT JOIN result_select rs
                        ON rs.id = ru.chat_room_message_id
                    INNER JOIN chat_room_message_user crmu
                        ON crmu.chat_room_message_id = rs.id
                    INNER JOIN chat_room_user cru
                        ON cru.user_id=crmu.user_id
                        AND
                        cru.chat_room_id = (
                            SELECT chat_room_id
                            FROM result_select
                            LIMIT 1
                        )
                    ORDER BY rs.id""";


    private final Connection conn;

    public ChatRoomMessageUserFacade(Connection conn) {
        this.conn = conn;
    }

    public Map<String, MessagesInfo> updateReadAt(String userId, List<Long> chatRoomMessageIds) throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(UPDATE_CHAT_ROOM_MESSAGE_USER_READ)) {
            Array array = conn.createArrayOf("BIGINT", chatRoomMessageIds.toArray());
            preparedStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now(Clock.systemUTC())));
            preparedStatement.setString(2,userId);
            preparedStatement.setArray(3,array);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<MessageInfoConnection> list = new ArrayList<>();
            while(resultSet.next()){
                String connectionId = resultSet.getString("connection_id");
                list.add(new MessageInfoConnection(connectionId, MessagesInfo.getFromResultSet(resultSet)));
            }
            return list.stream().collect(
                    Collectors.groupingBy(MessageInfoConnection::getConnectionId,
                            Collectors.mapping(MessageInfoConnection::getMessageInfo, Collectors.toSet())))
                    .entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,entry -> new MessagesInfo(entry.getValue())));
        }
    }

    public List<ChatRoomMessage> getChatRoomMessagesPageable(
            String userId, Long chatRoomId, Long fromMessageId) throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(GET_CHAT_ROOM_MESSAGES_USER)) {
            preparedStatement.setString(1,userId);
            preparedStatement.setLong(2,chatRoomId);
            preparedStatement.setLong(3,fromMessageId);
            preparedStatement.setTimestamp(4,Timestamp.valueOf(LocalDateTime.now(Clock.systemUTC())));
            ResultSet resultSet = preparedStatement.executeQuery();
            List<ChatRoomMessageAndInfo> list= new ArrayList<>();
            while (resultSet.next()){
                ChatRoomMessage chatRoomMessage = ChatRoomMessage.getFromResultSet(resultSet);
                list.add(new ChatRoomMessageAndInfo(chatRoomMessage, MessagesInfo.getFromResultSet(resultSet)));
            }
            return list.stream().collect(
                            Collectors.groupingBy(ChatRoomMessageAndInfo::getChatRoomMessage,
                                    Collectors.mapping(ChatRoomMessageAndInfo::getMessageInfo, Collectors.toSet())))
                    .entrySet().stream()
                    .map(ChatRoomMessage::new).toList();
        }
    }


}
