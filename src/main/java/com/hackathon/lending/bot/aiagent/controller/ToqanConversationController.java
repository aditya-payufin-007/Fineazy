package com.hackathon.lending.bot.aiagent.controller;

import com.hackathon.lending.bot.aiagent.dto.*;
import com.hackathon.lending.bot.aiagent.exception.*;
import com.hackathon.lending.bot.aiagent.service.ToqanConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * REST Controller for Toqan AI Agent conversation management
 */
@RestController
@RequestMapping("/api/aiagent")
public class ToqanConversationController {
    
    private static final Logger logger = LoggerFactory.getLogger(ToqanConversationController.class);
    
    private final ToqanConversationService conversationService;
    
    public ToqanConversationController(ToqanConversationService conversationService) {
        this.conversationService = conversationService;
    }
    
    /**
     * Create a new conversation
     * POST /api/aiagent/conversation
     */
    @PostMapping("/conversation")
    public ResponseEntity<?> createConversation(@RequestBody CreateConversationRequest request) {
        try {
            logger.info("Received request to create conversation");
            CreateConversationResponse response = conversationService.createConversation(
                    request.getUserMessage(), request.getPrivateUserFiles());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
                    
        } catch (ToqanApiException e) {
            logger.error("Toqan API error: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getMessage(), "statusCode", e.getStatusCode()));
                    
        } catch (Exception e) {
            logger.error("Unexpected error creating conversation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }
    
    /**
     * Get answer for a conversation
     * GET /api/aiagent/answer?conversation_id={conversationId}&request_id={requestId}
     */
    @GetMapping("/answer")
    public ResponseEntity<?> getAnswer(
            @RequestParam("conversation_id") String conversationId,
            @RequestParam("request_id") String requestId) {
        
        try {
            logger.info("Received request to get answer for conversation: {}, request: {}", 
                    conversationId, requestId);
            GetAnswerResponse response = conversationService.getAnswer(conversationId, requestId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
                    
        } catch (ToqanApiException e) {
            logger.error("Toqan API error: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getMessage(), "statusCode", e.getStatusCode()));
                    
        } catch (Exception e) {
            logger.error("Unexpected error getting answer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }
    
    /**
     * Create conversation and wait for answer
     * POST /api/aiagent/conversation/sync
     */
    @PostMapping("/conversation/sync")
    public ResponseEntity<?> createConversationAndGetAnswer(
            @RequestBody CreateConversationRequest request) {
        
        try {
            logger.info("Received request to create conversation and get answer synchronously");
            GetAnswerResponse response = conversationService.createConversationAndGetAnswer(
                    request.getUserMessage(), request.getPrivateUserFiles());
            return ResponseEntity.ok(response);
            
        } catch (TimeoutException e) {
            logger.warn("Timeout waiting for answer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                    .body(Map.of("error", e.getMessage()));
                    
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
                    
        } catch (ToqanApiException e) {
            logger.error("Toqan API error: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getMessage(), "statusCode", e.getStatusCode()));
                    
        } catch (Exception e) {
            logger.error("Unexpected error in sync conversation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }
    
    /**
     * Create conversation and get answer asynchronously
     * POST /api/aiagent/conversation/async
     */
    @PostMapping("/conversation/async")
    public ResponseEntity<?> createConversationAsync(@RequestBody CreateConversationRequest request) {
        try {
            logger.info("Received request to create conversation asynchronously");
            
            // Create conversation first to get IDs
            CreateConversationResponse conversation = conversationService.createConversation(
                    request.getUserMessage(), request.getPrivateUserFiles());
            
            Map<String, Object> response = new HashMap<>();
            response.put("conversation_id", conversation.getConversationId());
            response.put("request_id", conversation.getRequestId());
            response.put("message", "Conversation created. Use GET /api/aiagent/answer to poll for results.");
            
            return ResponseEntity.accepted().body(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
                    
        } catch (ToqanApiException e) {
            logger.error("Toqan API error: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getMessage(), "statusCode", e.getStatusCode()));
                    
        } catch (Exception e) {
            logger.error("Unexpected error in async conversation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }
    
    /**
     * Health check endpoint
     * GET /api/aiagent/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Toqan AI Agent Integration",
                "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * Global exception handler for ToqanApiException
     */
    @ExceptionHandler(ToqanApiException.class)
    public ResponseEntity<?> handleToqanApiException(ToqanApiException e) {
        logger.error("Toqan API Exception: {}", e.getMessage(), e);
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", e.getMessage());
        error.put("statusCode", e.getStatusCode());
        error.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(e.getStatusCode()).body(error);
    }
    
    /**
     * Global exception handler for general exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e) {
        logger.error("Unexpected Exception: {}", e.getMessage(), e);
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", "An unexpected error occurred");
        error.put("message", e.getMessage());
        error.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

