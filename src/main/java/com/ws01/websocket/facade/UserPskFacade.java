package com.ws01.websocket.facade;

import com.ws01.websocket.messages.Psk;
import com.ws01.websocket.messages.PskUserMessage;
import com.ws01.websocket.messages.UsersPskMessage;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class UserPskFacade {
    private static final  String SAVE_USER_PSK = """
                    WITH cte_psk AS (
                        SELECT
                        CAST(? AS UUID) AS owner_id
                        , ? AS identity
                        , ? AS ephemeral
                    )
                    , cte_insert_psk AS (
                        INSERT INTO user_psk (
                            owner_id
                            , identity
                            , ephemeral
                        )
                        SELECT
                            owner_id
                            , identity
                            , ephemeral
                        FROM cte_psk
                        RETURNING *
                    )
                    SELECT *
                    FROM cte_insert_psk
                    UNION
                    SELECT *
                    FROM user_psk
                    WHERE owner_id = (
                        SELECT owner_id
                        FROM cte_psk
                    )""";

    private static final String GET_AND_DELETE_USER_PSK = """
                    DELETE FROM user_ps
                    WHERE id = (
                        SELECT id
                        FROM user_psk
                        WHERE owner_id = CAST(? AS UUID)
                        LIMIT 1
                        )
                    RETURNING *;""";

    private static final String DELETE_USER_PSK = """
                    DELETE FROM user_psk
                    WHERE owner_id = CAST(? AS UUID)
                        AND identity = ?
                        AND ephemeral = ?
                    RETURNING *;""";
    public UsersPskMessage storeUserPsk(String identity, String ephemeral, String ownerId, Connection conn)
            throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(SAVE_USER_PSK)){
            preparedStatement.setString(1, ownerId);
            preparedStatement.setString(2, identity);
            preparedStatement.setString(3, ephemeral);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Psk> pskList = Psk.getFromResultSet(resultSet);
            return new UsersPskMessage(ownerId,pskList);
        }
    }
    public PskUserMessage deleteUserPsk(String identity, String ephemeral, String ownerId, Connection conn)
            throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(DELETE_USER_PSK)){
            preparedStatement.setString(1, ownerId);
            preparedStatement.setString(2, identity);
            preparedStatement.setString(3, ephemeral);
            ResultSet resultSet = preparedStatement.executeQuery();
            return PskUserMessage.getOneFromResultSet(resultSet);
        }
    }
    public PskUserMessage getAndRemoveUserPsk(String ownerId, Connection conn) throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(GET_AND_DELETE_USER_PSK)) {
            preparedStatement.setString(1, ownerId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return PskUserMessage.getOneFromResultSet(resultSet);
        }
    }

}
