package com.hackathon.lending.bot.aiagent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for getting answer from Toqan AI Agent
 */
public class GetAnswerResponse {
    
    @JsonProperty("status")
    private String status; // "in_progress", "error", or "finished"
    
    @JsonProperty("answer")
    private String answer;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("attachments")
    private List<Attachment> attachments;
    
    public GetAnswerResponse() {
    }
    
    public GetAnswerResponse(String status, String answer, LocalDateTime timestamp, List<Attachment> attachments) {
        this.status = status;
        this.answer = answer;
        this.timestamp = timestamp;
        this.attachments = attachments;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public List<Attachment> getAttachments() {
        return attachments;
    }
    
    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
    
    /**
     * Check if the answer is ready
     */
    public boolean isFinished() {
        return "finished".equalsIgnoreCase(status);
    }
    
    /**
     * Check if there was an error
     */
    public boolean isError() {
        return "error".equalsIgnoreCase(status);
    }
    
    /**
     * Check if the request is still in progress
     */
    public boolean isInProgress() {
        return "in_progress".equalsIgnoreCase(status);
    }
    
    /**
     * Inner class representing an attachment
     */
    public static class Attachment {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("mime_type")
        private String mimeType;
        
        public Attachment() {
        }
        
        public Attachment(String name, String mimeType) {
            this.name = name;
            this.mimeType = mimeType;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getMimeType() {
            return mimeType;
        }
        
        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }
    }
}

