package com.ws01.websocket.confing;

import com.google.gson.Gson;
import com.ws01.websocket.confing.request.WsRequestBody;
import com.ws01.websocket.confing.security.WsQueryParamJwtFilter;
import com.ws01.websocket.confing.session.WebSocketSessionContainer;
import com.ws01.websocket.exception.ApplicationException;
import com.ws01.websocket.exception.ValidationException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
@Component
public class BaseWebSocketHandler extends AbstractWebSocketHandler {
    private final Gson gson;
    private final WsQueryParamJwtFilter jwtFilter;
    private final WebSocketSessionContainer webSocketSessionContainer;
    private final Map<String, RequestBodyActionHandler> handlers;

    public BaseWebSocketHandler(WebSocketSessionContainer webSocketSessionContainer,
                                Gson gson, WsQueryParamJwtFilter jwtFilter,
                                Map<String, RequestBodyActionHandler> handlers) {
        this.webSocketSessionContainer = webSocketSessionContainer;
        this.gson = gson;
        this.jwtFilter = jwtFilter;
        this.handlers = handlers;
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws IOException {
        WsRequestBody wsRequestBody = gson.fromJson(message.getPayload(), WsRequestBody.class);
        if(Objects.nonNull(wsRequestBody))
            Optional.ofNullable(getHandler(wsRequestBody))
                    .ifPresent(handler -> handler.handleRequest(session, wsRequestBody));
        else
            session.sendMessage(message);
    }

    @Override
    protected void handleBinaryMessage(@NonNull WebSocketSession session,@NonNull BinaryMessage message)
            throws IOException {
        session.sendMessage(message);
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        if(jwtFilter.filter(session)) webSocketSessionContainer.addWebSocketSession(session);
    }

    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message)
            throws Exception {
        try {
            super.handleMessage(session, message);
        }catch (ApplicationException exception){
            webSocketSessionContainer.sendToOneSession(session, exception.getExceptionMessage());
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull  WebSocketSession session, @NonNull CloseStatus status)
            throws Exception {
        super.afterConnectionClosed(session, status);
        webSocketSessionContainer.removeWebSocketSession(session);
    }

    private RequestBodyActionHandler getHandler(WsRequestBody wsRequestBody) {
        return Optional.ofNullable(handlers.get(wsRequestBody.getAction()))
                .orElseThrow(()->
                        new ValidationException("Action {%s} is not supported".formatted(wsRequestBody.getAction())));
    }

}
