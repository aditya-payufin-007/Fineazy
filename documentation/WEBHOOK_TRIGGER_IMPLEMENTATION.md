# Webhook Trigger Implementation - Welcome Message

## Overview
This document describes the implementation of the webhook trigger that sends a welcome message when a user sends "Hi SMB Lending chatBot" through WhatsApp.

## Implementation Details

### Changes Made

#### File: `src/main/java/com/hackathon/lending/bot/service/WhatsAppMessageService.java`

**Changes:**
1. Added `OutgoingMessageService` dependency injection
2. Modified `processIncomingMessage()` method to check for trigger message
3. Sends personalized welcome message via `OutgoingMessageService`

### How It Works

1. **Webhook Receives Message**: When a WhatsApp message is received at the webhook endpoint (`/webhook`), it's processed by `WebhookController`

2. **Message Parsing**: The `MessageParser` extracts relevant information from the webhook payload including:
   - User's phone number (`from`)
   - User's name (`userName`)
   - Message content (`messageBody`)
   - Message ID
   - Timestamp

3. **Trigger Check**: In `WhatsAppMessageService.processIncomingMessage()`, the system checks if the incoming message matches "Hi SMB Lending chatBot" (case-insensitive)

4. **Welcome Message Sent**: If the trigger message is detected:
   - Extracts the user's name from the context (defaults to "there" if not available)
   - Formats the message: "Hi {name}, welcome to SMB Lending chatbot Team payU Finance"
   - Sends the message using `OutgoingMessageService.sendMessage()`

5. **Normal Processing Continues**: After sending the welcome message, the regular lending workflow continues to process the user's message based on their current stage

## Code Flow

```
WhatsApp User sends message
    ↓
WebhookController receives POST /webhook
    ↓
MessageParser.parseWebhookMessage()
    ↓
WhatsAppMessageService.processIncomingMessage()
    ↓
Check: message == "Hi SMB Lending chatBot"?
    ↓ YES
OutgoingMessageService.sendMessage()
    ↓
WhatsAppApiClient.sendTextMessage()
    ↓
WhatsApp Business API
    ↓
User receives: "Hi {name}, welcome to SMB Lending chatbot Team payU Finance"
```

## Key Components

### 1. WebhookController
- **Path**: `src/main/java/com/hackathon/lending/bot/controller/WebhookController.java`
- **Endpoint**: `POST /webhook`
- Receives incoming WhatsApp messages from Meta's WhatsApp Business API

### 2. WhatsAppMessageService
- **Path**: `src/main/java/com/hackathon/lending/bot/service/WhatsAppMessageService.java`
- **Key Method**: `processIncomingMessage(MessageContext context)`
- Contains the trigger logic and orchestrates the welcome message sending

### 3. OutgoingMessageService
- **Path**: `src/main/java/com/hackathon/lending/bot/service/OutgoingMessageService.java`
- **Key Method**: `sendMessage(String to, String message)`
- Handles outgoing message requests
- Provides validation and error handling

### 4. WhatsAppApiClient
- **Path**: `src/main/java/com/hackathon/lending/bot/utility/WhatsAppApiClient.java`
- **Key Method**: `sendTextMessage(String to, String message)`
- Low-level API client for WhatsApp Business API
- Handles HTTP communication with Meta's API

## Configuration Required

Ensure the following properties are set in `application.properties`:

```properties
whatsapp.access.token=YOUR_ACCESS_TOKEN
whatsapp.phone.number.id=YOUR_PHONE_NUMBER_ID
whatsapp.api.url=https://graph.facebook.com/v17.0
whatsapp.verify.token=YOUR_VERIFY_TOKEN
```

## Example Flow

### Scenario: User sends "Hi SMB Lending chatBot"

**Input Webhook Payload:**
```json
{
  "object": "whatsapp_business_account",
  "entry": [{
    "changes": [{
      "value": {
        "messages": [{
          "from": "919876543210",
          "id": "wamid.xxx",
          "timestamp": "1700000000",
          "text": {
            "body": "Hi SMB Lending chatBot"
          },
          "type": "text"
        }],
        "contacts": [{
          "profile": {
            "name": "John Doe"
          },
          "wa_id": "919876543210"
        }]
      }
    }]
  }]
}
```

**Processing:**
1. Message parsed: "Hi SMB Lending chatBot"
2. User name extracted: "John Doe"
3. Trigger condition matched ✓
4. Welcome message formatted: "Hi John Doe, welcome to SMB Lending chatbot Team payU Finance"

**Output:**
- Message sent via OutgoingMessageService
- User receives the personalized welcome message
- Regular workflow processing continues

## Testing

### Using Postman or cURL

**Test the webhook endpoint directly:**
```bash
curl -X POST http://localhost:8080/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "object": "whatsapp_business_account",
    "entry": [{
      "changes": [{
        "value": {
          "messages": [{
            "from": "919876543210",
            "id": "test_msg_id",
            "timestamp": "1700000000",
            "text": {
              "body": "Hi SMB Lending chatBot"
            },
            "type": "text"
          }],
          "contacts": [{
            "profile": {
              "name": "Test User"
            }
          }]
        }
      }]
    }]
  }'
```

**Expected Behavior:**
1. Webhook receives the message
2. Logs show: "Received trigger message. Sending welcome message via OutgoingMessageService to: 919876543210"
3. OutgoingMessageService sends: "Hi Test User, welcome to SMB Lending chatbot Team payU Finance"
4. Regular workflow response is also sent

## API Endpoints

### Webhook Endpoint
- **URL**: `/webhook`
- **Method**: POST
- **Purpose**: Receives incoming WhatsApp messages

### Outgoing Message API (for testing)
- **URL**: `/api/v1/messages/send`
- **Method**: POST
- **Body**:
```json
{
  "to": "919876543210",
  "message": "Hi John Doe, welcome to SMB Lending chatbot Team payU Finance"
}
```

## Logging

The implementation includes comprehensive logging:

```
INFO: Processing message from: 919876543210, Message: Hi SMB Lending chatBot
INFO: Received trigger message. Sending welcome message via OutgoingMessageService to: 919876543210
INFO: Attempting to send message to: 919876543210
INFO: Message sent successfully to: 919876543210
```

## Error Handling

The implementation includes error handling at multiple levels:

1. **Service Level**: Try-catch in `processIncomingMessage()`
2. **API Level**: Validation in `OutgoingMessageService`
3. **HTTP Level**: Response code checking in `WhatsAppApiClient`

If an error occurs, users receive: "Sorry, we encountered an error processing your request. Please try again."

## Benefits of Using OutgoingMessageService

1. **Separation of Concerns**: Clear distinction between incoming and outgoing message handling
2. **Validation**: Built-in validation for recipient and message content
3. **Error Handling**: Comprehensive error handling and logging
4. **Reusability**: Can be used by other parts of the application
5. **API Exposure**: Available as REST API for external integrations
6. **Consistency**: Ensures consistent message sending across the application

## Future Enhancements

Potential improvements:
1. Add multiple trigger phrases support
2. Template-based welcome messages
3. Language-specific welcome messages
4. Time-based personalization (Good morning/evening)
5. Rich media support (images, buttons)
6. A/B testing for different welcome messages

## Notes

- The trigger check is case-insensitive
- If userName is not available, defaults to "there"
- Both the welcome message and regular workflow response are sent
- Messages are marked as read after processing
- All messages are saved to the database for audit trail

