package com.ws01.websocket.facade;

import com.ws01.websocket.messages.OnlineStatus;

import java.sql.*;
import java.util.*;

public class UserConnection {

    private static final String SAVE_USER_CONNECTION =
            "INSERT INTO user_connection (user_id,connection_id) VALUES (CAST(? AS UUID),?)";

    private static final String DELETE_USER_CONNECTION =
            "DELETE FROM user_connection WHERE connection_id = ?";
    private static final String DELETE_ALL =
            "DELETE FROM user_connection";

    private static final String ONLINE_STATUS = """
                    WITH result_rooms1 AS (
                        SELECT
                            chat_room_id
                            , user_id
                        FROM chat_room_user
                        WHERE user_id= CAST(? AS UUID)
                    ), result_rooms2 AS (
                        SELECT
                            chat_room_id
                            , user_id
                        FROM chat_room_user
                        WHERE user_id = CAST(? AS UUID)
                    ), result_status AS (
                        SELECT
                            CASE
                                WHEN count(connection_id)>0 THEN TRUE
                                ELSE FALSE
                            END AS status
                        FROM user_connection
                        WHERE user_id = (
                            SELECT user_id
                            FROM result_rooms1
                            LIMIT 1
                        )
                    ), result_users AS(
                        SELECT  user_id
                        FROM chat_room_user
                        WHERE chat_room_id IN (
                            SELECT chat_room_id
                            FROM result_rooms1
                        )
                        GROUP BY user_id
                    )
                    SELECT
                        uc.connection_id
                        , (SELECT status FROM result_status)
                    FROM result_users ru
                    INNER JOIN user_connection uc
                        ON uc.user_id = ru.user_id
                    WHERE
                    EXISTS (
                        SELECT *
                        FROM result_rooms1 r1
                        INNER JOIN result_rooms2 r2
                            ON r2.chat_room_id = r1.chat_room_id
                    )""";
    private final Connection conn;

    public UserConnection(Connection conn) {
        this.conn = conn;
    }

    public boolean storeConnectionId(String connectionId, String userId ) throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(SAVE_USER_CONNECTION)){
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, connectionId);
            preparedStatement.executeUpdate();
            return true;
        }
    }

    public Map<String, OnlineStatus> userOnlineStatus(String userIdAsk, String userIdCheck) throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(ONLINE_STATUS)){
            preparedStatement.setString(1, userIdCheck);
            preparedStatement.setString(2, userIdAsk);
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String,OnlineStatus> result = new HashMap<>();
            while (resultSet.next()) {
                String connectionId = resultSet.getString("connection_id");
                boolean status = resultSet.getBoolean("status");
                result.put(connectionId,new OnlineStatus(userIdCheck,status));
            }
            return result;
        }
    }

    public boolean removeConnectionId(String connectionId) throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(DELETE_USER_CONNECTION)) {
            preparedStatement.setString(1, connectionId);
            preparedStatement.executeUpdate();
            return true;
        }
    }

    public boolean removeAll() throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(DELETE_ALL)) {
            preparedStatement.executeUpdate();
            return true;
        }
    }


}
