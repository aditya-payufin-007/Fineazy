# Toqan AI Agent Module

Quick reference guide for the Toqan AI Agent integration module.

## 📁 Directory Structure

```
src/main/java/com/hackathon/lending/bot/aiagent/
├── client/
│   └── ToqanApiClient.java           # HTTP client for API calls
├── controller/
│   └── ToqanConversationController.java  # REST endpoints
├── dto/
│   ├── ApiErrorResponse.java         # Error response DTO
│   ├── CreateConversationRequest.java    # Request DTO
│   ├── CreateConversationResponse.java   # Response DTO
│   └── GetAnswerResponse.java        # Answer response DTO
├── exception/
│   ├── BadRequestException.java      # 400 error
│   ├── ForbiddenException.java       # 403 error
│   ├── InternalServerErrorException.java  # 500 error
│   ├── NotFoundException.java        # 404 error
│   ├── RateLimitExceededException.java    # 429 error
│   ├── ToqanApiException.java        # Base exception
│   └── UnauthorizedException.java    # 401 error
└── service/
    └── ToqanConversationService.java  # Business logic
```

## 🚀 Quick Start

### 1. Configure API Key

Edit `application.properties`:

```properties
toqan.api.key=YOUR_ACTUAL_API_KEY_HERE
```

### 2. Start the Application

```bash
cd /Users/suryanshu.gupta/Desktop/hackathon/chatbot
./mvnw spring-boot:run
```

### 3. Test the Integration

```bash
# Health check
curl -k https://localhost:8902/chatbot/api/aiagent/health

# Create conversation
curl -k -X POST https://localhost:8902/chatbot/api/aiagent/conversation/sync \
  -H "Content-Type: application/json" \
  -d '{"user_message": "Hello, I need help!"}'
```

## 📡 Available Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/aiagent/conversation` | Create a conversation (async) |
| GET | `/api/aiagent/answer` | Get answer for a conversation |
| POST | `/api/aiagent/conversation/sync` | Create and wait for answer |
| POST | `/api/aiagent/conversation/async` | Create conversation, return IDs |
| GET | `/api/aiagent/health` | Health check |

## 🔧 Configuration Properties

```properties
# Required
toqan.api.base-url=https://api.coco.prod.toqan.ai
toqan.api.key=YOUR_API_KEY_HERE

# Optional (with defaults)
toqan.api.timeout=30                    # Request timeout in seconds
toqan.polling.max-attempts=20           # Max polling attempts
toqan.polling.interval-ms=2000          # Polling interval in milliseconds
```

## 📝 Usage Examples

### Java Service Usage

```java
@Autowired
private ToqanConversationService service;

// Simple conversation
GetAnswerResponse answer = service.createConversationAndGetAnswer(
    "What are the loan requirements?"
);

// Async conversation
CompletableFuture<GetAnswerResponse> future = 
    service.createConversationAndGetAnswerAsync("Help me with loan application");
```

### REST API Usage

```bash
# Synchronous (waits for answer)
curl -k -X POST https://localhost:8902/chatbot/api/aiagent/conversation/sync \
  -H "Content-Type: application/json" \
  -d '{"user_message": "What is the interest rate?"}'

# Asynchronous (poll manually)
curl -k -X POST https://localhost:8902/chatbot/api/aiagent/conversation \
  -H "Content-Type: application/json" \
  -d '{"user_message": "Calculate my EMI"}'

# Then poll for answer
curl -k "https://localhost:8902/chatbot/api/aiagent/answer?conversation_id=CONV_ID&request_id=REQ_ID"
```

## ⚠️ Error Handling

All errors return appropriate HTTP status codes with JSON error messages:

```json
{
  "error": "Error message",
  "statusCode": 400,
  "timestamp": 1700563800000
}
```

| Status | Exception | Description |
|--------|-----------|-------------|
| 400 | BadRequestException | Invalid input |
| 401 | UnauthorizedException | Invalid API key |
| 403 | ForbiddenException | Access denied |
| 404 | NotFoundException | Resource not found |
| 408 | Request Timeout | Polling timeout |
| 429 | RateLimitExceededException | Rate limit exceeded |
| 500 | InternalServerErrorException | Server error |

## 🔍 Debugging

Enable DEBUG logging:

```properties
logging.level.com.hackathon.lending.bot.aiagent=DEBUG
```

Check logs for:
- Request/response details
- Polling attempts
- Error messages
- API call timings

## 📚 Additional Documentation

For detailed documentation, see:
- [TOQAN_AI_AGENT_INTEGRATION.md](../TOQAN_AI_AGENT_INTEGRATION.md) - Complete documentation
- API documentation files:
  - `/Users/suryanshu.gupta/Desktop/scripts/api1.md` - Create Conversation API
  - `/Users/suryanshu.gupta/Desktop/scripts/api2.md` - Get Answer API

## 🛠️ Testing

Run unit tests:
```bash
./mvnw test -Dtest=ToqanConversationServiceTest
```

Run integration tests:
```bash
./mvnw test -Dtest=ToqanApiClientTest
```

## 🔐 Security Notes

- **Never commit API keys to version control**
- Use environment variables in production
- Enable HTTPS for all API calls
- Implement rate limiting on your endpoints
- Validate all user inputs

## 🐛 Common Issues

1. **"Unauthorized" errors**: Check API key configuration
2. **"Rate limit exceeded"**: Reduce request frequency
3. **Timeouts**: Increase polling max attempts or interval
4. **SSL errors**: Use `-k` flag with curl for self-signed certificates

## 📞 Support

For issues or questions:
1. Check the logs (DEBUG level)
2. Review the comprehensive documentation
3. Verify API key and configuration
4. Contact Toqan support for API-specific issues

---

**Version**: 1.0.0  
**Last Updated**: November 21, 2025

