package com.ws01.websocket.handlers.privatemsg;

import com.ws01.websocket.confing.RequestBodyActionHandler;
import com.ws01.websocket.confing.request.WsRequestBody;
import com.ws01.websocket.confing.session.WebSocketSessionContainer;
import com.ws01.websocket.facade.PrivateChatRoomFacade;
import com.ws01.websocket.messages.AcknowledgeMessage;
import com.ws01.websocket.util.JDBCUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

@Log4j2
@Component("privateResponse")
public class PrivateResponseHandler implements RequestBodyActionHandler {
    private final WebSocketSessionContainer sessionContainer;
    private final JDBCUtil jdbcUtil;
    private final PrivateChatRoomFacade privateChatRoomFacade;


    public PrivateResponseHandler(WebSocketSessionContainer sessionContainer, JDBCUtil jdbcUtil,
                                  PrivateChatRoomFacade privateChatRoomFacade) {
        this.sessionContainer = sessionContainer;
        this.jdbcUtil = jdbcUtil;
        this.privateChatRoomFacade = privateChatRoomFacade;
    }

    @Override
    public void handleRequest(WebSocketSession session, WsRequestBody wsRequestBody) {
        try(Connection connection = jdbcUtil.connection()) {

            String receiverUserId = wsRequestBody.getToUserId();
            String senderUserId1 = wsRequestBody.getUserId();
            String senderUserId = sessionContainer.getUserId(session);
            String encMsg = wsRequestBody.getMessage();
            boolean handshake = sessionContainer.checkHandshake(receiverUserId, senderUserId1, encMsg);

            if(!handshake) sessionContainer.removeAcknowledge(receiverUserId, senderUserId1);

            privateChatRoomFacade.handshakeAction(receiverUserId,senderUserId1, handshake, connection);
            AcknowledgeMessage acknowledgeMessage = new AcknowledgeMessage(senderUserId1, receiverUserId, handshake);
            sessionContainer.sendMessageToUserIdSet(Set.of(senderUserId1, receiverUserId), acknowledgeMessage);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
