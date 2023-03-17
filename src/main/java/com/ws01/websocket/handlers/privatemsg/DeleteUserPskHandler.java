package com.ws01.websocket.handlers.privatemsg;

import com.ws01.websocket.confing.RequestBodyActionHandler;
import com.ws01.websocket.confing.request.WsRequestBody;
import com.ws01.websocket.confing.session.WebSocketSessionContainer;
import com.ws01.websocket.facade.UserPskFacade;
import com.ws01.websocket.messages.PskUserMessage;
import com.ws01.websocket.util.JDBCUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.sql.Connection;
import java.sql.SQLException;

@Log4j2
@Component("deleteUserPsk")
public class DeleteUserPskHandler implements RequestBodyActionHandler {
    private final WebSocketSessionContainer sessionContainer;
    private final JDBCUtil jdbcUtil;
    private final UserPskFacade userPskFacade;

    public DeleteUserPskHandler(WebSocketSessionContainer sessionContainer, JDBCUtil jdbcUtil,
                                UserPskFacade userPskFacade) {
        this.sessionContainer = sessionContainer;
        this.jdbcUtil = jdbcUtil;
        this.userPskFacade = userPskFacade;
    }

    @Override
    public void handleRequest(WebSocketSession webSocketSession, WsRequestBody wsRequestBody) {
        try(Connection connection = jdbcUtil.connection()) {

            String identity = wsRequestBody.getIdentity();
            String ephemeral = wsRequestBody.getEphemeral();
            String userId = sessionContainer.getUserId(webSocketSession);
            PskUserMessage removeUserPsk = userPskFacade.deleteUserPsk(identity,ephemeral, userId, connection);
            sessionContainer.sendMessageToUserIdSessions(userId,removeUserPsk);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
