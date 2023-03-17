package com.ws01.websocket.confing;

import com.ws01.websocket.confing.request.WsRequestBody;
import org.springframework.web.socket.WebSocketSession;

public interface RequestBodyActionHandler {

    void handleRequest(WebSocketSession session, WsRequestBody wsRequestBody);
}
