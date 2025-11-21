package com.hackathon.lending.bot.utility;

import com.hackathon.lending.bot.dto.MessageContext;
import com.hackathon.lending.bot.dto.WhatsAppWebhookRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageParser {

    private static final Logger logger = LoggerFactory.getLogger(MessageParser.class);

    /**
     * Parse WhatsApp webhook request and extract message context
     */
    public static MessageContext parseWebhookMessage(WhatsAppWebhookRequest request) {
        if (request == null) {
            logger.warn("Webhook request is null");
            return null;
        }

        WhatsAppWebhookRequest.Value value = request.getFirstValue();
        if (value == null) {
            logger.warn("Webhook payload missing entry/change/value nodes");
            return null;
        }

        List<WhatsAppWebhookRequest.WhatsAppMessage> messages = value.getMessages();
        if (messages == null || messages.isEmpty()) {
            logger.warn("Webhook payload does not include messages");
            return null;
        }

        WhatsAppWebhookRequest.WhatsAppMessage message = messages.get(0);

        String userName = "";
        String from = message.getFrom();
        String messageId = message.getId();
        String messageType = message.getType();
        String timestamp = message.getTimestamp();
        String messageBody = extractMessageBody(message);

        List<WhatsAppWebhookRequest.Contact> contacts = value.getContacts();
        if (contacts != null && !contacts.isEmpty()) {
            WhatsAppWebhookRequest.Contact contact = contacts.get(0);
            if (contact.getProfile() != null) {
                userName = contact.getProfile().getName();
            }
        }

        MessageContext context = new MessageContext(
                from,
                messageId,
                messageBody,
                messageType,
                userName,
                timestamp
        );

        logger.info(
                "Parsed message context from webhook: from={} type={} messageId={} timestamp={}",
                context.getFrom(),
                context.getMessageType(),
                context.getMessageId(),
                context.getTimestamp()
        );

        return context;
    }

    /**
     * Extract message body based on message type
     */
    private static String extractMessageBody(WhatsAppWebhookRequest.WhatsAppMessage message) {
        if ("text".equals(message.getType()) && message.getText() != null) {
            return message.getText().getBody();
        }
        // You can expand here for other types, e.g. interactive if you start sending them!
        logger.debug("Unsupported message type encountered: {}", message.getType());
        return "";
    }

    private MessageParser() {
        // Utility class
    }
}
