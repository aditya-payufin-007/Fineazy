package com.hackathon.lending.bot.aiagent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Request DTO for creating a new conversation with Toqan AI Agent
 */
public class CreateConversationRequest {
    
    @JsonProperty("user_message")
    private String userMessage;
    
    @JsonProperty("private_user_files")
    private List<PrivateUserFile> privateUserFiles;
    
    public CreateConversationRequest() {
    }
    
    public CreateConversationRequest(String userMessage) {
        this.userMessage = userMessage;
        this.privateUserFiles = List.of();
    }
    
    public CreateConversationRequest(String userMessage, List<PrivateUserFile> privateUserFiles) {
        this.userMessage = userMessage;
        this.privateUserFiles = privateUserFiles;
    }
    
    // Getters and Setters
    public String getUserMessage() {
        return userMessage;
    }
    
    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }
    
    public List<PrivateUserFile> getPrivateUserFiles() {
        return privateUserFiles;
    }
    
    public void setPrivateUserFiles(List<PrivateUserFile> privateUserFiles) {
        this.privateUserFiles = privateUserFiles;
    }
    
    /**
     * Inner class representing a private user file
     */
    public static class PrivateUserFile {
        private String name;
        private String url;
        private String mimeType;
        
        public PrivateUserFile() {
        }
        
        public PrivateUserFile(String name, String url, String mimeType) {
            this.name = name;
            this.url = url;
            this.mimeType = mimeType;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getMimeType() {
            return mimeType;
        }
        
        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }
    }
}

