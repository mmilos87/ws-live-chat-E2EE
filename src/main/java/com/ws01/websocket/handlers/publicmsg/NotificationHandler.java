package com.ws01.websocket.handlers.publicmsg;

import com.ws01.websocket.confing.session.WebSocketSessionContainer;
import com.ws01.websocket.facade.ChatRoomMessageFacade;
import com.ws01.websocket.confing.RequestBodyActionHandler;
import com.ws01.websocket.confing.request.WsRequestBody;
import com.ws01.websocket.messages.Notifications;
import com.ws01.websocket.util.JDBCUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.sql.Connection;
import java.sql.SQLException;

@Component("notifications")
public class NotificationHandler implements RequestBodyActionHandler {

    private final WebSocketSessionContainer sessionContainer;
    private final JDBCUtil jdbcUtil;

    public NotificationHandler(WebSocketSessionContainer sessionContainer, JDBCUtil jdbcUtil) {
        this.sessionContainer = sessionContainer;
        this.jdbcUtil = jdbcUtil;
    }


    @Override
    public void handleRequest(WebSocketSession webSocketSession, WsRequestBody wsRequestBody) {
        try(Connection connection=jdbcUtil.connection()) {
            String userId = sessionContainer.getUserId(webSocketSession);
            ChatRoomMessageFacade chatRoomMessageFacade = new ChatRoomMessageFacade(connection);
            Notifications notifications = chatRoomMessageFacade.notReceivedMessagesPerRoom(userId);
            sessionContainer.sendToOneSession(webSocketSession,notifications);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


}
