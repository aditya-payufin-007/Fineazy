# Architecture Documentation - WhatsApp Lending Bot

## System Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                         End User (WhatsApp)                           │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             │ Sends message via WhatsApp
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│                      Meta WhatsApp Cloud API                          │
│                   (graph.facebook.com/v18.0)                         │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             │ POST /webhook (webhook callback)
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│                      AWS EC2 Instance / Your Server                   │
│                     Spring Boot Application (Port 8080)               │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │              CONTROLLER LAYER                                │   │
│  │  ┌────────────────────────────────────────────────────┐     │   │
│  │  │  WebhookController                                  │     │   │
│  │  │  - GET  /webhook (verify)                          │     │   │
│  │  │  - POST /webhook (receive messages)                 │     │   │
│  │  │  - GET  /webhook/health                            │     │   │
│  │  └──────────────────┬─────────────────────────────────┘     │   │
│  └───────────────────────┼───────────────────────────────────────┘   │
│                          │                                            │
│  ┌───────────────────────▼───────────────────────────────────────┐   │
│  │              SERVICE LAYER                                     │   │
│  │  ┌─────────────────────────────────────────────────────┐     │   │
│  │  │  WhatsAppMessageService                              │     │   │
│  │  │  - processIncomingMessage()                         │     │   │
│  │  │  - saveMessage()                                    │     │   │
│  │  │  - saveChatHistory()                                │     │   │
│  │  │  - getOrCreateStageTracker()                        │     │   │
│  │  └──────────────────┬──────────────────────────────────┘     │   │
│  │                     │                                         │   │
│  │  ┌──────────────────▼──────────────────────────────────┐     │   │
│  │  │  LendingWorkflowService                              │     │   │
│  │  │  - processUserInput()                               │     │   │
│  │  │  - handleOnboarding()                               │     │   │
│  │  │  - handleCreateApplication()                        │     │   │
│  │  │  - handleKYC()                                      │     │   │
│  │  │  - handleEligibility()                              │     │   │
│  │  │  - handleOffer()                                    │     │   │
│  │  │  - handleDocumentsVerification()                    │     │   │
│  │  │  - handleDisbursal()                                │     │   │
│  │  │  - handlePostDisbursal()                            │     │   │
│  │  └──────────────────┬──────────────────────────────────┘     │   │
│  └───────────────────────┼───────────────────────────────────────┘   │
│                          │                                            │
│  ┌───────────────────────▼───────────────────────────────────────┐   │
│  │              REPOSITORY LAYER (Spring Data JPA)                │   │
│  │  ┌──────────────┬──────────────┬──────────────┬───────────┐  │   │
│  │  │ ChatHistory  │   Message    │ UserDetails  │  Stage    │  │   │
│  │  │ Repository   │  Repository  │ Repository   │ Tracker   │  │   │
│  │  │              │              │              │Repository │  │   │
│  │  └──────────────┴──────────────┴──────────────┴───────────┘  │   │
│  └───────────────────────┼───────────────────────────────────────┘   │
│                          │                                            │
│  ┌───────────────────────▼───────────────────────────────────────┐   │
│  │              UTILITY LAYER                                     │   │
│  │  ┌─────────────────────────────────────────────────────┐     │   │
│  │  │  WhatsAppApiClient                                   │     │   │
│  │  │  - sendTextMessage()                                │     │   │
│  │  │  - markMessageAsRead()                              │     │   │
│  │  └──────────────────────────────────────────────────────┘     │   │
│  │  ┌─────────────────────────────────────────────────────┐     │   │
│  │  │  MessageParser                                       │     │   │
│  │  │  - parseWebhookMessage()                            │     │   │
│  │  └──────────────────────────────────────────────────────┘     │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             │ JDBC Connection
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│                      MySQL Database Server                            │
│                                                                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌──────────┐  │
│  │ chat_history│  │  messages   │  │user_details │  │  stage_  │  │
│  │             │  │             │  │             │  │  tracker │  │
│  └─────────────┘  └─────────────┘  └─────────────┘  └──────────┘  │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Component Interaction Flow

### Incoming Message Flow

