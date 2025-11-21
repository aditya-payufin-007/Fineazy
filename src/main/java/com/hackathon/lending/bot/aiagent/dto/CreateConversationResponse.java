package com.hackathon.lending.bot.aiagent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for conversation creation
 */
public class CreateConversationResponse {
    
    @JsonProperty("conversation_id")
    private String conversationId;
    
    @JsonProperty("request_id")
    private String requestId;
    
    public CreateConversationResponse() {
    }
    
    public CreateConversationResponse(String conversationId, String requestId) {
        this.conversationId = conversationId;
        this.requestId = requestId;
    }
    
    public String getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}

