package com.ws01.websocket.messages;

public class ExceptionMessage extends BaseMessage{
    private String message;
    public ExceptionMessage( String message) {
        super("EXCEPTION");
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
