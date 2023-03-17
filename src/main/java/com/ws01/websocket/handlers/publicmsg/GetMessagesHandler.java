package com.ws01.websocket.handlers.publicmsg;

import com.ws01.websocket.confing.RequestBodyActionHandler;
import com.ws01.websocket.confing.request.WsRequestBody;
import com.ws01.websocket.confing.session.WebSocketSessionContainer;
import com.ws01.websocket.facade.ChatRoomMessageUserFacade;
import com.ws01.websocket.messages.ChatRoomMessage;
import com.ws01.websocket.util.JDBCUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Component("getMessages")
@Log4j2
public class GetMessagesHandler implements RequestBodyActionHandler {
    private final WebSocketSessionContainer sessionContainer;
    private final JDBCUtil jdbcUtil;

    public GetMessagesHandler(WebSocketSessionContainer sessionContainer, JDBCUtil jdbcUtil) {
        this.sessionContainer = sessionContainer;
        this.jdbcUtil = jdbcUtil;
    }

    @Override
    public void handleRequest(WebSocketSession webSocketSession, WsRequestBody wsRequestBody) {
        try(Connection connection = jdbcUtil.connection()) {
            String userid = sessionContainer.getUserId(webSocketSession);
            Long chatRoomId = wsRequestBody.getChatRoomId();
            Long fromMsgId = wsRequestBody.getFromMessageId();
            ChatRoomMessageUserFacade chatRoomMessageUserFacade = new ChatRoomMessageUserFacade(connection);
            List<ChatRoomMessage> chatRoomMessages =
                    chatRoomMessageUserFacade.getChatRoomMessagesPageable(userid, chatRoomId, fromMsgId);
            sessionContainer.sendToOneSession(webSocketSession, chatRoomMessages);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

}
