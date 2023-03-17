package com.ws01.websocket.facade;


import com.ws01.websocket.exception.ValidationException;
import com.ws01.websocket.messages.*;

import java.sql.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ChatRoomMessageFacade {
     private static final String SAVE_CHAT_ROOM_MESSAGE_PUBLIC = """
                WITH prepare_statement AS (
                    SELECT
                        CAST(? AS UUID)  AS user_id
                        , ? AS chat_room_id
                        , ? AS message
                        , CAST(? AS TIMESTAMP)  AS created_at
                         
                )
                , cte_user_public_chat_room AS (
                    SELECT cr.id
                    FROM chat_room cr
                    INNER JOIN chat_room_user cru
                        ON cr.id = cru.chat_room_id
                    WHERE NOT cr.private_room
                        AND cru.user_id IN (
                            SELECT user_id
                            FROM prepare_statement
                        )
                        AND cr.id IN (
                            SELECT chat_room_id
                            FROM prepare_statement
                        )
                )
                , result_insert_message AS (
                    INSERT INTO chat_room_message (
                        sender_id
                        , chat_room_id
                        , message
                        , created_at
                    )
                    SELECT *
                    FROM prepare_statement
                    WHERE EXISTS (
                        SELECT *
                        FROM cte_user_public_chat_room
                        )
                    RETURNING *
                    
                ), result_insert_message_user AS (
                    INSERT INTO  chat_room_message_user (
                        user_id
                        , chat_room_message_id
                        , received_at
                        , read_at
                    )
                    SELECT
                        cru.user_id
                        , (
                            SELECT id
                            FROM result_insert_message
                        ) AS chat_room_message_id
                        , (
                            CASE
                                WHEN (
                                    SELECT count(*)
                                    FROM user_connection uc
                                    WHERE uc.user_id = cru.user_id
                                ) > 0
                                THEN (
                                    SELECT created_at
                                    FROM result_insert_message
                                )
                            END
                        ) AS received_at
                        , (
                            CASE
                                WHEN cru.user_id = (
                                    SELECT sender_id
                                    FROM result_insert_message
                                )
                                THEN (
                                    SELECT created_at
                                    FROM result_insert_message
                                )
                            END
                        )AS read_at
                    FROM chat_room_user cru
                    WHERE cru.chat_room_id = (
                        SELECT chat_room_id
                        FROM result_insert_message
                    )
                    RETURNING *
                )
                SELECT
                    DISTINCT rimu.*
                    , rim.*
                    , uc.connection_id
                    , (
                        SELECT user_nickname
                        FROM chat_room_user
                        WHERE user_id = rim.sender_id
                            AND chat_room_id = rim.chat_room_id
                    ) AS sender_nickname
                    , cru.user_nickname
                FROM result_insert_message_user rimu
                INNER JOIN chat_room_user cru
                    ON (
                        cru.user_id = rimu.user_id
                        AND
                        cru.chat_room_id = (
                            SELECT chat_room_id
                            FROM prepare_statement
                        )
                    )
                INNER JOIN result_insert_message rim
                    ON rim.id=rimu.chat_room_message_id
                INNER JOIN user_connection uc
                    ON uc.user_id=rimu.user_id""";


    private static final String UPDATE_CHAT_ROOM_MESSAGE = """
                    WITH result_update AS (
                        UPDATE chat_room_message
                        SET message = ?
                            , updated_at = CAST(? AS TIMESTAMP)
                        WHERE sender_id = CAST(? AS UUID)
                            AND id = ?
                            AND deleted_at IS NULL
                        RETURNING *
                    
                    )
                    SELECT
                        crmu.*
                        , ru.*
                        , uc.connection_id
                        , (
                            SELECT user_nickname
                            FROM chat_room_user
                            WHERE user_id = ru.sender_id
                                AND chat_room_id = ru.chat_room_id
                        ) AS sender_nickname
                        , cru.user_nickname
                    FROM chat_room_user cru
                    INNER JOIN result_update ru
                        ON ru.chat_room_id = cru.chat_room_id
                    INNER JOIN chat_room_message_user crmu
                        ON (
                            crmu.chat_room_message_id = ru.id
                            AND
                            cru.user_id = crmu.user_id
                        )
                    INNER JOIN user_connection uc
                        ON uc.user_id = cru.user_id""";
    private static final String NOT_RECEIVED_MESSAGES_PER_ROOM = """
                    WITH cte_user AS (
                        SELECT
                            CAST(? AS UUID) AS user_id
                    )
                    , cte_rooms AS (
                        SELECT
                            cr.id AS chat_room_id
                            , (
                                CASE
                                    WHEN cr.private_room
                                    THEN (
                                        SELECT cru1.user_nickname
                                        FROM chat_room_user cru1
                                        WHERE cru1.chat_room_id = cr.id
                                            AND cru1.user_id <> cru.user_id
                                    )
                                    ELSE cr.room_name
                                END
                            ) AS room_name
                            , cr.private_room
                        FROM chat_room_user cru
                        INNER JOIN chat_room cr
                            ON cr.id = cru.chat_room_id
                        WHERE user_id = (
                            SELECT user_id
                            FROM cte_user
                        )
                    )
                    , cte_count AS (
                        SELECT
                            crm.chat_room_id
                            , count(*)
                        FROM chat_room_message_user crmu
                        INNER JOIN chat_room_message crm
                            ON crm.id = crmu.chat_room_message_id
                        WHERE read_at IS  NULL
                            AND crmu.user_id = (
                                SELECT user_id
                                FROM cte_user
                            )
                        GROUP BY crm.chat_room_id
                    )
                    SELECT
                         r.*
                         , c.count
                    FROM cte_count c
                    INNER JOIN cte_rooms r
                        ON r.chat_room_id = c.chat_room_id""";
    private static final String DELETE_CHAT_ROOM_MESSAGE = """
                    WITH result_update AS (
                        UPDATE chat_room_message
                        SET
                            message = NULL
                            , deleted_at = CAST(? AS TIMESTAMP)
                        WHERE sender_id = CAST(? AS UUID)
                            AND id = ?
                            AND deleted_at IS NULL
                        RETURNING *
                    )
                    SELECT
                        crmu.*
                        , ru.*
                        , uc.connection_id
                        , (
                            SELECT user_nickname
                            FROM chat_room_user
                            WHERE user_id = ru.sender_id
                            AND chat_room_id = ru.chat_room_id
                        ) AS sender_nickname
                        , cru.user_nickname
                    FROM chat_room_user cru
                    INNER JOIN result_update ru
                        ON ru.chat_room_id = cru.chat_room_id
                    INNER JOIN chat_room_message_user crmu
                        ON (
                            crmu.chat_room_message_id = ru.id 
                            AND 
                            cru.user_id=crmu.user_id
                        )
                    INNER JOIN user_connection uc
                        ON uc.user_id = cru.user_id""";



    private final Connection conn;

    public ChatRoomMessageFacade(Connection conn){
        this.conn =conn;
    }

    public Map<String,ChatRoomMessage> deleteChatRoomMessage(String senderId, Long chatRoomMessageId)
            throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(DELETE_CHAT_ROOM_MESSAGE)) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now(Clock.systemUTC())));
            preparedStatement.setString(2,senderId);
            preparedStatement.setLong(3,chatRoomMessageId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return getConnectionIdChatRoomMessageMap(resultSet);
        }
    }
    public Map<String,ChatRoomMessage> updateChatRoomMessage(String senderId, Long chatRoomMessageId, String message)
            throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(UPDATE_CHAT_ROOM_MESSAGE)) {
            preparedStatement.setString(1,message);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now(Clock.systemUTC())));
            preparedStatement.setString(3, senderId);
            preparedStatement.setLong(4,chatRoomMessageId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return getConnectionIdChatRoomMessageMap(resultSet);
        }
    }

    public Map<String, ChatRoomMessage> saveChatRoomMessage(String senderId, Long chatRoomId, String message)
            throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(SAVE_CHAT_ROOM_MESSAGE_PUBLIC)) {
            preparedStatement.setString(1,senderId);
            preparedStatement.setLong(2,chatRoomId);
            preparedStatement.setString(3,message);
            preparedStatement.setTimestamp(4,Timestamp.valueOf(LocalDateTime.now(Clock.systemUTC())));
            ResultSet resultSet = preparedStatement.executeQuery();
            return getConnectionIdChatRoomMessageMap(resultSet);
        }
    }

    public static Map<String, ChatRoomMessage> getConnectionIdChatRoomMessageMap(ResultSet resultSet)
            throws SQLException {
        List<MessageInfoConnection> listMsg = new ArrayList<>();
        Map<String, ChatRoomMessage> mapPsk = new HashMap<>();

        if (!resultSet.next()) {
            throw new ValidationException("Invalid chat room ID or message ID!");

        }else {
            do {
                String connectionId = resultSet.getString("connection_id");

                if (hasColumn(resultSet,"identity") && Objects.nonNull(resultSet.getString("identity"))) {
                    String identity = resultSet.getString("identity");
                    String ephemeral = resultSet.getString("ephemeral");
                    String ownerId = resultSet.getString("owner_id");
                    PskUserMessage pskUserMessage =  new PskUserMessage(ownerId,identity,ephemeral);
                    mapPsk.put(connectionId, new ChatRoomMessage(pskUserMessage));

                } else {
                    MessagesInfo.Info info = MessagesInfo.getFromResultSet(resultSet);
                    ChatRoomMessage chatRoomMessage = ChatRoomMessage.getFromResultSet(resultSet);
                    listMsg.add(new MessageInfoConnection(connectionId, info, chatRoomMessage));

                }
            } while (resultSet.next());

        }



        if(!mapPsk.isEmpty()) return mapPsk;

        List<ChatRoomMessage> chatRoomMessages = listMsg.stream()
                .collect(Collectors.groupingBy(MessageInfoConnection::getChatRoomMessage,
                        Collectors.mapping(MessageInfoConnection::getMessageInfo, Collectors.toSet())))
                .entrySet().stream().map(ChatRoomMessage::new).toList();

        return listMsg.stream().collect(
                Collectors.toMap(MessageInfoConnection::getConnectionId, mic -> chatRoomMessages.get(0)));
    }

    public Notifications notReceivedMessagesPerRoom(String userId) throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(NOT_RECEIVED_MESSAGES_PER_ROOM)) {
            preparedStatement.setString(1,userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Notifications.Notification> list= new ArrayList<>();
            while (resultSet.next()){
                Notifications.Notification fromResultSet = Notifications.getFromResultSet(resultSet);
                list.add(fromResultSet);
            }
            return new Notifications(list);
        }
    }
    public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData resultSetMetaData = rs.getMetaData();
        int columns = resultSetMetaData.getColumnCount();
        for (int i = 1; i <= columns; i++) {
            if (columnName.equals(resultSetMetaData.getColumnName(i))) {
                return true;
            }
        }
        return false;
    }

}