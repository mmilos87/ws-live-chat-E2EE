package com.ws01.websocket.confing.request;

import com.ws01.websocket.exception.ValidationException;


import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class WsRequestBody implements Serializable, Cloneable {
    @Serial
    private static final long serialVersionUID = 362498820763181265L;
    private String action;
    private String userId;
    private String userToAddId;
    private String toUserId;
    private String message;
    private String chatRoomName;
    private String ephemeral;
    private String ephemeralPsk;
    private String identity;
    private String identityPsk;

    public String getEphemeralPsk() {
        return ephemeralPsk;
    }

    public String getIdentityPsk() {
        return identityPsk;
    }

    private String hmac;
    private Long chatRoomId;
    private Long messageId;
    private Long fromMessageId;
    private Boolean adminRole;
    private Boolean accept;

    private List<Long> chatRoomMessageIds;

    public String getHmac() {
        return hmac;
    }

    public String getUserId() {
        return Optional.ofNullable(userId)
                .orElseThrow(() -> new ValidationException("The userId field is NULL."));
    }

    public String getUserToAddId() {
        return userToAddId;
    }

    public String getEphemeral() {
        return ephemeral;
    }

    public String getToUserId() {
        return toUserId;
    }

    public String getIdentity() {
        return identity;
    }

    public String getMessage() {
        return Optional.ofNullable(message)
                .orElseThrow(() -> new ValidationException("The message field is NULL."));
    }

    public String getChatRoomName() {
        return chatRoomName;
    }


    public Long getChatRoomId() {
        return Optional.ofNullable(chatRoomId)
                .orElseThrow(() -> new ValidationException("The chatRoomId field is NULL."));
    }


    public Long getMessageId() {
        return Optional.ofNullable(messageId)
                .orElseThrow(() -> new ValidationException("The messageId field is NULL"));

    }


    public Long getFromMessageId() {
        return Optional.ofNullable(fromMessageId).orElse(Long.MIN_VALUE);
    }

    public List<Long> getChatRoomMessageIds() {
        return Optional.ofNullable(chatRoomMessageIds)
                .orElse(Collections.emptyList());
    }

    public String getAction() {
        return Optional.ofNullable(action)
                .orElseThrow(() -> new ValidationException("The action field is NULL"));

    }

    public String getUserIdToAdd() {
        return userToAddId;
    }


    public Boolean getAdminRole() {
        return adminRole;
    }


    public Boolean getAccept() {
        return accept;
    }


}