```
1. User sends message via WhatsApp
   ↓
2. Meta Cloud API receives message
   ↓
3. Meta calls POST /webhook with message payload
   ↓
4. WebhookController receives webhook
   ↓
5. MessageParser extracts message context
   ↓
6. WhatsAppMessageService processes message
   ├─ Saves message to database
   ├─ Saves chat history
   └─ Gets user's current stage
   ↓
7. LendingWorkflowService handles stage logic
   ├─ Processes user input based on stage
   ├─ Updates stage if needed
   └─ Generates response message
   ↓
8. WhatsAppApiClient sends response
   ↓
9. Meta Cloud API delivers message
   ↓
10. User receives response in WhatsApp
```

---

## Layer Responsibilities

### 1. Controller Layer
**Purpose**: Handle HTTP requests/responses

**Components**:
- `WebhookController`
  - Webhook verification (GET)
  - Message receiving (POST)
  - Health check (GET)

**Responsibilities**:
- Validate webhook requests
- Parse JSON payloads
- Return appropriate HTTP responses
- Handle errors gracefully

---

### 2. Service Layer
**Purpose**: Business logic and orchestration

**Components**:
- `WhatsAppMessageService`
  - Message processing orchestration
  - Database operations coordination
  - Stage management

- `LendingWorkflowService`
  - Loan application workflow
  - Stage-based conversation handling
  - Business rule enforcement

**Responsibilities**:
- Process incoming messages
- Manage conversation state
- Execute business logic
- Coordinate between layers

---

### 3. Repository Layer
**Purpose**: Data access and persistence

**Components**:
- `ChatHistoryRepository`
- `MessageRepository`
- `UserDetailsRepository`
- `StageTrackerRepository`

**Responsibilities**:
- CRUD operations
- Custom queries
- Transaction management
- Data validation

---

### 4. Utility Layer
**Purpose**: Helper functions and external integrations

**Components**:
- `WhatsAppApiClient`
  - HTTP client for Meta API
  - Message sending
  - Read receipts

- `MessageParser`
  - Webhook payload parsing
  - Data extraction

- `ApplicationStages`
  - Stage constants

**Responsibilities**:
- External API calls
- Data transformation
- Utility functions

---

## Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    INCOMING MESSAGE                              │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
                    ┌──────────────┐
                    │  Parse JSON  │
                    └──────┬───────┘
                           │
                           ▼
                  ┌─────────────────┐
                  │ Extract Context │
                  │ - from          │
                  │ - message       │
                  │ - timestamp     │
                  │ - type          │
                  └────────┬────────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
          ▼                ▼                ▼
    ┌─────────┐    ┌─────────────┐   ┌─────────┐
    │  Save   │    │   Get User  │   │  Save   │
    │ Message │    │    Stage    │   │  Chat   │
    │   DB    │    │   Tracker   │   │ History │
    └─────────┘    └──────┬──────┘   └─────────┘
                          │
                          ▼
                  ┌────────────────┐
                  │ Process Based  │
                  │   on Stage     │
                  └────────┬───────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
          ▼                ▼                ▼
    ┌──────────┐   ┌────────────┐   ┌──────────┐
    │  Update  │   │  Generate  │   │  Update  │
    │ User Data│   │  Response  │   │  Stage   │
    └──────────┘   └─────┬──────┘   └──────────┘
                         │
                         ▼
                  ┌─────────────┐
                  │ Send Reply  │
                  │  via API    │
                  └─────────────┘
```

---

## State Machine (User Journey)

```
┌──────────────┐
│ ONBOARDING   │ ─────> User provides name
└──────┬───────┘
       │
       ▼
┌──────────────────┐
│CREATE_APPLICATION│ ─────> User selects loan type
└──────┬───────────┘
       │
       ▼
┌──────────────┐
│     KYC      │ ─────> User provides PAN and Aadhaar
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ ELIGIBILITY  │ ─────> System checks eligibility
└──────┬───────┘
       │
       ▼
┌──────────────┐
│    OFFER     │ ─────> User accepts/rejects offer
└──────┬───────┘
       │
       ▼
┌────────────────────┐
│DOCUMENTS_VERIFICATION│ ─────> User uploads documents
└──────┬─────────────┘
       │
       ▼
┌──────────────┐
│  DISBURSAL   │ ─────> Loan is disbursed
└──────┬───────┘
       │
       ▼
┌──────────────┐
│POST_DISBURSAL│ ─────> Status checks, payments
└──────────────┘
```

---

## Database Entity Relationships

```
┌─────────────────────┐
│   chat_history      │
│  PK: mobile_number  │────┐
│  - name             │    │
│  - message_id       │    │
│  - time_stamp       │    │
│  - message_type     │    │
└─────────────────────┘    │
                           │
                           │
