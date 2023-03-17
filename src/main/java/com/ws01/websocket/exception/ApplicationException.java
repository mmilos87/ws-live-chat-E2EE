package com.ws01.websocket.exception;

import com.ws01.websocket.messages.ExceptionMessage;

public abstract class ApplicationException extends RuntimeException{
    private final ExceptionMessage exceptionMessage;
    ApplicationException(String message) {
        super(message);
        exceptionMessage= new ExceptionMessage(message);
    }

    public ExceptionMessage getExceptionMessage() {
        return exceptionMessage;
    }
}
