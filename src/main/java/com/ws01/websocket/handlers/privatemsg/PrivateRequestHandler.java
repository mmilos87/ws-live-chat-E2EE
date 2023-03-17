package com.ws01.websocket.handlers.privatemsg;

import com.ws01.websocket.confing.RequestBodyActionHandler;
import com.ws01.websocket.confing.request.WsRequestBody;
import com.ws01.websocket.confing.session.WebSocketSessionContainer;
import com.ws01.websocket.facade.PrivateChatRoomFacade;
import com.ws01.websocket.messages.ChatRoomMessage;
import com.ws01.websocket.messages.PrivateRequestMessage;
import com.ws01.websocket.util.JDBCUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Log4j2
@Component("privateRequest")
public class PrivateRequestHandler implements RequestBodyActionHandler {
    private final WebSocketSessionContainer sessionContainer;
    private final JDBCUtil jdbcUtil;
    private final PrivateChatRoomFacade privateChatRoomFacade;

    public PrivateRequestHandler(WebSocketSessionContainer sessionContainer, JDBCUtil jdbcUtil,
                                 PrivateChatRoomFacade privateChatRoomFacade) {
        this.sessionContainer = sessionContainer;
        this.jdbcUtil = jdbcUtil;
        this.privateChatRoomFacade = privateChatRoomFacade;
    }


    @Override
    public void handleRequest(WebSocketSession webSocketSession, WsRequestBody wsRequestBody) {
        try(Connection connection = jdbcUtil.connection()) {

            String receiverUserId = wsRequestBody.getToUserId();
            String senderUserId = sessionContainer.getUserId(webSocketSession);
            String senderEphemeral = wsRequestBody.getEphemeral();
            String senderIdentity = wsRequestBody.getIdentity();
            String receiverIdentity = wsRequestBody.getIdentityPsk();
            String receiverEphemeral = wsRequestBody.getEphemeralPsk();
            String encMsg = wsRequestBody.getMessage();
            sessionContainer.addAcknowledge(senderUserId,receiverUserId,encMsg);
            PrivateRequestMessage privateRequestMessage =
                    new PrivateRequestMessage(senderEphemeral,senderIdentity,receiverEphemeral,receiverIdentity);
            String message = sessionContainer.getJsonText(privateRequestMessage);
            // todo only one private chat room for users, trigger function in db or ...
            Map<String, ChatRoomMessage> map =
                    privateChatRoomFacade.createPrivateChatRoom(senderUserId, receiverUserId, message,connection);
            sessionContainer.sendMessageToSessionId(map);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
