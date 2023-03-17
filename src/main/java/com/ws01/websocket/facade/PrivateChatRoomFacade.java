package com.ws01.websocket.facade;

import com.ws01.websocket.messages.ChatRoomMessage;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
@Component
public class PrivateChatRoomFacade {
    private static final String PRIVATE_CHAT_ROOM_ACK ="""
                    WITH cte_user1 AS (
                        SELECT CAST( ? AS UUID) AS id
                    
                    )
                    , cte_user2 AS (
                        SELECT CAST( ? AS UUID) AS id
                    
                    )
                    , cte_handshake AS (
                        SELECT CAST( ? AS BOOLEAN) AS handshake
                    
                    )
                    , cte_union AS (
                        SELECT *
                        FROM cte_user1
                        UNION
                        SELECT *
                        FROM cte_user2
                    
                    )
                    , cte_private_rooms AS (
                        SELECT
                            cr.id
                            , ARRAY[created_bY_id::TEXT, room_name] AS users
                        FROM chat_room cr
                        WHERE cr.private_room
                    
                    )
                    , cte_private_room AS (
                        SELECT cte.id
                        FROM cte_private_rooms cte
                        WHERE ARRAY [
                            (
                                SELECT id
                                FROM cte_user1
                            ) :: TEXT
                            ,
                            (
                                SELECT id
                                FROM cte_user2
                            ) :: TEXT
                        ] @>  cte.users
                        
                    )
                    , cte_update_room_handshake AS (
                        UPDATE chat_room
                        SET handshake = (
                            SELECT handshake
                            FROM cte_handshake
                        )
                        WHERE id IN (
                            SELECT id
                            FROM cte_private_room
                        ) AND (
                            SELECT handshake
                            FROM cte_handshake
                        )
                        RETURNING id, handshake
                        
                    )
                    , cte_chat_room_message AS (
                        SELECT *
                        FROM chat_room_message
                        WHERE chat_room_id IN (
                            SELECT id
                            FROM cte_update_room_handshake
                            UNION
                            SELECT id
                            FROM cte_private_room
                            WHERE NOT (
                                SELECT handshake
                                FROM cte_handshake
                            ) OR handshake_msg
                        )
                        
                    )
                    , cte_delete_users_message AS (
                        DELETE
                        FROM chat_room_message_user
                        WHERE chat_room_message_id IN (
                            SELECT id
                            FROM cte_chat_room_message
                        )
                        RETURNING chat_room_message_id
                        
                    )
                    , cte_delete_chat_room_message AS (
                        DELETE
                        FROM chat_room_message
                        WHERE id IN (
                            SELECT chat_room_message_id
                            FROM cte_delete_users_message
                            UNION
                            SELECT id
                            FROM cte_chat_room_message
                        )
                        RETURNING chat_room_id

                    )
                    , cte_delete_chat_room_users AS (
                        DELETE
                        FROM chat_room_user
                        WHERE chat_room_id IN (
                            SELECT chat_room_id
                            FROM cte_delete_chat_room_message
                            UNION
                            SELECT id
                            FROM cte_private_room
                        ) AND NOT (
                            SELECT handshake
                            FROM cte_handshake
                        )
                        RETURNING chat_room_id
                        
                    )
                    DELETE
                    FROM chat_room
                    WHERE id IN (
                        SELECT chat_room_id
                        FROM cte_delete_chat_room_users
                    )""";

