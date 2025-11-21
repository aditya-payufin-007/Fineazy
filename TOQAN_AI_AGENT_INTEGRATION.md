# Toqan AI Agent Integration

This module provides integration with the Toqan AI Agent APIs for creating and managing AI-powered conversations.

## Architecture

The integration follows a layered architecture:

```
├── aiagent/
│   ├── client/          # HTTP client for API calls
│   ├── controller/      # REST endpoints
│   ├── dto/            # Data Transfer Objects
│   ├── exception/      # Custom exceptions
│   └── service/        # Business logic
```

## Components

### 1. DTOs (Data Transfer Objects)

Located in `com.hackathon.lending.bot.aiagent.dto`:

- **CreateConversationRequest**: Request to create a new conversation
  - `user_message` (String, required): The message text to initialize the conversation
  - `private_user_files` (Array, optional): Array of file objects to attach

- **CreateConversationResponse**: Response after creating a conversation
  - `conversation_id` (String): Unique conversation identifier
  - `request_id` (String): Unique request identifier

- **GetAnswerResponse**: Response containing the AI agent's answer
  - `status` (String): "in_progress", "error", or "finished"
  - `answer` (String): The AI agent's response
  - `timestamp` (DateTime): When the answer was generated
  - `attachments` (Array): List of attachment objects

- **ApiErrorResponse**: Standard error response format
  - `message` (String): Error message

### 2. Exceptions

Located in `com.hackathon.lending.bot.aiagent.exception`:

- **ToqanApiException**: Base exception for all Toqan API errors
- **BadRequestException** (400): Invalid input or request body
- **UnauthorizedException** (401): Missing or invalid API key
- **ForbiddenException** (403): User not allowed to access resource
- **NotFoundException** (404): Resource not found
- **RateLimitExceededException** (429): API rate limit exceeded
- **InternalServerErrorException** (500): Server error

### 3. Client

**ToqanApiClient** (`com.hackathon.lending.bot.aiagent.client`)

Handles HTTP communication with Toqan APIs:
- Creates HTTP requests with proper headers and authentication
- Handles response parsing and error mapping
- Implements timeout and retry logic

Key methods:
- `createConversation(CreateConversationRequest)`: Creates a new conversation
- `getAnswer(conversationId, requestId)`: Retrieves answer for a conversation

### 4. Service

**ToqanConversationService** (`com.hackathon.lending.bot.aiagent.service`)

Provides high-level business logic:
- Conversation creation and management
- Polling for answers with configurable intervals
- Asynchronous conversation handling
- Validation and error handling

Key methods:
- `createConversation(userMessage)`: Create a conversation with text
- `createConversation(userMessage, files)`: Create a conversation with files
- `getAnswer(conversationId, requestId)`: Get answer for a conversation
- `pollForAnswer(conversationId, requestId)`: Poll until answer is ready
- `createConversationAndGetAnswer(userMessage)`: Create and wait for answer
- `createConversationAndGetAnswerAsync(userMessage)`: Async version

### 5. Controller

**ToqanConversationController** (`com.hackathon.lending.bot.aiagent.controller`)

REST API endpoints for external access:

#### Endpoints

##### 1. Create Conversation
```
POST /api/aiagent/conversation
Content-Type: application/json

Request Body:
{
  "user_message": "Hello, I need assistance!",
  "private_user_files": []
}

Response (200 OK):
{
  "conversation_id": "1234567890abcdef",
  "request_id": "abcdef1234567890"
}
```

##### 2. Get Answer
```
GET /api/aiagent/answer?conversation_id={id}&request_id={reqId}

Response (200 OK):
{
  "status": "finished",
  "answer": "I can help you with that...",
  "timestamp": "2025-11-21T10:30:00",
  "attachments": []
}
```

##### 3. Create Conversation and Wait (Synchronous)
```
POST /api/aiagent/conversation/sync
Content-Type: application/json

Request Body:
{
  "user_message": "What is the loan approval process?"
}

Response (200 OK):
{
  "status": "finished",
  "answer": "The loan approval process involves...",
  "timestamp": "2025-11-21T10:30:00",
  "attachments": []
}
```

##### 4. Create Conversation (Asynchronous)
```
POST /api/aiagent/conversation/async
Content-Type: application/json

Request Body:
{
  "user_message": "Calculate my loan eligibility"
}

Response (202 Accepted):
{
  "conversation_id": "1234567890abcdef",
  "request_id": "abcdef1234567890",
  "message": "Conversation created. Use GET /api/aiagent/answer to poll for results."
}
```

##### 5. Health Check
```
GET /api/aiagent/health

Response (200 OK):
{
  "status": "UP",
  "service": "Toqan AI Agent Integration",
  "timestamp": 1700563800000
}
```

## Configuration

Add the following properties to `application.properties`:

```properties
# Toqan AI Agent Configuration
toqan.api.base-url=https://api.coco.prod.toqan.ai
toqan.api.key=YOUR_API_KEY_HERE
toqan.api.timeout=30

# Polling Configuration
toqan.polling.max-attempts=20
toqan.polling.interval-ms=2000
```

### Configuration Properties

