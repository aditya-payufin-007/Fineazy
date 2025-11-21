package com.hackathon.lending.bot.aiagent.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.lending.bot.aiagent.dto.*;
import com.hackathon.lending.bot.aiagent.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP Client for interacting with Toqan AI Agent APIs
 */
@Component
public class ToqanApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ToqanApiClient.class);
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Value("${toqan.api.base-url}")
    private String baseUrl;
    
    @Value("${toqan.api.key}")
    private String apiKey;
    
    @Value("${toqan.api.timeout:30}")
    private int timeoutSeconds;
    
    public ToqanApiClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }
    
    /**
     * Create a new conversation with the AI agent
     *
     * @param request CreateConversationRequest containing user message and optional files
     * @return CreateConversationResponse with conversation_id and request_id
     * @throws ToqanApiException if the API call fails
     */
    public CreateConversationResponse createConversation(CreateConversationRequest request) 
            throws ToqanApiException {
        
        logger.info("Creating new conversation with message: {}", 
                request.getUserMessage().substring(0, Math.min(50, request.getUserMessage().length())));
        
        try {
            String requestBody = objectMapper.writeValueAsString(request);
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/create_conversation"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, 
                    HttpResponse.BodyHandlers.ofString());
            
            handleResponseStatus(response);
            
            CreateConversationResponse result = objectMapper.readValue(
                    response.body(), CreateConversationResponse.class);
            
            logger.info("Successfully created conversation with ID: {}", result.getConversationId());
            return result;
            
        } catch (ToqanApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating conversation", e);
            throw new ToqanApiException("Failed to create conversation: " + e.getMessage(), 
                    500, e);
        }
    }
    
    /**
     * Get answer for a specific conversation and request
     *
     * @param conversationId The conversation ID
     * @param requestId The request ID
     * @return GetAnswerResponse with status, answer, timestamp, and attachments
     * @throws ToqanApiException if the API call fails
     */
    public GetAnswerResponse getAnswer(String conversationId, String requestId) 
            throws ToqanApiException {
        
        logger.info("Getting answer for conversation: {}, request: {}", conversationId, requestId);
        
        try {
            String url = String.format("%s/api/get_answer?conversation_id=%s&request_id=%s",
                    baseUrl, conversationId, requestId);
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "*/*")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, 
                    HttpResponse.BodyHandlers.ofString());
            
            handleResponseStatus(response);
            
            GetAnswerResponse result = objectMapper.readValue(
                    response.body(), GetAnswerResponse.class);
            
            logger.info("Successfully retrieved answer with status: {}", result.getStatus());
            return result;
            
        } catch (ToqanApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error getting answer", e);
            throw new ToqanApiException("Failed to get answer: " + e.getMessage(), 
                    500, e);
        }
    }
    
    /**
     * Handle HTTP response status codes and throw appropriate exceptions
     */
    private void handleResponseStatus(HttpResponse<String> response) throws ToqanApiException {
        int statusCode = response.statusCode();
        
        if (statusCode >= 200 && statusCode < 300) {
            return; // Success
        }
        
        String errorMessage = extractErrorMessage(response.body());
        
        switch (statusCode) {
            case 400:
                logger.warn("Bad Request: {}", errorMessage);
                throw new BadRequestException(errorMessage);
                
            case 401:
                logger.error("Unauthorized: {}", errorMessage);
                throw new UnauthorizedException(errorMessage);
                
            case 403:
                logger.warn("Forbidden: {}", errorMessage);
                throw new ForbiddenException(errorMessage);
                
            case 404:
                logger.warn("Not Found: {}", errorMessage);
                throw new NotFoundException(errorMessage);
                
            case 429:
                logger.warn("Rate Limit Exceeded: {}", errorMessage);
                throw new RateLimitExceededException(errorMessage);
                
            case 500:
            case 502:
            case 503:
            case 504:
                logger.error("Internal Server Error: {}", errorMessage);
                throw new InternalServerErrorException(errorMessage);
                
            default:
                logger.error("Unexpected error status {}: {}", statusCode, errorMessage);
                throw new ToqanApiException(errorMessage, statusCode);
        }
    }
    
    /**
     * Extract error message from response body
     */
    private String extractErrorMessage(String responseBody) {
        try {
            // Try to parse as JSON error response
            ApiErrorResponse errorResponse = objectMapper.readValue(
                    responseBody, ApiErrorResponse.class);
            return errorResponse.getMessage();
        } catch (Exception e) {
            // If parsing fails, return raw response body
            return responseBody != null && !responseBody.isEmpty() 
                    ? responseBody 
                    : "Unknown error occurred";
        }
    }
}

