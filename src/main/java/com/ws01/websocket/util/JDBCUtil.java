package com.ws01.websocket.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
@Component
public class JDBCUtil {
    @Value("${application.jdbc.dbUser}")
    private String dbUser;

    @Value("${application.jdbc.dbPort}")
    private String dbPort;
    @Value("${application.jdbc.dbPassword}")
    private String dbPassword;
    @Value("${application.jdbc.dbUrl}")
    private String dbUrl;

    @Value("${application.jdbc.dbname}")
    private String dbName;

    public Connection connection() throws SQLException {
        return DriverManager
                .getConnection(String.format("jdbc:postgresql://%s:%s/%s", dbUrl, dbPort, dbName), dbUser, dbPassword);
    }

}