┌─────────────────────┐    │    ┌─────────────────────┐
│     messages        │    │    │   user_details      │
│  PK: message_id     │    │    │  PK: mobile_id      │
│  - message          │    └───▶│  - application_id   │
│  - created_at       │         │  - name             │
│  - updated_at       │         │  - pan              │
└─────────────────────┘         │  - aadhaar          │
                                └──────────┬──────────┘
                                           │
                                           │
                                ┌──────────▼──────────┐
                                │   stage_tracker     │
                                │  PK: id             │
                                │  - application_id   │
                                │  - mobile_id        │
                                │  - current_stage    │
                                │  - last_updated     │
                                └─────────────────────┘
```

---

## Security Considerations

1. **Webhook Verification**
   - Verify token validation
   - HTTPS required
   - Request signature validation (optional)

2. **Database Security**
   - Encrypted connection
   - Strong passwords
   - Limited user privileges

3. **API Security**
   - Access token protection
   - Environment variables for secrets
   - Rate limiting

4. **Data Privacy**
   - PAN/Aadhaar encryption (to be implemented)
   - Secure data transmission
   - Compliance with data protection laws

---

## Scalability Considerations

1. **Horizontal Scaling**
   - Stateless application design
   - Load balancer ready
   - Database connection pooling

2. **Asynchronous Processing**
   - Messages processed in separate threads
   - Quick webhook acknowledgment
   - Background job processing

3. **Caching** (Future)
   - Redis for session data
   - FAQ response caching
   - User state caching

4. **Database Optimization**
   - Indexed columns
   - Query optimization
   - Connection pooling

---

## Monitoring & Logging

### Application Logs
- Request/Response logging
- Error tracking
- Performance metrics

### Database Monitoring
- Query performance
- Connection pool status
- Slow query identification

### API Monitoring
- Meta API call success rate
- Response times
- Rate limit tracking

---

## Deployment Architecture (AWS)

```
┌─────────────────────────────────────────────────────────────┐
│                         AWS Cloud                            │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Route 53 (DNS)                           │  │
│  └────────────────────┬─────────────────────────────────┘  │
│                       │                                     │
│  ┌────────────────────▼─────────────────────────────────┐  │
│  │    Application Load Balancer (ALB)                    │  │
│  │    - SSL/TLS Termination                             │  │
│  └────────────────────┬─────────────────────────────────┘  │
│                       │                                     │
│  ┌────────────────────▼─────────────────────────────────┐  │
│  │         EC2 Auto Scaling Group                        │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │  │
│  │  │   EC2       │  │   EC2       │  │   EC2       │  │  │
│  │  │ Instance 1  │  │ Instance 2  │  │ Instance 3  │  │  │
│  │  │ Spring Boot │  │ Spring Boot │  │ Spring Boot │  │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  │  │
│  └────────────────────┬─────────────────────────────────┘  │
│                       │                                     │
│  ┌────────────────────▼─────────────────────────────────┐  │
│  │              RDS MySQL                                │  │
│  │        - Multi-AZ Deployment                          │  │
│  │        - Automated Backups                            │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## Technology Stack Summary

| Layer | Technology |
|-------|------------|
| Framework | Spring Boot 4.0.0 |
| Language | Java 17 |
| Web Server | Embedded Tomcat |
| ORM | Hibernate / Spring Data JPA |
| Database | MySQL 8.0 |
| Build Tool | Maven |
| API Client | Java HttpClient |
| JSON | Jackson |
| Logging | SLF4J + Logback |
| External API | Meta WhatsApp Business API v18.0 |

---

## Future Architecture Enhancements

1. **Message Queue** (RabbitMQ/Kafka)
   - Decouple message processing
   - Handle high throughput
   - Retry mechanism

2. **Caching Layer** (Redis)
   - Session management
   - Response caching
   - Rate limiting

3. **Microservices**
   - Separate KYC service
   - Document verification service
   - Payment service

4. **AI/NLP Integration**
   - AWS Comprehend
   - Dialogflow
   - Custom ML models

5. **Analytics**
   - ELK Stack (Elasticsearch, Logstash, Kibana)
   - Application Insights
   - Custom dashboards

---

**End of Architecture Documentation**