- **toqan.api.base-url**: Base URL for Toqan API (default: https://api.coco.prod.toqan.ai)
- **toqan.api.key**: Your Toqan API key (required)
- **toqan.api.timeout**: Request timeout in seconds (default: 30)
- **toqan.polling.max-attempts**: Maximum polling attempts (default: 20)
- **toqan.polling.interval-ms**: Interval between polls in milliseconds (default: 2000)

## Error Handling

All endpoints return appropriate HTTP status codes:

- **200 OK**: Successful request
- **202 Accepted**: Request accepted, processing asynchronously
- **400 Bad Request**: Invalid input or request body
- **401 Unauthorized**: Missing or invalid API key
- **403 Forbidden**: Not allowed to access resource
- **404 Not Found**: Resource not found
- **408 Request Timeout**: Polling timeout exceeded
- **429 Too Many Requests**: Rate limit exceeded
- **500 Internal Server Error**: Server error

Error Response Format:
```json
{
  "error": "Error message description",
  "statusCode": 400,
  "timestamp": 1700563800000
}
```

## Usage Examples

### Example 1: Create Conversation and Poll Manually

```java
@Autowired
private ToqanConversationService service;

// Create conversation
CreateConversationResponse conversation = service.createConversation(
    "What are the interest rates for business loans?"
);

// Poll for answer
GetAnswerResponse answer = service.pollForAnswer(
    conversation.getConversationId(), 
    conversation.getRequestId()
);

System.out.println("Answer: " + answer.getAnswer());
```

### Example 2: Create Conversation and Wait (Synchronous)

```java
@Autowired
private ToqanConversationService service;

try {
    GetAnswerResponse answer = service.createConversationAndGetAnswer(
        "Calculate EMI for a loan of $50,000"
    );
    
    if (answer.isFinished()) {
        System.out.println("Answer: " + answer.getAnswer());
    }
} catch (TimeoutException e) {
    System.err.println("Timeout waiting for answer");
} catch (ToqanApiException e) {
    System.err.println("API Error: " + e.getMessage());
}
```

### Example 3: Asynchronous Processing

```java
@Autowired
private ToqanConversationService service;

CompletableFuture<GetAnswerResponse> future = 
    service.createConversationAndGetAnswerAsync(
        "What documents do I need for a loan?"
    );

future.thenAccept(answer -> {
    System.out.println("Answer: " + answer.getAnswer());
}).exceptionally(ex -> {
    System.err.println("Error: " + ex.getMessage());
    return null;
});
```

### Example 4: With File Attachments

```java
@Autowired
private ToqanConversationService service;

List<CreateConversationRequest.PrivateUserFile> files = new ArrayList<>();
files.add(new CreateConversationRequest.PrivateUserFile(
    "financial_statement.pdf",
    "https://example.com/files/statement.pdf",
    "application/pdf"
));

GetAnswerResponse answer = service.createConversationAndGetAnswer(
    "Analyze my financial statement",
    files
);
```

## Testing

### Using cURL

#### Create Conversation:
```bash
curl -X POST https://localhost:8902/chatbot/api/aiagent/conversation \
  -H "Content-Type: application/json" \
  -d '{
    "user_message": "Hello, I need help with loan eligibility"
  }'
```

#### Get Answer:
```bash
curl -X GET "https://localhost:8902/chatbot/api/aiagent/answer?conversation_id=YOUR_CONV_ID&request_id=YOUR_REQ_ID"
```

#### Synchronous Request:
```bash
curl -X POST https://localhost:8902/chatbot/api/aiagent/conversation/sync \
  -H "Content-Type: application/json" \
  -d '{
    "user_message": "What are the loan terms?"
  }'
```

### Using Postman

Import the endpoints into Postman:
1. Set base URL: `https://localhost:8902/chatbot`
2. Add endpoints as documented above
3. Test each endpoint with sample data

## Important Notes

1. **API Key**: Ensure you have a valid Toqan API key configured in `application.properties`

2. **Rate Limits**: The Toqan API has rate limits. Implement appropriate retry logic and respect the rate limits.

3. **URL Expiration**: URLs in requests/responses may expire after 30 days.

4. **Polling**: For long-running requests, use the async endpoint and poll for results to avoid timeouts.

5. **Error Handling**: Always implement proper error handling for all API calls.

6. **Logging**: The integration includes comprehensive logging. Set logging level to DEBUG for troubleshooting:
   ```properties
   logging.level.com.hackathon.lending.bot.aiagent=DEBUG
   ```

## Security Considerations

1. **API Key Storage**: Store API keys securely, use environment variables in production
2. **HTTPS**: Always use HTTPS for API calls
3. **Input Validation**: Validate all user inputs before sending to API
4. **Rate Limiting**: Implement rate limiting on your endpoints to prevent abuse
5. **Error Messages**: Don't expose sensitive information in error messages

## Monitoring and Logging

The integration provides comprehensive logging:

- **INFO**: Successful operations, conversation creation, answer retrieval
- **DEBUG**: Detailed request/response information, polling attempts
- **WARN**: Rate limits, timeouts, validation errors
- **ERROR**: API errors, unexpected exceptions

Monitor these logs for:
- Failed API calls
- Rate limit issues
- Timeout patterns
- Error trends

## Troubleshooting

### Common Issues

1. **401 Unauthorized**
   - Check if API key is correctly configured
   - Verify API key is valid and not expired

2. **429 Rate Limit Exceeded**
   - Reduce request frequency
   - Implement exponential backoff

3. **408 Request Timeout**
   - Increase `toqan.polling.max-attempts`
   - Increase `toqan.polling.interval-ms`

4. **500 Internal Server Error**
   - Check Toqan API status
   - Review error logs for details
   - Contact Toqan support if persistent

## Future Enhancements

Potential improvements:
- Conversation history persistence
- Response caching
- Batch processing support
- Webhook support for async callbacks
- Metrics and analytics integration
- Circuit breaker pattern implementation

---

For more information, refer to the official Toqan API documentation.

