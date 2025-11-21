package com.hackathon.lending.bot.aiagent.service;

import com.hackathon.lending.bot.aiagent.client.ToqanApiClient;
import com.hackathon.lending.bot.aiagent.dto.*;
import com.hackathon.lending.bot.aiagent.exception.ToqanApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

/**
 * Service for managing conversations with Toqan AI Agent
 */
@Service
public class ToqanConversationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ToqanConversationService.class);
    
    private final ToqanApiClient apiClient;
    
    @Value("${toqan.polling.max-attempts:20}")
    private int maxPollingAttempts;
    
    @Value("${toqan.polling.interval-ms:2000}")
    private long pollingIntervalMs;
    
    public ToqanConversationService(ToqanApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Create a new conversation with a user message
     *
     * @param userMessage The message from the user
     * @return CreateConversationResponse containing conversation_id and request_id
     * @throws ToqanApiException if conversation creation fails
     */
    public CreateConversationResponse createConversation(String userMessage) 
            throws ToqanApiException {
        
        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("User message cannot be null or empty");
        }
        
        CreateConversationRequest request = new CreateConversationRequest(userMessage.trim());
        return apiClient.createConversation(request);
    }
    
    /**
     * Create a new conversation with a user message and files
     *
     * @param userMessage The message from the user
     * @param files List of private user files to attach
     * @return CreateConversationResponse containing conversation_id and request_id
     * @throws ToqanApiException if conversation creation fails
     */
    public CreateConversationResponse createConversation(String userMessage, 
            List<CreateConversationRequest.PrivateUserFile> files) throws ToqanApiException {
        
        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("User message cannot be null or empty");
        }
        
        CreateConversationRequest request = new CreateConversationRequest(
                userMessage.trim(), files);
        return apiClient.createConversation(request);
    }
    
    /**
     * Get the answer for a specific conversation and request
     *
     * @param conversationId The conversation ID
     * @param requestId The request ID
     * @return GetAnswerResponse with status and answer
     * @throws ToqanApiException if retrieval fails
     */
    public GetAnswerResponse getAnswer(String conversationId, String requestId) 
            throws ToqanApiException {
        
        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Conversation ID cannot be null or empty");
        }
        
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be null or empty");
        }
        
        return apiClient.getAnswer(conversationId, requestId);
    }
    
    /**
     * Poll for answer until it's ready or max attempts reached
     *
     * @param conversationId The conversation ID
     * @param requestId The request ID
     * @return GetAnswerResponse with the final answer
     * @throws ToqanApiException if retrieval fails
     * @throws TimeoutException if max polling attempts exceeded
     */
    public GetAnswerResponse pollForAnswer(String conversationId, String requestId) 
            throws ToqanApiException, TimeoutException {
        
        logger.info("Starting to poll for answer. ConversationId: {}, RequestId: {}", 
                conversationId, requestId);
        
        int attempts = 0;
        
        while (attempts < maxPollingAttempts) {
            try {
                GetAnswerResponse response = getAnswer(conversationId, requestId);
                
                if (response.isFinished()) {
                    logger.info("Answer is ready after {} attempts", attempts + 1);
                    return response;
                }
                
                if (response.isError()) {
                    logger.error("Error status received from AI agent: {}", response.getAnswer());
                    throw new ToqanApiException("AI agent returned error: " + response.getAnswer(), 
                            500);
                }
                
                logger.debug("Answer not ready yet (attempt {}/{}). Status: {}", 
                        attempts + 1, maxPollingAttempts, response.getStatus());
                
                // Wait before next attempt
                Thread.sleep(pollingIntervalMs);
                attempts++;
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Polling interrupted", e);
                throw new ToqanApiException("Polling interrupted", 500, e);
            }
        }
        
        logger.warn("Max polling attempts ({}) exceeded for conversationId: {}, requestId: {}", 
                maxPollingAttempts, conversationId, requestId);
        throw new TimeoutException("Max polling attempts exceeded. Answer not ready yet.");
    }
    
    /**
     * Create a conversation and wait for the answer
     *
     * @param userMessage The message from the user
     * @return GetAnswerResponse with the final answer
     * @throws ToqanApiException if any API call fails
     * @throws TimeoutException if polling timeout is exceeded
     */
    public GetAnswerResponse createConversationAndGetAnswer(String userMessage) 
            throws ToqanApiException, TimeoutException {
        
        logger.info("Creating conversation and waiting for answer");
        
        // Create conversation
        CreateConversationResponse conversation = createConversation(userMessage);
        
        // Poll for answer
        return pollForAnswer(conversation.getConversationId(), 
                conversation.getRequestId());
    }
    
    /**
     * Create a conversation with files and wait for the answer
     *
     * @param userMessage The message from the user
     * @param files List of private user files to attach
     * @return GetAnswerResponse with the final answer
     * @throws ToqanApiException if any API call fails
     * @throws TimeoutException if polling timeout is exceeded
     */
    public GetAnswerResponse createConversationAndGetAnswer(String userMessage, 
            List<CreateConversationRequest.PrivateUserFile> files) 
            throws ToqanApiException, TimeoutException {
        
        logger.info("Creating conversation with {} files and waiting for answer", 
                files != null ? files.size() : 0);
        
        // Create conversation
        CreateConversationResponse conversation = createConversation(userMessage, files);
        
        // Poll for answer
        return pollForAnswer(conversation.getConversationId(), 
                conversation.getRequestId());
    }
    
    /**
     * Create a conversation and get answer asynchronously
     *
     * @param userMessage The message from the user
     * @return CompletableFuture with GetAnswerResponse
     */
    public CompletableFuture<GetAnswerResponse> createConversationAndGetAnswerAsync(
            String userMessage) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return createConversationAndGetAnswer(userMessage);
            } catch (Exception e) {
                logger.error("Error in async conversation creation", e);
                throw new CompletionException(e);
            }
        });
    }
}

