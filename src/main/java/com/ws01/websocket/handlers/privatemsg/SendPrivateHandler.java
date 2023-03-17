package com.ws01.websocket.handlers.privatemsg;

import com.ws01.websocket.confing.RequestBodyActionHandler;
import com.ws01.websocket.confing.request.WsRequestBody;
import com.ws01.websocket.confing.session.WebSocketSessionContainer;
import com.ws01.websocket.facade.PrivateChatRoomFacade;
import com.ws01.websocket.messages.ChatRoomMessage;
import com.ws01.websocket.util.JDBCUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Log4j2
@Component("sendPrivate")
public class SendPrivateHandler implements RequestBodyActionHandler {
    private final WebSocketSessionContainer sessionContainer;
    private final JDBCUtil jdbcUtil;
    private final PrivateChatRoomFacade privateChatRoomFacade;


    public SendPrivateHandler(WebSocketSessionContainer sessionContainer, JDBCUtil jdbcUtil,
                              PrivateChatRoomFacade privateChatRoomFacade) {
        this.sessionContainer = sessionContainer;
        this.jdbcUtil = jdbcUtil;
        this.privateChatRoomFacade = privateChatRoomFacade;
    }

    @Override
    public void handleRequest(WebSocketSession session, WsRequestBody wsRequestBody) {
        try(Connection connection = jdbcUtil.connection()) {

            String receiverUserId = wsRequestBody.getToUserId();
            String senderUserId = sessionContainer.getUserId(session);
            String encMsg = wsRequestBody.getMessage();
            Map<String, ChatRoomMessage> map =
                    privateChatRoomFacade.sendPrivateMsgOrPsk(senderUserId, receiverUserId, encMsg, connection);
            sessionContainer.sendMessageToSessionId(map);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
