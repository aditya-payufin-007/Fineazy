package com.hackathon.lending.bot.aiagent.exception;

/**
 * Exception thrown when authentication fails (401 Unauthorized)
 */
public class UnauthorizedException extends ToqanApiException {
    
    public UnauthorizedException(String message) {
        super(message, 401);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, 401, cause);
    }
}

