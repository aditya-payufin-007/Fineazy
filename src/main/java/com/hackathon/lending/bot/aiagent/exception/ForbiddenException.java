package com.hackathon.lending.bot.aiagent.exception;

/**
 * Exception thrown when access is forbidden (403 Forbidden)
 */
public class ForbiddenException extends ToqanApiException {
    
    public ForbiddenException(String message) {
        super(message, 403);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, 403, cause);
    }
}

