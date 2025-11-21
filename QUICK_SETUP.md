# Quick Setup Guide - Toqan AI Agent Integration

This guide will help you set up and test the Toqan AI Agent integration in 5 minutes.

## Prerequisites

- Java 11 or higher
- Maven
- Toqan API Key (from your Toqan Space)
- Running MySQL database (for the main application)

## Step 1: Configure API Key

1. Open the file:
   ```
   /Users/suryanshu.gupta/Desktop/hackathon/chatbot/src/main/resources/application.properties
   ```

2. Find the Toqan configuration section and replace `YOUR_TOQAN_API_KEY_HERE` with your actual API key:
   ```properties
   toqan.api.key=your_actual_api_key_here
   ```

3. Save the file.

## Step 2: Build the Project

```bash
cd /Users/suryanshu.gupta/Desktop/hackathon/chatbot
./mvnw clean install
```

## Step 3: Start the Application

```bash
./mvnw spring-boot:run
```

Wait for the application to start. You should see:
```
Started ChatbotApplication in X.XXX seconds
```

## Step 4: Test the Integration

### Option A: Using cURL (Quick Test)

Open a new terminal and run:

```bash
# Test health endpoint
curl -k https://localhost:8902/chatbot/api/aiagent/health

# Test synchronous conversation
curl -k -X POST https://localhost:8902/chatbot/api/aiagent/conversation/sync \
  -H "Content-Type: application/json" \
  -d '{
    "user_message": "Hello, can you help me understand business loan requirements?"
  }'
```

### Option B: Using Postman (Recommended)

1. Import the Postman collection:
   ```
   /Users/suryanshu.gupta/Desktop/hackathon/chatbot/Toqan_AI_Agent_API.postman_collection.json
   ```

2. In Postman, disable SSL certificate verification:
   - File → Settings → General → SSL certificate verification → OFF

3. Run the "Health Check" request to verify the service is running.

4. Run the "Create Conversation and Get Answer (Sync)" request to test the full flow.

## Step 5: Verify Everything Works

You should see responses like:

### Health Check Response:
```json
{
  "status": "UP",
  "service": "Toqan AI Agent Integration",
  "timestamp": 1700563800000
}
```

### Conversation Response:
```json
{
  "status": "finished",
  "answer": "I'd be happy to help you understand business loan requirements...",
  "timestamp": "2025-11-21T10:30:00",
  "attachments": []
}
```

## Available Endpoints

Once running, you can access:

| Endpoint | Description | Method |
|----------|-------------|--------|
| `/api/aiagent/health` | Health check | GET |
| `/api/aiagent/conversation` | Create conversation | POST |
| `/api/aiagent/answer` | Get answer | GET |
| `/api/aiagent/conversation/sync` | Create and wait for answer | POST |
| `/api/aiagent/conversation/async` | Create async | POST |

Base URL: `https://localhost:8902/chatbot`

## Troubleshooting

### Problem: "Connection refused"
**Solution**: Ensure the application is running on port 8902

### Problem: "401 Unauthorized"
**Solution**: Check your API key in `application.properties`

### Problem: "SSL certificate error"
**Solution**: Use `-k` flag with curl or disable SSL verification in Postman

### Problem: "Timeout waiting for answer"
**Solution**: Increase polling settings in `application.properties`:
```properties
toqan.polling.max-attempts=30
toqan.polling.interval-ms=3000
```

### Problem: "429 Rate Limit Exceeded"
**Solution**: Wait a moment and reduce request frequency

## Next Steps

1. **Read the full documentation**:
   - `/Users/suryanshu.gupta/Desktop/hackathon/chatbot/TOQAN_AI_AGENT_INTEGRATION.md`

2. **Review examples**:
   - `/Users/suryanshu.gupta/Desktop/hackathon/chatbot/src/main/java/com/hackathon/lending/bot/aiagent/examples/ToqanAiAgentUsageExamples.java`

3. **Integrate with your application**:
   - Inject `ToqanConversationService` into your services
   - Use the provided examples as templates

4. **Enable detailed logging**:
   ```properties
   logging.level.com.hackathon.lending.bot.aiagent=DEBUG
   ```

## Quick Code Example

```java
@Autowired
private ToqanConversationService conversationService;

public String askAiAgent(String question) {
    try {
        GetAnswerResponse answer = conversationService
            .createConversationAndGetAnswer(question);
        return answer.getAnswer();
    } catch (Exception e) {
        return "Error: " + e.getMessage();
    }
}
```

## Configuration Reference

Default configuration (adjust as needed):

```properties
# API Settings
toqan.api.base-url=https://api.coco.prod.toqan.ai
toqan.api.key=YOUR_API_KEY_HERE
toqan.api.timeout=30

# Polling Settings
toqan.polling.max-attempts=20        # Max 40 seconds (20 * 2000ms)
toqan.polling.interval-ms=2000       # 2 seconds between checks

# Logging
logging.level.com.hackathon.lending.bot.aiagent=DEBUG
```

## Support

- Check logs in the console for detailed error messages
- Review the comprehensive documentation
- Check the API documentation files:
  - `/Users/suryanshu.gupta/Desktop/scripts/api1.md`
  - `/Users/suryanshu.gupta/Desktop/scripts/api2.md`

## Security Note

⚠️ **Important**: Never commit your API key to version control!

In production, use environment variables:
```properties
toqan.api.key=${TOQAN_API_KEY}
```

Then set:
```bash
export TOQAN_API_KEY=your_actual_key
```

---

**Setup complete!** You're now ready to use the Toqan AI Agent integration.

