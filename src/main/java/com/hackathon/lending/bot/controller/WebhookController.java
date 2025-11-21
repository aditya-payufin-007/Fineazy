package com.hackathon.lending.bot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.lending.bot.dto.MessageContext;
import com.hackathon.lending.bot.dto.WhatsAppWebhookRequest;
import com.hackathon.lending.bot.service.WhatsAppMessageService;
import com.hackathon.lending.bot.utility.MessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    @Value("${whatsapp.verify.token}")
    private String verifyToken;

    @Autowired
    private WhatsAppMessageService whatsAppMessageService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Webhook verification endpoint (GET)
     * Meta will call this endpoint to verify the webhook
     */
    @GetMapping
    public ResponseEntity<?> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        logger.info("Webhook verification request received");

        // Verify the token matches
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            logger.info("Webhook verified successfully");
            return ResponseEntity.ok(challenge);
        } else {
            logger.warn("Webhook verification failed");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verification failed");
        }
    }

    /**
     * Webhook endpoint to receive messages (POST)
     * Meta will send incoming messages to this endpoint
     */
    @PostMapping
    public ResponseEntity<String> receiveMessage(@RequestBody String requestString) {
        try {
            logger.info("Received webhook payload: {}", requestString);

            WhatsAppWebhookRequest request = objectMapper.readValue(requestString, WhatsAppWebhookRequest.class);

            logger.info("Webhook payload summary: {}", request);

            // Parse the message
            MessageContext context = MessageParser.parseWebhookMessage(request);

            if (context != null) {
                logger.info(
                        "Scheduling processing for messageId={} from={} body=\"{}\"",
                        context.getMessageId(),
                        context.getFrom(),
                        context.getMessageBody()
                );
                // Process the message asynchronously
                new Thread(() -> whatsAppMessageService.processIncomingMessage(context)).start();
            } else {
                logger.warn("Could not parse message from webhook");
            }
            return ResponseEntity.ok("EVENT_RECEIVED");

        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            // Still return 200 to avoid Meta retrying
            return ResponseEntity.ok("EVENT_RECEIVED");
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Webhook service is running");
    }
}
 
