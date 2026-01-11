# Leave Management System - Backend

A comprehensive Leave and Attendance Management System for Indian companies built with Spring Boot and PostgreSQL.

## Tech Stack

- **Java**: 17
- **Framework**: Spring Boot 3.2.1
- **Database**: PostgreSQL
- **Security**: JWT-based authentication
- **Build Tool**: Maven

## Prerequisites

- JDK 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6+

## Database Setup

1. Create a PostgreSQL database:
```sql
CREATE DATABASE leavemarker;
```

2. Update database credentials in `src/main/resources/application.yml` or set environment variables:
```
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

## Running the Application

1. Clone the repository
2. Navigate to the backend directory
3. Run using Maven:
```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

## Environment Variables

- `DB_USERNAME`: Database username (default: postgres)
- `DB_PASSWORD`: Database password (default: postgres)
- `JWT_SECRET`: Secret key for JWT token generation (must be changed in production)
- `MAIL_HOST`: SMTP host for email notifications
- `MAIL_PORT`: SMTP port
- `MAIL_USERNAME`: Email username
- `MAIL_PASSWORD`: Email password
- `CORS_ORIGINS`: Allowed CORS origins (default: http://localhost:3000)

## API Documentation

### Authentication APIs

#### Signup
- **POST** `/api/auth/signup`
- Creates company and super admin user
- Request Body:
```json
{
  "companyName": "string",
  "companyEmail": "string",
  "fullName": "string",
  "email": "string",
  "password": "string",
  "employeeId": "string",
  "workLocation": "MAHARASHTRA"
}
```

#### Login
- **POST** `/api/auth/login`
- Request Body:
```json
{
  "email": "string",
  "password": "string"
}
```

#### Password Reset Request
- **POST** `/api/auth/password-reset-request`
- Request Body:
```json
{
  "email": "string"
}
```

#### Password Reset Confirm
- **POST** `/api/auth/password-reset-confirm`
- Request Body:
```json
{
  "token": "string",
  "newPassword": "string"
}
```

### Employee Management APIs

All employee endpoints require authentication. HR_ADMIN and SUPER_ADMIN roles can create/update employees.

- **GET** `/api/employees` - Get all employees
- **GET** `/api/employees/active` - Get active employees
- **GET** `/api/employees/{id}` - Get employee by ID
- **POST** `/api/employees` - Create employee (HR/SUPER_ADMIN)
- **PUT** `/api/employees/{id}` - Update employee (HR/SUPER_ADMIN)
- **DELETE** `/api/employees/{id}` - Deactivate employee (HR/SUPER_ADMIN)

### Health Check

- **GET** `/api/health` - Health check endpoint (public)

## Project Structure

```
src/main/java/com/leavemarker/
├── config/              # Configuration classes
├── controller/          # REST controllers
├── dto/                 # Data Transfer Objects
├── entity/              # JPA entities
├── enums/               # Enum types
├── exception/           # Custom exceptions
├── repository/          # JPA repositories
├── security/            # Security configuration
└── service/             # Business logic
```

## Features Implemented

- [x] Authentication & Authorization with JWT
- [x] Company Management
- [x] Employee Management
- [x] Role-based access control
- [x] Soft delete functionality
- [x] Audit logging support
- [ ] Leave Policy Management
- [ ] Holiday Calendar
- [ ] Leave Application & Approval
- [ ] Attendance Management
- [ ] Leave Balance Calculation
- [ ] Email Notifications
- [ ] Reports & Exports

## Security

- JWT-based authentication
- Role-based authorization
- Password encryption using BCrypt
- CORS configuration
- Session-less architecture

## Database Schema

The application uses JPA/Hibernate for ORM with the following main entities:

- Company
- Employee
- LeavePolicy
- Holiday
- LeaveBalance
- LeaveApplication
- Attendance
- AuditLog

## Development

### Build
```bash
mvn clean install
```

### Run Tests
```bash
mvn test
```

### Package
```bash
mvn clean package
```

The packaged JAR will be in `target/leave-management-1.0.0.jar`

## Production Deployment

1. Set proper environment variables
2. Change JWT_SECRET to a secure random string
3. Configure email settings
4. Set up PostgreSQL database
5. Run the JAR file:
```bash
java -jar target/leave-management-1.0.0.jar
```

## License

Proprietary - All rights reserved
