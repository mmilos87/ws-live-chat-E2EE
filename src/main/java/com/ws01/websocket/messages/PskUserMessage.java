package com.ws01.websocket.messages;


import java.sql.ResultSet;
import java.sql.SQLException;


public class PskUserMessage extends BaseMessage{
    private String userId;
    private String identity;
    private String ephemeral;

    public PskUserMessage(String userId, String identity, String ephemeral) {
        super("USER_PSK");
        this.userId =userId;
        this.identity = identity;
        this.ephemeral = ephemeral;
    }

    public static PskUserMessage getOneFromResultSet(ResultSet resultSet) throws SQLException {
        String userId=null;
        String identity=null;
        String ephemeral=null;
        while (resultSet.next()) {
            userId = resultSet.getString("owner_id");
            identity = resultSet.getString("identity");
            ephemeral = resultSet.getString("ephemeral");

        }
        return new PskUserMessage(userId,identity,ephemeral);
    }


}
