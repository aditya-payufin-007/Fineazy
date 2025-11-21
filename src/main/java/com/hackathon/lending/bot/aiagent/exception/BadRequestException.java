package com.hackathon.lending.bot.aiagent.exception;

/**
 * Exception thrown when the API request is invalid (400 Bad Request)
 */
public class BadRequestException extends ToqanApiException {
    
    public BadRequestException(String message) {
        super(message, 400);
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(message, 400, cause);
    }
}

