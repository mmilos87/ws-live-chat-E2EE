package com.ws01.websocket.handlers.privatemsg;

import com.ws01.websocket.confing.RequestBodyActionHandler;
import com.ws01.websocket.confing.request.WsRequestBody;
import com.ws01.websocket.confing.session.WebSocketSessionContainer;
import com.ws01.websocket.facade.UserPskFacade;
import com.ws01.websocket.messages.UsersPskMessage;
import com.ws01.websocket.util.JDBCUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.sql.Connection;
import java.sql.SQLException;

@Log4j2
@Component("userPskAdd")
public class UserPskAddHandler implements RequestBodyActionHandler {

    private final WebSocketSessionContainer sessionContainer;
    private final UserPskFacade userPskFacade;
    private final JDBCUtil jdbcUtil;

    public UserPskAddHandler(WebSocketSessionContainer sessionContainer, UserPskFacade userPskFacade, JDBCUtil jdbcUtil) {
        this.sessionContainer = sessionContainer;
        this.userPskFacade = userPskFacade;
        this.jdbcUtil = jdbcUtil;
    }

    @Override
    public void handleRequest(WebSocketSession session, WsRequestBody wsRequestBody) {
        try(Connection connection =jdbcUtil.connection()) {
            String userId = sessionContainer.getUserId(session);
            String ephemeral = wsRequestBody.getEphemeral();
            String identity = wsRequestBody.getIdentity();
            UsersPskMessage userPskMessage = userPskFacade.storeUserPsk(identity, ephemeral, userId, connection);
            sessionContainer.sendMessageToUserIdSessions(userId, userPskMessage);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
