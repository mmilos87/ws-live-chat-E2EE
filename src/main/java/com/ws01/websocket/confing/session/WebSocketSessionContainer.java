package com.ws01.websocket.confing.session;

import com.google.gson.Gson;
import com.ws01.websocket.facade.UserConnection;
import com.ws01.websocket.util.JDBCUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class WebSocketSessionContainer {


    private final Set<WebSocketSession> webSocketSessions = new HashSet<>();
    private final Set<Acknowledge> acknowledges = new HashSet<>();
    private final Gson gson;

    private final JDBCUtil jdbcUtil;
    public WebSocketSessionContainer(Gson gson, JDBCUtil jdbcUtil) {
        this.gson = gson;
        this.jdbcUtil = jdbcUtil;
        deleteOldSessionsFromDb();
    }

    public Set<WebSocketSession> getWebSocketSessions() {
        return webSocketSessions;
    }

    public void addWebSocketSession(WebSocketSession webSocketSession) {
        try(Connection connection= jdbcUtil.connection()) {
            UserConnection  userConnection= new UserConnection(connection);
            String sub =getUserId(webSocketSession);
            if(userConnection.storeConnectionId(webSocketSession.getId(), sub)) {
                this.webSocketSessions.add(webSocketSession);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public boolean addAcknowledge(String toUserId, String fromUserId, String encMsg) {
       return acknowledges.add(new Acknowledge(fromUserId, toUserId, encMsg));
    }

    public boolean checkHandshake(String userId1, String userId2, String encMsg) {
        return acknowledges
                .removeIf(acknowledge ->  acknowledge.equals(userId1, userId2, encMsg));

    }
    public boolean removeAcknowledge(String userId1, String userId2) {
        return acknowledges
                .removeIf(acknowledge ->  Set.of(userId1, userId2).containsAll(acknowledge.getUserIds()));

    }


    public void removeWebSocketSession(WebSocketSession webSocketSession) {
        try(Connection connection= jdbcUtil.connection()) {
            UserConnection  userConnection= new UserConnection(connection);
            if(userConnection.removeConnectionId(webSocketSession.getId())) {
                this.webSocketSessions.remove(webSocketSession);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
     }

    public void sendToOneSession(WebSocketSession webSocketSession, TextMessage msg) {
        try (Connection connection = jdbcUtil.connection()) {
            UserConnection userConnection = new UserConnection(connection);
            sendToSession(webSocketSession, msg, userConnection);
        } catch (SQLException e) {
            //todo exception handling
            throw new RuntimeException(e);
        }
    }

    public <M> void sendToOneSession(WebSocketSession webSocketSession, M msg) {
        TextMessage textMessage = getTextMessage(msg);
        sendToOneSession(webSocketSession,textMessage);
    }

    private void sendToSession(WebSocketSession webSocketSession, TextMessage msg, UserConnection userConnection)
            throws SQLException {
        try {
            if (webSocketSession.isOpen())
                webSocketSession.sendMessage(msg);
            else
                removeSession(userConnection, webSocketSession);

        } catch (IOException exception) {
            removeSession(userConnection, webSocketSession);

        }
    }

    public <M> void sendMessageToSessionId(Map<String, M> mapSessionIdMsg){
        Map<WebSocketSession, TextMessage> webSocketSessionTextMessageMap =
                webSocketSessions.parallelStream()
                        .filter(webSocketSession -> mapSessionIdMsg.containsKey(webSocketSession.getId()))
                            .collect(Collectors.toMap(Function.identity(),
                                    webSocketSession ->
                                            getTextMessage(mapSessionIdMsg.get(webSocketSession.getId()))));

        webSocketSessionTextMessageMap
                .entrySet().parallelStream()
                .forEach(entry -> sendToOneSession(entry.getKey(), entry.getValue()));

    }

    public <M> void sendOneMessageToManySessions(Set<String> wsSessionIds, M msg){
        TextMessage textMessage = getTextMessage(msg);
        webSocketSessions.parallelStream()
                .filter(webSocketSession -> wsSessionIds.contains(webSocketSession.getId()))
                .forEach(webSocketSession -> sendToOneSession(webSocketSession,textMessage));
    }

    public <M> void sendMessageToUserIdSessions(String userId, M msg){
        sendMessageToUserIdSet(Set.of(userId),msg);
    }

    public <M> void sendMessageToUserIdSet(Set<String> userIdSet, M msg){
        TextMessage textMessage = getTextMessage(msg);
        webSocketSessions.parallelStream()
                .filter(session -> userIdSet.contains(getUserId(session)))
                .forEach(webSocketSession -> sendToOneSession(webSocketSession,textMessage));
    }

    private void removeSession(UserConnection  userConnection, WebSocketSession webSocketSession)
            throws SQLException {
        userConnection.removeConnectionId(webSocketSession.getId());
        removeWebSocketSession(webSocketSession);
    }

    public <M> TextMessage getTextMessage(M m) {
        return new TextMessage(gson.toJson(m));

    }

    public <M> String getJsonText(M m) {
        return gson.toJson(m);

    }

    public String getUserId(WebSocketSession webSocketSession){
        return  (String) webSocketSession.getAttributes().get("sub");
    }

    private void deleteOldSessionsFromDb() {
     try(Connection connection = jdbcUtil.connection()){
         UserConnection userConnection = new UserConnection(connection);
         userConnection.removeAll();
     } catch (SQLException e) {
         throw new RuntimeException(e);
     }
    }
}
