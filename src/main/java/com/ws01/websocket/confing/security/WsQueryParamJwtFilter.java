package com.ws01.websocket.confing.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Objects;

@Component
public class WsQueryParamJwtFilter {
    private final CognitoJwtProcessor cognitoJwtProcessor;

    @Value("${application.jwt.query.param}")
    private  String queryParam;

    public WsQueryParamJwtFilter(CognitoJwtProcessor cognitoJwtProcessor) {
        this.cognitoJwtProcessor = cognitoJwtProcessor;
    }
   public boolean filter(WebSocketSession webSocketSession) throws IOException {
       String uriQuery = webSocketSession.getUri().getQuery();
       if (Objects.isNull(uriQuery)){
           webSocketSession.close(new CloseStatus(1008)
                   .withReason("Unauthorized, Query param is null"));

           return false;

       }

       String jwt = uriQuery.substring(queryParam.replaceFirst("=","").length()+1);

       try {
           String sub = cognitoJwtProcessor.getSub(jwt);

           if (Objects.isNull(sub)){
               webSocketSession.close(new CloseStatus(1008)
                       .withReason("Unauthorized, token is not valid"));

               return false;

           }

           webSocketSession.getAttributes().put("sub",sub);

           return true;

       }catch (JwtValidationException e){
           webSocketSession.close(new CloseStatus(1008)
                   .withReason("Unauthorized, token is not valid"));

           return false;

       }

   }

}
