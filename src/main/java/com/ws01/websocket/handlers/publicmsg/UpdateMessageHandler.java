package com.ws01.websocket.handlers.publicmsg;

import com.ws01.websocket.confing.RequestBodyActionHandler;
import com.ws01.websocket.confing.request.WsRequestBody;
import com.ws01.websocket.confing.session.WebSocketSessionContainer;
import com.ws01.websocket.facade.ChatRoomMessageFacade;
import com.ws01.websocket.messages.ChatRoomMessage;
import com.ws01.websocket.util.JDBCUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Component("updateMessage")
@Log4j2
public class UpdateMessageHandler implements RequestBodyActionHandler {
    private final WebSocketSessionContainer sessionContainer;
    private final JDBCUtil jdbcUtil;

    public UpdateMessageHandler(WebSocketSessionContainer sessionContainer, JDBCUtil jdbcUtil) {
        this.sessionContainer = sessionContainer;
        this.jdbcUtil = jdbcUtil;
    }

    @Override
    public void handleRequest(WebSocketSession webSocketSession, WsRequestBody wsRequestBody) {
        try (Connection connection = jdbcUtil.connection()) {

            String userid = sessionContainer.getUserId(webSocketSession);
            Long messageId =  wsRequestBody.getMessageId();
            String msgFromBody = wsRequestBody.getMessage();
            ChatRoomMessageFacade chatRoomMessageFacade = new ChatRoomMessageFacade(connection);
            Map<String, ChatRoomMessage> chatRoomMesssageMap =
                    chatRoomMessageFacade.updateChatRoomMessage(userid, messageId, msgFromBody);
            sessionContainer.sendMessageToSessionId(chatRoomMesssageMap);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
