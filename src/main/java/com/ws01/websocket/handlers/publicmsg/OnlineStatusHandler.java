package com.ws01.websocket.handlers.publicmsg;

import com.ws01.websocket.confing.RequestBodyActionHandler;
import com.ws01.websocket.confing.session.WebSocketSessionContainer;
import com.ws01.websocket.facade.UserConnection;
import com.ws01.websocket.confing.request.WsRequestBody;
import com.ws01.websocket.messages.OnlineStatus;
import com.ws01.websocket.util.JDBCUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
@Component("onlineStatus")
@Log4j2
public class OnlineStatusHandler implements RequestBodyActionHandler {
    private final WebSocketSessionContainer sessionContainer;
    private final JDBCUtil jdbcUtil;

    public OnlineStatusHandler(WebSocketSessionContainer sessionContainer, JDBCUtil jdbcUtil) {
        this.sessionContainer = sessionContainer;
        this.jdbcUtil = jdbcUtil;
    }


    @Override
    public void handleRequest(WebSocketSession  webSocketSession, WsRequestBody wsRequestBody) {

        try (Connection connection = jdbcUtil.connection()) {
            String userIdAsk = sessionContainer.getUserId(webSocketSession);
            String userIdCheck = wsRequestBody.getUserId();
            UserConnection userConnection = new UserConnection(connection);
            Map<String, OnlineStatus> onlineStatusMap = userConnection.userOnlineStatus(userIdAsk, userIdCheck);
            OnlineStatus onlineStatus = onlineStatusMap.values().stream().findFirst().orElseThrow();
            sessionContainer.sendOneMessageToManySessions(onlineStatusMap.keySet(), onlineStatus);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

}
