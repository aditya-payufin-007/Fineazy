package com.hackathon.lending.bot.aiagent.exception;

/**
 * Exception thrown when rate limit is exceeded (429 Too Many Requests)
 */
public class RateLimitExceededException extends ToqanApiException {
    
    public RateLimitExceededException(String message) {
        super(message, 429);
    }
    
    public RateLimitExceededException(String message, Throwable cause) {
        super(message, 429, cause);
    }
}