    private static final String CREATE_PRIVATE_ROOM_AND_SAVE_MSG = """
                   WITH cte_user1 AS(
                       SELECT
                           u.id
                           , concat(u.first_name, ' ', u.last_name) as nickname
                       FROM users u
                       WHERE u.id = CAST(? AS UUID)
                                       
                   )
                   , cte_user2 AS(
                       SELECT
                           u.id
                           , concat(u.first_name, ' ', u.last_name) as nickname
                       FROM users u
                       WHERE u.id = CAST(? AS UUID)
                                       
                   )
                   , cte_union AS(
                       SELECT *
                       FROM cte_user1
                       UNION
                       SELECT *
                       FROM cte_user2
                                       
                   )
                   , cte_private_rooms AS (
                       SELECT
                           cr.id
                           , ARRAY[created_bY_id::TEXT, room_name] AS users
                       FROM chat_room cr
                       WHERE cr.private_room
                               
                   )
                   , cte_users_private_room AS (
                       SELECT
                           cte.id
                           , TRUE as real
                       FROM cte_private_rooms cte
                       WHERE ARRAY [
                           (
                               SELECT id
                               FROM cte_user1
                           ) :: TEXT
                           ,
                           (
                               SELECT id
                               FROM cte_user2
                           ) :: TEXT
                       ] @>  cte.users
                               
                   )
                   , cte_create_chat_room AS (
                       INSERT INTO chat_room(
                           created_by_id
                           , room_name
                           , private_room
                           , created_at
                       )
                       SELECT
                           u.id AS created_by_id
                           , (
                               SELECT id
                               FROM cte_user2
                           ) AS room_name
                           , TRUE AS private_room
                           , (
                               now() AT TIME ZONE 'UTC'
                           ) AS created_at
                       FROM cte_user1 u
                       WHERE NOT EXISTS (
                        SELECT er.id
                        FROM  cte_users_private_room er
                       )
                       RETURNING id
                               
                   )
                   , cte_create_chat_room_user AS (
                       INSERT INTO chat_room_user(
                           user_id
                           , chat_room_id
                           , added_by_id
                           , user_nickname
                           , admin
                           , blocked
                           , added_at
                       )
                       SELECT
                           cu.id AS user_id
                           , (
                               SELECT id
                               FROM cte_create_chat_room
                           ) AS chat_room_id
                           , (
                               SELECT id
                               FROM cte_user1
                           ) AS added_by_id
                           , cu.nickname AS user_nickname
                           , TRUE AS admin
                           , FALSE AS blocked
                           , (
                               now() AT TIME ZONE 'UTC'
                           ) AS added_at
                       FROM cte_union cu
                       WHERE NOT EXISTS (
                        SELECT er.id
                        FROM  cte_users_private_room er
                       )
                       RETURNING chat_room_id
                       
                   )
                   , cte_insert_chat_room_message AS (
                        INSERT INTO chat_room_message(
                                    chat_room_id
                                    , sender_id
                                    , message
                                    , handshake_msg
                        )
                        SELECT
                            (
                                CASE
                                    WHEN EXISTS (
                                        SELECT er.id
                                        FROM  cte_users_private_room er
                                    )
                                    THEN (
                                        SELECT er.id
                                        FROM  cte_users_private_room er
                                    )
                                    ELSE (
                                        SELECT nr.chat_room_id
                                        FROM cte_create_chat_room_user nr
                                        LIMIT 1
                                    )
                                END
                                
                            ) as chat_room_id
                            , u.id AS sender_id
                            , ? AS message
                            , TRUE AS handshake_msg
                        FROM cte_user1 u
                        LIMIT 1
                        RETURNING *
                       
                   )
                   , cte_insert_message_user AS (
                       INSERT INTO chat_room_message_user (
                           user_id
                           , chat_room_message_id
                           , received_at
                           , read_at
                       )
                       SELECT
                           cu.id
                           , (
                               SELECT id
                               FROM cte_insert_chat_room_message
                           ) AS chat_room_message_id
                           , (
                               CASE
                                   WHEN (
                                       SELECT count(*)
                                       FROM user_connection uc
                                       WHERE uc.user_id = cu.id
                                   ) > 0
                                   THEN (now() AT TIME ZONE 'UTC')
                                   ELSE NULL
                               END
                           ) AS received_at
                           , (
                               CASE
                                   WHEN cu.id = (
                                       SELECT id
                                       FROM cte_user1
                                   )
                                   THEN (now() AT TIME ZONE 'UTC')
                                   ELSE NULL
                               END
                           ) AS read_at
                       FROM cte_union cu
                       RETURNING *
                       
                   )
                   SELECT
                       cimu.*
                       , cim.*
                       , uc.connection_id
                       , (
                           SELECT nickname
                           FROM cte_union
                           WHERE id = cimu.user_id
                       ) AS user_nickname
                       , (
                           SELECT nickname
                           FROM cte_user1
                       ) AS sender_nickname
                   FROM cte_insert_message_user cimu
                   INNER JOIN   cte_insert_chat_room_message cim
                       ON cim.id = cimu.chat_room_message_id
                   INNER JOIN user_connection uc
                       ON uc.user_id = cimu.user_id""";

