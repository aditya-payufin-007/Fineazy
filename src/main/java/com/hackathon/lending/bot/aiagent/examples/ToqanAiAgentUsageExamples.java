package com.hackathon.lending.bot.aiagent.examples;

import com.hackathon.lending.bot.aiagent.dto.CreateConversationRequest;
import com.hackathon.lending.bot.aiagent.dto.CreateConversationResponse;
import com.hackathon.lending.bot.aiagent.dto.GetAnswerResponse;
import com.hackathon.lending.bot.aiagent.exception.ToqanApiException;
import com.hackathon.lending.bot.aiagent.service.ToqanConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * Example usage of Toqan AI Agent integration
 * 
 * This class demonstrates various ways to interact with the Toqan AI Agent
 * including synchronous, asynchronous, and polling patterns.
 * 
 * NOTE: This is for demonstration purposes only.
 * Remove or comment out @Component annotation to prevent automatic instantiation.
 */
@Component
public class ToqanAiAgentUsageExamples {
    
    private static final Logger logger = LoggerFactory.getLogger(ToqanAiAgentUsageExamples.class);
    
    @Autowired
    private ToqanConversationService conversationService;
    
    /**
     * Example 1: Simple synchronous conversation
     * Creates a conversation and waits for the answer
     */
    public void example1_SimpleSync() {
        try {
            logger.info("Example 1: Simple synchronous conversation");
            
            GetAnswerResponse answer = conversationService.createConversationAndGetAnswer(
                    "What are the requirements for a business loan?"
            );
            
            if (answer.isFinished()) {
                logger.info("Answer received: {}", answer.getAnswer());
            } else if (answer.isError()) {
                logger.error("Error in response: {}", answer.getAnswer());
            }
            
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for answer", e);
        } catch (ToqanApiException e) {
            logger.error("API error: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Example 2: Create conversation and poll manually
     * Useful when you want control over the polling process
     */
    public void example2_ManualPolling() {
        try {
            logger.info("Example 2: Manual polling");
            
            // Step 1: Create conversation
            CreateConversationResponse conversation = conversationService.createConversation(
                    "Calculate EMI for a loan of $50,000 at 8% interest for 5 years"
            );
            
            logger.info("Conversation created: {}, Request: {}", 
                    conversation.getConversationId(), 
                    conversation.getRequestId());
            
            // Step 2: Poll for answer
            GetAnswerResponse answer = conversationService.pollForAnswer(
                    conversation.getConversationId(),
                    conversation.getRequestId()
            );
            
            logger.info("Answer: {}", answer.getAnswer());
            
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for answer", e);
        } catch (ToqanApiException e) {
            logger.error("API error: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Example 3: Asynchronous conversation
     * Non-blocking approach using CompletableFuture
     */
    public void example3_Async() {
        logger.info("Example 3: Asynchronous conversation");
        
        CompletableFuture<GetAnswerResponse> future = 
                conversationService.createConversationAndGetAnswerAsync(
                        "What documents are needed for loan approval?"
                );
        
        future.thenAccept(answer -> {
            logger.info("Async answer received: {}", answer.getAnswer());
        }).exceptionally(ex -> {
            logger.error("Async error: {}", ex.getMessage(), ex);
            return null;
        });
        
        logger.info("Async request initiated, continuing with other work...");
    }
    
    /**
     * Example 4: Conversation with file attachments
     * Demonstrates how to send files with the conversation
     */
    public void example4_WithFiles() {
        try {
            logger.info("Example 4: Conversation with file attachments");
            
            // Create file list
            List<CreateConversationRequest.PrivateUserFile> files = new ArrayList<>();
            files.add(new CreateConversationRequest.PrivateUserFile(
                    "financial_statement.pdf",
                    "https://example.com/files/statement.pdf",
                    "application/pdf"
            ));
            files.add(new CreateConversationRequest.PrivateUserFile(
                    "tax_return.pdf",
                    "https://example.com/files/tax_return.pdf",
                    "application/pdf"
            ));
            
            // Create conversation with files
            GetAnswerResponse answer = conversationService.createConversationAndGetAnswer(
                    "Please analyze these financial documents and assess loan eligibility",
                    files
            );
            
            logger.info("Answer with file analysis: {}", answer.getAnswer());
            
            // Check for attachments in response
            if (answer.getAttachments() != null && !answer.getAttachments().isEmpty()) {
                logger.info("Response includes {} attachments", answer.getAttachments().size());
                for (GetAnswerResponse.Attachment attachment : answer.getAttachments()) {
                    logger.info("Attachment: {} ({})", attachment.getName(), attachment.getMimeType());
                }
            }
            
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for answer", e);
        } catch (ToqanApiException e) {
            logger.error("API error: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Example 5: Check only, no polling
     * Get current status without waiting
     */
    public void example5_CheckStatus() {
        try {
            logger.info("Example 5: Check status without polling");
            
            // First create a conversation
            CreateConversationResponse conversation = conversationService.createConversation(
                    "What is the interest rate for small business loans?"
            );
            
            // Check status immediately (without polling)
            GetAnswerResponse status = conversationService.getAnswer(
                    conversation.getConversationId(),
                    conversation.getRequestId()
            );
            
            if (status.isInProgress()) {
                logger.info("Answer is still being processed...");
            } else if (status.isFinished()) {
                logger.info("Answer is ready: {}", status.getAnswer());
            } else if (status.isError()) {
                logger.error("Error occurred: {}", status.getAnswer());
            }
            
        } catch (ToqanApiException e) {
            logger.error("API error: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Example 6: Multiple concurrent conversations
     * Demonstrates handling multiple conversations in parallel
     */
    public void example6_MultipleConcurrent() {
        logger.info("Example 6: Multiple concurrent conversations");
        
        String[] questions = {
                "What are the interest rates?",
                "What documents do I need?",
                "How long does approval take?",
                "What is the minimum loan amount?"
        };
        
        List<CompletableFuture<GetAnswerResponse>> futures = new ArrayList<>();
        
        for (String question : questions) {
            CompletableFuture<GetAnswerResponse> future = 
                    conversationService.createConversationAndGetAnswerAsync(question);
            futures.add(future);
        }
        
        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    logger.info("All conversations completed");
                    for (int i = 0; i < futures.size(); i++) {
                        final int index = i; // Make effectively final for lambda
                        futures.get(i).thenAccept(answer -> 
                                logger.info("Q{}: {}", index + 1, answer.getAnswer())
                        );
                    }
                })
                .exceptionally(ex -> {
                    logger.error("Error in concurrent conversations: {}", ex.getMessage(), ex);
                    return null;
                });
    }
    
    /**
     * Example 7: Error handling
     * Demonstrates proper error handling for different scenarios
     */
    public void example7_ErrorHandling() {
        logger.info("Example 7: Comprehensive error handling");
        
        try {
            GetAnswerResponse answer = conversationService.createConversationAndGetAnswer(
                    "Help me understand the loan process"
            );
            logger.info("Success: {}", answer.getAnswer());
            
        } catch (TimeoutException e) {
            logger.error("Operation timed out. The AI agent is taking too long to respond.");
            // Handle timeout - maybe retry or notify user
            
        } catch (ToqanApiException e) {
            // Handle specific API errors based on status code
            switch (e.getStatusCode()) {
                case 400:
                    logger.error("Bad request - check your input: {}", e.getMessage());
                    break;
                case 401:
                    logger.error("Unauthorized - check your API key: {}", e.getMessage());
                    break;
                case 403:
                    logger.error("Forbidden - you don't have permission: {}", e.getMessage());
                    break;
                case 404:
                    logger.error("Not found - resource doesn't exist: {}", e.getMessage());
                    break;
                case 429:
                    logger.error("Rate limit exceeded - slow down: {}", e.getMessage());
                    // Implement backoff strategy
                    break;
                case 500:
                    logger.error("Server error - try again later: {}", e.getMessage());
                    break;
                default:
                    logger.error("API error ({}): {}", e.getStatusCode(), e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Example 8: Integration with WhatsApp chatbot
     * Shows how to integrate with existing WhatsApp messaging
     */
    public String example8_WhatsAppIntegration(String userPhoneNumber, String userMessage) {
        try {
            logger.info("Processing WhatsApp message from {} via AI Agent", userPhoneNumber);
            
            // Create conversation with user message
            GetAnswerResponse answer = conversationService.createConversationAndGetAnswer(
                    userMessage
            );
            
            if (answer.isFinished()) {
                // Return answer to send back via WhatsApp
                return answer.getAnswer();
            } else if (answer.isError()) {
                return "I apologize, but I encountered an error processing your request. Please try again.";
            } else {
                return "I'm still processing your request. Please wait a moment...";
            }
            
        } catch (TimeoutException e) {
            logger.error("Timeout for user {}", userPhoneNumber, e);
            return "I'm taking longer than expected to respond. Please try again in a moment.";
            
        } catch (ToqanApiException e) {
            logger.error("API error for user {}: {}", userPhoneNumber, e.getMessage(), e);
            return "I'm experiencing technical difficulties. Please try again later.";
        }
    }
}

