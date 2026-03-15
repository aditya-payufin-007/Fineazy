> Refer documentation directory for detailed documentations

# WhatsApp Lending Bot



A Spring Boot-based WhatsApp chatbot for loan application processing, integrated with Meta's WhatsApp Business API.

## Architecture

```
EndUser -> WhatsApp -> Meta Cloud API -> Webhook (EC2) -> Spring Boot Backend -> MySQL Database
```

## Features

- **Onboarding**: User registration and welcome flow
- **Loan Application**: Create and manage loan applications
- **KYC Processing**: Collect and verify PAN and Aadhaar details
- **Eligibility Check**: Automated eligibility assessment
- **Loan Offers**: Present loan offers to eligible users
- **Document Verification**: Collect and verify user documents
- **Disbursal**: Loan approval and disbursal tracking
- **Post-Disbursal**: Repayment tracking and status updates

## Tech Stack

- **Framework**: Spring Boot 4.0.0-SNAPSHOT
- **Language**: Java 17
- **Database**: MySQL
- **ORM**: Spring Data JPA / Hibernate
- **API Integration**: Meta WhatsApp Business API
- **Build Tool**: Maven

## Project Structure

```
src/main/java/com/hackathon/lending/
├── bot/
│   └── ChatbotApplication.java          # Main application entry point
├── config/
│   ├── JacksonConfig.java               # JSON serialization configuration
│   └── WebConfig.java                   # CORS and web configuration
├── controller/
│   └── WebhookController.java           # Meta webhook endpoints
├── dto/
│   ├── MessageContext.java              # Message context object
│   ├── WhatsAppMessageRequest.java      # Outgoing message structure
│   └── WhatsAppWebhookRequest.java      # Incoming webhook structure
├── entity/
│   ├── ChatHistory.java                 # Chat history entity
│   ├── Message.java                     # Message entity
│   ├── StageTracker.java                # User journey stage tracker
│   └── UserDetails.java                 # User details entity
├── repository/
│   ├── ChatHistoryRepository.java       # Chat history data access
│   ├── MessageRepository.java           # Message data access
│   ├── StageTrackerRepository.java      # Stage tracker data access
│   └── UserDetailsRepository.java       # User details data access
├── service/
│   ├── LendingWorkflowService.java      # Loan workflow business logic
│   └── WhatsAppMessageService.java      # Message processing service
└── utility/
    ├── ApplicationStages.java           # Stage constants
    ├── MessageParser.java               # Message parsing utility
    └── WhatsAppApiClient.java           # WhatsApp API client
```

## Database Schema

### 1. chat_history
- `mobile_number` (PK) - User's mobile number
- `name` - User's name
- `message_id` - WhatsApp message ID
- `time_stamp` - Message timestamp
- `message_type` - Type of message (text, image, etc.)

### 2. messages
- `message_id` (PK) - Unique message identifier
- `message` - Message content
- `created_at` - Creation timestamp
- `updated_at` - Update timestamp

### 3. user_details
- `mobile_id` (PK) - User's mobile number
- `application_id` - Loan application ID
- `name` - User's full name
- `pan` - PAN number
- `aadhaar` - Aadhaar number

### 4. stage_tracker
- `id` (PK) - Auto-generated ID
- `application_id` - Loan application ID
- `mobile_id` - User's mobile number
- `current_stage` - Current stage in the workflow
- `last_updated` - Last update timestamp

## Setup Instructions

### Prerequisites

1. Java 17 or higher
2. MySQL 8.0 or higher
3. Maven 3.6+
4. Meta WhatsApp Business Account
5. WhatsApp Business API Access Token

### Configuration

1. **Database Setup**

Create a MySQL database:
```sql
CREATE DATABASE whatsapp_bot;
```

2. **Application Properties**

Update `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/whatsapp_bot?createDatabaseIfNotExist=true
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

# Meta WhatsApp Configuration
whatsapp.verify.token=YOUR_VERIFY_TOKEN_HERE
whatsapp.access.token=YOUR_WHATSAPP_ACCESS_TOKEN
whatsapp.phone.number.id=YOUR_PHONE_NUMBER_ID
whatsapp.business.account.id=YOUR_BUSINESS_ACCOUNT_ID
```

