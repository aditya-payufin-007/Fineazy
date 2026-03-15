# Quick Start Guide - WhatsApp Lending Bot

## Prerequisites Checklist

- [ ] Java 17 or higher installed
- [ ] MySQL 8.0 or higher installed and running
- [ ] Maven 3.6+ installed
- [ ] Meta Business Account created
- [ ] WhatsApp Business API access

## Step-by-Step Setup

### 1. Clone and Navigate to Project

```bash
cd /path/to/chatbot
```

### 2. Setup MySQL Database

```bash
# Login to MySQL
mysql -u root -p

# Create database
CREATE DATABASE whatsapp_bot;

# Verify
SHOW DATABASES;

# Exit
exit;
```

### 3. Configure Application Properties

Edit `src/main/resources/application.properties`:

```properties
# Update these values
spring.datasource.username=root
spring.datasource.password=your_mysql_password

whatsapp.verify.token=my_secure_verify_token_123
whatsapp.access.token=EAAxxxxxxxxxxxxxxxxxxxxxxxxx
whatsapp.phone.number.id=123456789012345
whatsapp.business.account.id=123456789012345
```

### 4. Build the Project

```bash
# Clean and build
mvn clean install

# If build fails, try:
mvn clean install -DskipTests
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

You should see:
```
Started ChatbotApplication in X.XXX seconds
```

### 6. Test Locally

Open a new terminal and test the health endpoint:

```bash
curl http://localhost:8080/webhook/health
```

Expected response: `Webhook service is running`

### 7. Expose Local Server (for testing)

Use ngrok to expose your local server:

```bash
# Install ngrok first: https://ngrok.com/download
ngrok http 8080
```

Copy the HTTPS URL (e.g., `https://abc123.ngrok.io`)

### 8. Configure Meta Webhook

1. Go to https://business.facebook.com
2. Select your Business Account
3. Navigate to **WhatsApp** > **Configuration** > **Webhook**
4. Click **Edit**
5. Enter:
   - **Callback URL**: `https://abc123.ngrok.io/webhook`
   - **Verify Token**: `my_secure_verify_token_123` (same as in application.properties)
6. Click **Verify and Save**
7. Subscribe to **messages** field

### 9. Test with WhatsApp

1. Send a message to your WhatsApp Business number
2. Check application logs for:
   ```
   Received webhook: ...
   Processing message from: ...
   ```
3. You should receive a welcome message

### 10. Monitor Application

Check logs:
```bash
# In the terminal where app is running
# You'll see all incoming messages and responses
```

## Common Issues and Fixes

### Issue: Database Connection Error

```
Error: Access denied for user 'root'@'localhost'
```

**Fix:**
- Verify MySQL is running: `mysql.server start` (Mac) or `sudo service mysql start` (Linux)
- Check username/password in application.properties
- Grant permissions: `GRANT ALL PRIVILEGES ON whatsapp_bot.* TO 'root'@'localhost';`

### Issue: Webhook Verification Failed

```
Meta shows: Verification failed
```

**Fix:**
- Ensure verify token in application.properties matches Meta dashboard
- Check if server is accessible (test with curl)
- Verify HTTPS is being used (Meta requires HTTPS)

### Issue: Port Already in Use

```
Error: Port 8080 is already in use
```

**Fix:**
- Change port in application.properties: `server.port=8081`
- Or kill process using port: `lsof -ti:8080 | xargs kill -9`

### Issue: Messages Not Being Received

**Fix:**
- Check webhook subscription is active in Meta dashboard
- Verify access token is valid
- Check application logs for errors
- Ensure phone number ID is correct

## Testing the Flow

Send these messages in order to test the complete flow:

```
1. "Hi" → Bot asks for name
2. "John Doe" → Bot asks for loan type
3. "1" → Bot creates application and asks for PAN
4. "ABCDE1234F" → Bot asks for Aadhaar
5. "123456789012" → Bot checks eligibility
6. "YES" → Bot accepts offer
7. "DONE" → Bot confirms documents
```

## Production Deployment

### Deploy to AWS EC2

1. **Launch EC2 Instance**
   - Ubuntu 22.04 LTS
   - t2.medium or higher
   - Security Group: Allow ports 22, 80, 443, 8080

2. **Install Dependencies**
   ```bash
   sudo apt update
   sudo apt install openjdk-17-jdk mysql-server maven -y
   ```

3. **Setup MySQL**
   ```bash
   sudo mysql
   CREATE DATABASE whatsapp_bot;
   CREATE USER 'chatbot'@'localhost' IDENTIFIED BY 'secure_password';
   GRANT ALL PRIVILEGES ON whatsapp_bot.* TO 'chatbot'@'localhost';
   FLUSH PRIVILEGES;
   exit;
   ```

4. **Transfer Project**
   ```bash
   scp -r /path/to/chatbot ubuntu@your-ec2-ip:/home/ubuntu/
   ```

5. **Configure Application**
   ```bash
   cd /home/ubuntu/chatbot
   nano src/main/resources/application.properties
   # Update database credentials
   ```

6. **Build and Run**
   ```bash
   mvn clean install
   nohup java -jar target/chatbot-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
   ```

7. **Setup Nginx as Reverse Proxy**
   ```bash
   sudo apt install nginx -y
   sudo nano /etc/nginx/sites-available/chatbot
   ```

   Add:
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;

       location / {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```

8. **Setup SSL with Certbot**
   ```bash
   sudo apt install certbot python3-certbot-nginx -y
   sudo certbot --nginx -d your-domain.com
   ```

9. **Update Meta Webhook URL**
   - Change to: `https://your-domain.com/webhook`

## Useful Commands

```bash
# Check if app is running
ps aux | grep java

# Stop application
pkill -f chatbot

# View logs
tail -f app.log

# Check database
mysql -u root -p
USE whatsapp_bot;
SHOW TABLES;
SELECT * FROM stage_tracker;
```

## Next Steps

- [ ] Add more conversational responses
- [ ] Integrate with actual loan processing system
- [ ] Add document upload handling
- [ ] Implement payment gateway
- [ ] Add analytics tracking
- [ ] Setup monitoring and alerts

## Support

For issues:
1. Check application logs
2. Verify Meta webhook subscription
3. Test database connectivity
4. Review Meta API documentation: https://developers.facebook.com/docs/whatsapp

---

**Team BroCode** - Hackathon Project

