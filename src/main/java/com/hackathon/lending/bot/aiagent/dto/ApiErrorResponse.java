package com.hackathon.lending.bot.aiagent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for API error responses
 */
public class ApiErrorResponse {
    
    @JsonProperty("message")
    private String message;
    
    public ApiErrorResponse() {
    }
    
    public ApiErrorResponse(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}

