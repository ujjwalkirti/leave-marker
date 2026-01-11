# Leave Management System - Backend Setup Guide

## Prerequisites

Before you begin, ensure you have the following installed:

1. **Java Development Kit (JDK) 17 or higher**
   - Download from: https://adoptium.net/ or https://www.oracle.com/java/technologies/downloads/
   - Verify installation: `java -version`

2. **Maven 3.6 or higher**
   - Download from: https://maven.apache.org/download.cgi
   - Verify installation: `mvn -version`

3. **PostgreSQL 12 or higher**
   - Download from: https://www.postgresql.org/download/
   - Or use Docker: `docker run --name postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres`

4. **Git** (optional, for version control)

## Database Setup

### Step 1: Create Database

Open PostgreSQL command line or pgAdmin and run:

```sql
CREATE DATABASE leavemarker;
```

### Step 2: Create Database User (Optional)

If you want a dedicated user:

```sql
CREATE USER leavemarker_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE leavemarker TO leavemarker_user;
```

### Step 3: Verify Connection

Test the connection using psql:
```bash
psql -h localhost -U postgres -d leavemarker
```

## Application Configuration

### Step 1: Configure Database Connection

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/leavemarker
    username: postgres
    password: postgres  # Change this!
```

Or use environment variables (recommended for production):

```bash
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
```

### Step 2: Configure JWT Secret

**IMPORTANT**: Change the JWT secret for production!

Option 1: Edit application.yml
```yaml
jwt:
  secret: your-super-secure-256-bit-secret-key-here-minimum-32-characters
```

Option 2: Use environment variable
```bash
export JWT_SECRET=your-super-secure-256-bit-secret-key-here-minimum-32-characters
```

### Step 3: Configure Email (Optional)

For email notifications, configure SMTP settings:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
```

Or use environment variables:
```bash
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
```

**Note for Gmail**: You need to use an App Password, not your regular password.
Create one at: https://myaccount.google.com/apppasswords

## Building the Application

### Step 1: Clean and Install Dependencies

```bash
cd backend
mvn clean install
```

This will:
- Download all dependencies
- Compile the code
- Run tests
- Package the application

### Step 2: Run the Application

**Option A: Using Maven**
```bash
mvn spring-boot:run
```

**Option B: Using JAR file**
```bash
java -jar target/leave-management-1.0.0.jar
```

**Option C: With Environment Variables**
```bash
DB_USERNAME=postgres \
DB_PASSWORD=your_password \
JWT_SECRET=your_secret_key \
java -jar target/leave-management-1.0.0.jar
```

The application will start on: `http://localhost:8080/api`

## Verify Installation

### 1. Health Check

```bash
curl http://localhost:8080/api/health
```

Expected response:
```json
{
  "status": "UP",
  "service": "Leave Management System"
}
```

### 2. Test Signup

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Test Company",
    "companyEmail": "company@test.com",
    "fullName": "John Doe",
    "email": "john@test.com",
    "password": "password123",
    "employeeId": "EMP001",
    "workLocation": "MAHARASHTRA"
  }'
```

You should receive a JWT token and user details.

### 3. Test Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@test.com",
    "password": "password123"
  }'
```

## Database Schema Initialization

The application uses Hibernate's `ddl-auto: update` setting, which automatically:
- Creates tables on first run
- Updates schema when entities change
- Does NOT drop existing data

Tables created:
- companies
- employees
- leave_policies
- holidays
- leave_balances
- leave_applications
- attendance
- audit_logs

## Troubleshooting

### Issue: "Could not connect to database"

**Solution:**
- Verify PostgreSQL is running: `pg_isready`
- Check connection details in application.yml
- Ensure database exists: `psql -l | grep leavemarker`
- Check firewall settings

### Issue: "Port 8080 already in use"

**Solution:**
Change the port in application.yml:
```yaml
server:
  port: 8081
```

Or use environment variable:
```bash
SERVER_PORT=8081 java -jar target/leave-management-1.0.0.jar
```

### Issue: "Invalid JWT token"

**Solution:**
- Ensure JWT_SECRET is the same across restarts
- Token might be expired (valid for 24 hours)
- Login again to get a new token

### Issue: Lombok errors during build

**Solution:**
- Ensure Lombok plugin is installed in your IDE
- For IntelliJ: Settings → Plugins → Search "Lombok" → Install
- For Eclipse: Download lombok.jar and run it
- Enable annotation processing in IDE settings

### Issue: Email not sending

**Solution:**
- Check SMTP credentials
- For Gmail, use App Password not regular password
- Verify firewall allows SMTP connection
- Check logs for detailed error messages

## Production Deployment

### 1. Build Production JAR

```bash
mvn clean package -DskipTests
```

### 2. Set Production Environment Variables

Create a `.env` file or export variables:

```bash
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password
export JWT_SECRET=very-long-secure-random-string-here
export MAIL_HOST=smtp.yourcompany.com
export MAIL_USERNAME=noreply@yourcompany.com
export MAIL_PASSWORD=mail_password
export CORS_ORIGINS=https://yourfrontend.com
```

### 3. Run with Production Profile

```bash
java -jar -Dspring.profiles.active=prod target/leave-management-1.0.0.jar
```

### 4. Use Process Manager (Recommended)

**Using systemd (Linux):**

Create `/etc/systemd/system/leave-management.service`:

```ini
[Unit]
Description=Leave Management System
After=network.target

[Service]
Type=simple
User=appuser
WorkingDirectory=/opt/leave-management
ExecStart=/usr/bin/java -jar /opt/leave-management/leave-management-1.0.0.jar
Restart=always
Environment="DB_USERNAME=postgres"
Environment="DB_PASSWORD=your_password"
Environment="JWT_SECRET=your_secret"

[Install]
WantedBy=multi-user.target
```

Then:
```bash
sudo systemctl daemon-reload
sudo systemctl enable leave-management
sudo systemctl start leave-management
sudo systemctl status leave-management
```

### 5. Security Checklist

- [ ] Change default JWT secret
- [ ] Use strong database password
- [ ] Enable HTTPS/SSL
- [ ] Set up firewall rules
- [ ] Configure CORS properly
- [ ] Use environment variables for secrets
- [ ] Set up database backups
- [ ] Enable application logging
- [ ] Monitor application health
- [ ] Set up rate limiting (if needed)

## API Documentation

Once running, you can test all APIs using tools like:
- **Postman**: Import the API collection
- **cURL**: Use command line examples from README.md
- **Swagger** (not included): Can be added with springdoc-openapi

## Support

For issues or questions:
1. Check logs: `tail -f logs/spring.log`
2. Verify configuration in application.yml
3. Review this setup guide
4. Check the main README.md for API documentation

## Next Steps

1. Set up the frontend (Next.js)
2. Initialize leave policies for your company
3. Add employees
4. Configure holiday calendar
5. Start using the system!
