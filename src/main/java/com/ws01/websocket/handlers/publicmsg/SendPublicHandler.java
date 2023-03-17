package com.ws01.websocket.handlers.publicmsg;

import com.ws01.websocket.confing.RequestBodyActionHandler;
import com.ws01.websocket.confing.request.WsRequestBody;
import com.ws01.websocket.confing.session.WebSocketSessionContainer;
import com.ws01.websocket.facade.ChatRoomMessageFacade;
import com.ws01.websocket.facade.NotificationFacade;
import com.ws01.websocket.messages.ChatRoomMessage;
import com.ws01.websocket.util.JDBCUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Component("sendPublic")
@Log4j2
public class SendPublicHandler implements RequestBodyActionHandler {
    private final WebSocketSessionContainer sessionContainer;
    private final JDBCUtil jdbcUtil;

    private final NotificationFacade notificationFacade;

    public SendPublicHandler(WebSocketSessionContainer sessionContainer, JDBCUtil jdbcUtil,
                             NotificationFacade notificationFacade) {
        this.sessionContainer = sessionContainer;
        this.jdbcUtil = jdbcUtil;
        this.notificationFacade = notificationFacade;
    }

    @Override
    public void handleRequest(WebSocketSession webSocketSession, WsRequestBody wsRequestBody) {
        try(Connection connection = jdbcUtil.connection()) {

            String userid = sessionContainer.getUserId(webSocketSession);
            ChatRoomMessageFacade chatRoomMessageFacade = new ChatRoomMessageFacade(connection);
            Long chatRoomId = wsRequestBody.getChatRoomId();
            String message = wsRequestBody.getMessage();
            Map<String, ChatRoomMessage> chatRoomMessageMap =
                    chatRoomMessageFacade.saveChatRoomMessage(userid, chatRoomId, message);
            ChatRoomMessage chatRoomMessage =
                    chatRoomMessageMap.entrySet()
                            .stream().findFirst()
                            .orElseThrow().getValue();
            sessionContainer
                    .sendOneMessageToManySessions(chatRoomMessageMap.keySet(), chatRoomMessage);

            String sendNotification = notificationFacade.sendNotification(userid, chatRoomMessage.getMessageId());
            log.info("Notification: %s".formatted(sendNotification));


        } catch (SQLException | URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
