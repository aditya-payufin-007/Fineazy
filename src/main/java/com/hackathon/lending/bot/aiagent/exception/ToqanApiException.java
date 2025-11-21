package com.hackathon.lending.bot.aiagent.exception;

/**
 * Base exception for all Toqan AI Agent related exceptions
 */
public class ToqanApiException extends RuntimeException {
    
    private final int statusCode;
    
    public ToqanApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public ToqanApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}

