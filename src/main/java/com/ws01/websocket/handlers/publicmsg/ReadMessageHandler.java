package com.ws01.websocket.handlers.publicmsg;

import com.ws01.websocket.confing.RequestBodyActionHandler;
import com.ws01.websocket.confing.request.WsRequestBody;
import com.ws01.websocket.confing.session.WebSocketSessionContainer;
import com.ws01.websocket.facade.ChatRoomMessageUserFacade;
import com.ws01.websocket.messages.MessagesInfo;
import com.ws01.websocket.util.JDBCUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
@Component("readMessages")
@Log4j2
public class ReadMessageHandler implements RequestBodyActionHandler {
    private final WebSocketSessionContainer sessionContainer;
    private final JDBCUtil jdbcUtil;

    public ReadMessageHandler(WebSocketSessionContainer sessionContainer, JDBCUtil jdbcUtil) {
        this.sessionContainer = sessionContainer;
        this.jdbcUtil = jdbcUtil;
    }

    @Override
    public void handleRequest(WebSocketSession webSocketSession, WsRequestBody wsRequestBody) {
        try (Connection connection = jdbcUtil.connection()) {

            String userid = sessionContainer.getUserId(webSocketSession);
            List<Long> chatRoomMessageIds =wsRequestBody.getChatRoomMessageIds();
            ChatRoomMessageUserFacade chatRoomMessageUserFacade = new ChatRoomMessageUserFacade(connection);
            Map<String, MessagesInfo> stringMessageInfoMap =
                    chatRoomMessageUserFacade.updateReadAt(userid, chatRoomMessageIds);
            sessionContainer.sendMessageToSessionId(stringMessageInfoMap);


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }
}
