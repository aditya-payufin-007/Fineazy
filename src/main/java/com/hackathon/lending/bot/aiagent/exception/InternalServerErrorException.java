package com.hackathon.lending.bot.aiagent.exception;

/**
 * Exception thrown when there's an internal server error (500)
 */
public class InternalServerErrorException extends ToqanApiException {
    
    public InternalServerErrorException(String message) {
        super(message, 500);
    }
    
    public InternalServerErrorException(String message, Throwable cause) {
        super(message, 500, cause);
    }
}

