package com.hackathon.lending.bot.aiagent.exception;

/**
 * Exception thrown when a resource is not found (404 Not Found)
 */
public class NotFoundException extends ToqanApiException {
    
    public NotFoundException(String message) {
        super(message, 404);
    }
    
    public NotFoundException(String message, Throwable cause) {
        super(message, 404, cause);
    }
}

