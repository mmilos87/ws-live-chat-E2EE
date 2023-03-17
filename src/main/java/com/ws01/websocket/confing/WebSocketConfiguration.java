package com.ws01.websocket.confing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    @Value("${application.socket.stage}")
    private String stage;
    @Value("${application.socket.allowedOrigin}")
    private String allowedOrigin;

    private final BaseWebSocketHandler baseWebSocketHandler;

    public WebSocketConfiguration(BaseWebSocketHandler baseWebSocketHandler) {
        this.baseWebSocketHandler = baseWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(baseWebSocketHandler, "/".concat(stage))
                .setAllowedOrigins(allowedOrigin);
    }
}
