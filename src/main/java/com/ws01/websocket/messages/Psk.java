package com.ws01.websocket.messages;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Psk extends BaseMessage{

    private String identity;
    private String ephemeral;

    public Psk( String identity, String ephemeral) {
        super("PSK");
        this.identity = identity;
        this.ephemeral = ephemeral;
    }

    public static List<Psk> getFromResultSet(ResultSet resultSet) throws SQLException {
        List<Psk>  pskList= new ArrayList<>();
        while (resultSet.next()) {
            String identity = resultSet.getString("identity");
            String ephemeral = resultSet.getString("ephemeral");
            pskList.add(new Psk(identity,ephemeral));
        }
        return pskList;
    }

}