3. **Meta WhatsApp Setup**

- Create a Meta Business Account
- Set up WhatsApp Business API
- Configure webhook URL: `https://your-domain.com/webhook`
- Set verify token in Meta dashboard (same as in application.properties)
- Subscribe to webhook events: `messages`

### Running the Application

1. **Build the project**
```bash
mvn clean install
```

2. **Run the application**
```bash
mvn spring-boot:run
```

Or run the JAR:
```bash
java -jar target/chatbot-0.0.1-SNAPSHOT.jar
```

The application will start on port 8080 (configurable in application.properties).

### Webhook Verification

Meta will verify your webhook by sending a GET request to:
```
GET /webhook?hub.mode=subscribe&hub.verify_token=YOUR_TOKEN&hub.challenge=CHALLENGE_STRING
```

Your application should respond with the challenge string.

## API Endpoints

### 1. Webhook Verification (GET)
```
GET /webhook
Query Params:
- hub.mode
- hub.verify_token
- hub.challenge
```

### 2. Receive Messages (POST)
```
POST /webhook
Content-Type: application/json
Body: WhatsApp webhook payload
```

### 3. Health Check
```
GET /webhook/health
```

## Application Flow

### User Journey Stages

1. **ONBOARDING**: Initial user greeting and name collection
2. **CREATE_APPLICATION**: Loan type selection and application creation
3. **KYC**: PAN and Aadhaar collection
4. **ELIGIBILITY**: Automated eligibility check
5. **OFFER**: Loan offer presentation
6. **DOCUMENTS_VERIFICATION**: Document collection
7. **DISBURSAL**: Loan disbursal processing
8. **POST_DISBURSAL**: Repayment and status tracking

### Sample Conversation Flow

```
Bot: Welcome to our lending service! 🏦
     We help you get loans quickly and easily.
     To get started, please reply with your full name.

User: John Doe

Bot: Thank you, John Doe! ✅
     Now let's create your loan application.
     Would you like to:
     1. Apply for a Personal Loan
     2. Apply for a Business Loan
     Please reply with 1 or 2.

User: 1

Bot: Great! Your application ID is: APP12345678
     Now let's complete your KYC process.
     Please provide your PAN number:

... (continues through all stages)
```

## Development

### Adding New Features

1. **New Stages**: Add to `ApplicationStages.java`
2. **New Handlers**: Implement in `LendingWorkflowService.java`
3. **New Entities**: Create in `entity/` package with corresponding repository

### Testing

Run tests:
```bash
mvn test
```

## Deployment

### Deploy to AWS EC2

1. Set up an EC2 instance (Ubuntu recommended)
2. Install Java 17 and MySQL
3. Configure security groups (allow port 8080)
4. Use nginx as reverse proxy
5. Set up SSL certificate (required by Meta)
6. Configure application.properties with production values

### Using Docker (Optional)

Create a Dockerfile:
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/chatbot-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:
```bash
docker build -t whatsapp-bot .
docker run -p 8080:8080 whatsapp-bot
```

## Future Enhancements

- Multi-channel support (SMS, email, push notifications)
- FAQ identification and caching
- NLP/AI integration for better intent recognition
- Advanced document verification with OCR
- Payment gateway integration
- Analytics dashboard
- Multi-language support

## Troubleshooting

### Common Issues

1. **Webhook not receiving messages**
   - Verify webhook URL is publicly accessible
   - Check SSL certificate validity
   - Ensure verify token matches
   - Check Meta webhook subscription

2. **Database connection errors**
   - Verify MySQL is running
   - Check credentials in application.properties
   - Ensure database exists

3. **Message sending fails**
   - Verify access token is valid
   - Check phone number ID
   - Review Meta API rate limits

## Support

For issues and questions:
- Check Meta's WhatsApp Business API documentation
- Review application logs: `logs/application.log`
- Contact: Team BroCode

## License

Internal use only - Hackathon Project