    private static final String SEND_PRIVATE_OR_PSK = """
                    WITH cte_user1 AS(
                        SELECT
                            u.id
                            , concat(u.first_name, ' ', u.last_name) as nickname
                        FROM users u
                        WHERE u.id = CAST(? AS UUID)
                        
                    )
                    , cte_user2 AS(
                        SELECT
                            u.id
                            , concat(u.first_name, ' ', u.last_name) as nickname
                        FROM users u
                        WHERE u.id = CAST(? AS UUID)
                                            
                    )
                    , cte_union AS (
                        SELECT *
                        FROM cte_user1
                        UNION
                        SELECT *
                        FROM cte_user2
                                        
                    )
                    , cte_private_rooms AS (
                        SELECT
                            cr.id
                            , ARRAY[created_bY_id::TEXT, room_name] AS users
                        FROM chat_room cr
                        WHERE cr.private_room
                            AND cr.handshake
                        
                    )
                    , cte_users_private_room AS (
                        SELECT
                            cte.id
                            , TRUE as real
                        FROM cte_private_rooms cte
                        WHERE ARRAY [
                            (
                                SELECT id
                                FROM cte_user1
                            ) :: TEXT
                            ,
                            (
                                SELECT id
                                FROM cte_user2
                            ) :: TEXT
                        ] @>  cte.users
                                        
                    )
                    , cte_insert_chat_room_message AS (
                        INSERT INTO chat_room_message(
                            chat_room_id
                            , sender_id
                            , message
                        )
                        SELECT
                            cr.id
                            , (
                                SELECT id
                                FROM cte_user1
                            ) AS sender_id
                            , CAST(? AS TEXT) AS message
                        FROM cte_users_private_room cr
                        WHERE cr.id IS NOT NULL
                        LIMIT 1
                        RETURNING *
                                        
                    )
                    , cte_insert_message_user AS (
                        INSERT INTO chat_room_message_user (
                            user_id
                            , chat_room_message_id
                            , received_at
                            , read_at
                        )
                        SELECT
                            cu.id
                            , (
                                SELECT id
                                FROM cte_insert_chat_room_message
                            ) AS chat_room_message_id
                            , (
                                CASE
                                    WHEN EXISTS(
                                        SELECT *
                                        FROM user_connection uc
                                        WHERE uc.user_id = cu.id
                                    )
                                    THEN (now() AT TIME ZONE 'UTC')
                                    ELSE NULL
                                END
                            ) AS received_at
                            , (
                                CASE
                                    WHEN cu.id = (
                                        SELECT id
                                        FROM cte_user1
                                    )
                                    THEN (now() AT TIME ZONE 'UTC')
                                    ELSE NULL
                                END
                            ) AS read_at
                        FROM cte_union cu
                        WHERE EXISTS (
                            SELECT id
                            FROM cte_users_private_room cpr
                        )
                        RETURNING *
                                        
                    )
                    , cte_return_message AS (
                        SELECT
                            cimu.*
                            , cim.*
                            , (
                                SELECT nickname
                                FROM cte_union
                                WHERE id = cimu.user_id
                            ) AS user_nickname
                            , (
                                SELECT nickname
                                FROM cte_user1
                            ) AS sender_nickname
                            , (
                                SELECT id
                                FROM cte_user2
                            ) AS receiver_id
                        FROM cte_insert_message_user cimu
                        INNER JOIN   cte_insert_chat_room_message cim
                            ON cim.id = cimu.chat_room_message_id
                        
                    )
                    , cte_psk AS (
                        DELETE
                        FROM user_psk up
                        WHERE id = (
                            SELECT id
                            FROM user_psk
                            WHERE owner_id IN (
                                SELECT id
                                FROM cte_user2
                            )
                            LIMIT 1
                        ) AND NOT EXISTS (
                            SELECT *
                            FROM  cte_users_private_room
                        )
                        RETURNING
                            owner_id
                            , identity
                            , ephemeral
                    )
                    SELECT
                        crm.*
                        , cpsk.*
                        , uc.connection_id
                    FROM  cte_return_message crm
                    FULL JOIN cte_psk cpsk
                        ON cpsk.owner_id = crm.receiver_id
                    INNER JOIN user_connection uc
                        ON uc.user_id = crm.user_id
                        OR uc.user_id = cpsk.owner_id
                        OR uc.user_id = (
                            SELECT id
                            FROM cte_user1
                            WHERE NOT EXISTS (
                                    SELECT *
                                    FROM cte_users_private_room
                                )
                        )""";

    // delete chat room message, if handshake is false delete chat room users and chat room
    public int handshakeAction(String user1Id, String user2Id,
                                     boolean handshake, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(PRIVATE_CHAT_ROOM_ACK)){
            preparedStatement.setString(1, user1Id);
            preparedStatement.setString(2, user2Id);
            preparedStatement.setBoolean(3, handshake);
            return preparedStatement.executeUpdate();
        }
    }

    // create private chat room, chat room users, save chat room message, save chat room message users
    public  Map<String, ChatRoomMessage> createPrivateChatRoom(String user1Id, String user2Id,
                                                               String msg,Connection conn) throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(CREATE_PRIVATE_ROOM_AND_SAVE_MSG)){
            preparedStatement.setString(1, user1Id);
            preparedStatement.setString(2, user2Id);
            preparedStatement.setString(3, msg);
            ResultSet resultSet = preparedStatement.executeQuery();
            return ChatRoomMessageFacade.getConnectionIdChatRoomMessageMap(resultSet);

        }
    }
    public Map<String, ChatRoomMessage> sendPrivateMsgOrPsk(String senderId, String receiverId,
                                                                   String message, Connection conn)
            throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(SEND_PRIVATE_OR_PSK)) {
            preparedStatement.setString(1,senderId);
            preparedStatement.setString(2, receiverId);
            preparedStatement.setString(3,message);
            ResultSet resultSet = preparedStatement.executeQuery();
            return ChatRoomMessageFacade.getConnectionIdChatRoomMessageMap(resultSet);
        }
    }

}
