# Leave Management System - Complete API Reference

Base URL: `http://localhost:8080/api`

## Authentication

All endpoints except `/auth/**` and `/health` require a valid JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

---

## 1. Authentication APIs

### 1.1 Signup (Create Company & Super Admin)

**Endpoint:** `POST /auth/signup`

**Access:** Public

**Request Body:**
```json
{
  "companyName": "Acme Corporation",
  "companyEmail": "admin@acmecorp.com",
  "fullName": "John Doe",
  "email": "john.doe@acmecorp.com",
  "password": "SecurePass123",
  "employeeId": "EMP001",
  "workLocation": "MAHARASHTRA"
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Company and user created successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "email": "john.doe@acmecorp.com",
    "fullName": "John Doe",
    "role": "SUPER_ADMIN",
    "companyId": 1
  }
}
```

### 1.2 Login

**Endpoint:** `POST /auth/login`

**Access:** Public

**Request Body:**
```json
{
  "email": "john.doe@acmecorp.com",
  "password": "SecurePass123"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "email": "john.doe@acmecorp.com",
    "fullName": "John Doe",
    "role": "SUPER_ADMIN",
    "companyId": 1
  }
}
```

### 1.3 Request Password Reset

**Endpoint:** `POST /auth/password-reset-request`

**Access:** Public

**Request Body:**
```json
{
  "email": "john.doe@acmecorp.com"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Password reset email sent",
  "data": null
}
```

### 1.4 Confirm Password Reset

**Endpoint:** `POST /auth/password-reset-confirm`

**Access:** Public

**Request Body:**
```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "NewSecurePass123"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Password reset successful",
  "data": null
}
```

---

## 2. Employee Management APIs

### 2.1 Create Employee

**Endpoint:** `POST /employees`

**Access:** SUPER_ADMIN, HR_ADMIN

**Request Body:**
```json
{
  "employeeId": "EMP002",
  "fullName": "Jane Smith",
  "email": "jane.smith@acmecorp.com",
  "password": "password123",
  "role": "EMPLOYEE",
  "department": "Engineering",
  "jobTitle": "Software Engineer",
  "dateOfJoining": "2024-01-15",
  "employmentType": "FULL_TIME",
  "workLocation": "KARNATAKA",
  "managerId": 1
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Employee created successfully",
  "data": {
    "id": 2,
    "employeeId": "EMP002",
    "fullName": "Jane Smith",
    "email": "jane.smith@acmecorp.com",
    "role": "EMPLOYEE",
    "department": "Engineering",
    "jobTitle": "Software Engineer",
    "dateOfJoining": "2024-01-15",
    "employmentType": "FULL_TIME",
    "workLocation": "KARNATAKA",
    "status": "ACTIVE",
    "managerId": 1,
    "managerName": "John Doe",
    "companyId": 1,
    "companyName": "Acme Corporation"
  }
}
```

### 2.2 Get All Employees

**Endpoint:** `GET /employees`

**Access:** Authenticated users

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Employees retrieved successfully",
  "data": [
    {
      "id": 1,
      "employeeId": "EMP001",
      "fullName": "John Doe",
      "email": "john.doe@acmecorp.com",
      "role": "SUPER_ADMIN",
      "status": "ACTIVE",
      ...
    },
    ...
  ]
}
```

### 2.3 Get Employee by ID

**Endpoint:** `GET /employees/{id}`

**Access:** Authenticated users

**Response:** `200 OK`

### 2.4 Get Active Employees

**Endpoint:** `GET /employees/active`

**Access:** Authenticated users

**Response:** `200 OK`

### 2.5 Update Employee

**Endpoint:** `PUT /employees/{id}`

**Access:** SUPER_ADMIN, HR_ADMIN

**Request Body:**
```json
{
  "fullName": "Jane Smith Updated",
  "department": "Product",
  "jobTitle": "Senior Software Engineer",
  "status": "ACTIVE"
}
```

**Response:** `200 OK`

### 2.6 Deactivate Employee

**Endpoint:** `DELETE /employees/{id}`

**Access:** SUPER_ADMIN, HR_ADMIN

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Employee deactivated successfully",
  "data": null
}
```

---

## 3. Leave Policy Management APIs

### 3.1 Create Leave Policy

**Endpoint:** `POST /leave-policies`

**Access:** SUPER_ADMIN, HR_ADMIN

**Request Body:**
```json
{
  "leaveType": "CASUAL_LEAVE",
  "annualQuota": 12,
  "monthlyAccrual": 1.0,
  "carryForward": true,
  "maxCarryForward": 5,
  "encashmentAllowed": false,
  "halfDayAllowed": true,
  "active": true
}
```

**Response:** `201 Created`

### 3.2 Get All Leave Policies

**Endpoint:** `GET /leave-policies`

**Access:** Authenticated users

**Response:** `200 OK`

### 3.3 Get Active Leave Policies

**Endpoint:** `GET /leave-policies/active`

**Access:** Authenticated users

**Response:** `200 OK`

### 3.4 Update Leave Policy

**Endpoint:** `PUT /leave-policies/{id}`

**Access:** SUPER_ADMIN, HR_ADMIN

**Response:** `200 OK`

### 3.5 Delete Leave Policy

**Endpoint:** `DELETE /leave-policies/{id}`

**Access:** SUPER_ADMIN, HR_ADMIN

**Response:** `200 OK`

---

## 4. Holiday Calendar APIs

### 4.1 Create Holiday

**Endpoint:** `POST /holidays`

**Access:** SUPER_ADMIN, HR_ADMIN

**Request Body:**
```json
{
  "name": "Republic Day",
  "date": "2024-01-26",
  "type": "NATIONAL",
  "state": null,
  "active": true
}
```

**Response:** `201 Created`

### 4.2 Get All Holidays

**Endpoint:** `GET /holidays`

**Access:** Authenticated users

**Response:** `200 OK`

### 4.3 Get Holidays by Date Range

**Endpoint:** `GET /holidays/date-range?startDate=2024-01-01&endDate=2024-12-31`

**Access:** Authenticated users

**Response:** `200 OK`

---

## 5. Leave Application APIs

### 5.1 Apply for Leave

**Endpoint:** `POST /leave-applications`

**Access:** Authenticated users

**Request Body:**
```json
{
  "leaveType": "CASUAL_LEAVE",
  "startDate": "2024-02-15",
  "endDate": "2024-02-17",
  "isHalfDay": false,
  "reason": "Personal work",
  "attachmentUrl": null
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Leave application submitted successfully",
  "data": {
    "id": 1,
    "employeeId": 2,
    "employeeName": "Jane Smith",
    "leaveType": "CASUAL_LEAVE",
    "startDate": "2024-02-15",
    "endDate": "2024-02-17",
    "numberOfDays": 3.0,
    "isHalfDay": false,
    "reason": "Personal work",
    "status": "PENDING",
    "requiresHrApproval": false
  }
}
```

### 5.2 Get My Leave Applications

**Endpoint:** `GET /leave-applications/my-leaves`

**Access:** Authenticated users

**Response:** `200 OK`

### 5.3 Get Pending Approvals (Manager)

**Endpoint:** `GET /leave-applications/pending-approvals/manager`

**Access:** MANAGER

**Response:** `200 OK`

### 5.4 Get Pending Approvals (HR)

**Endpoint:** `GET /leave-applications/pending-approvals/hr`

**Access:** SUPER_ADMIN, HR_ADMIN

**Response:** `200 OK`

### 5.5 Manager Approval/Rejection

**Endpoint:** `POST /leave-applications/{id}/approve/manager`

**Access:** MANAGER

**Request Body:**
```json
{
  "approved": true,
  "reason": null
}
```

**Response:** `200 OK`

### 5.6 HR Approval/Rejection

**Endpoint:** `POST /leave-applications/{id}/approve/hr`

**Access:** SUPER_ADMIN, HR_ADMIN

**Request Body:**
```json
{
  "approved": false,
  "reason": "Insufficient staff during this period"
}
```

**Response:** `200 OK`

### 5.7 Cancel Leave

**Endpoint:** `POST /leave-applications/{id}/cancel`

**Access:** Employee who applied

**Response:** `200 OK`

---

## 6. Attendance Management APIs

### 6.1 Punch In/Out

**Endpoint:** `POST /attendance/punch`

**Access:** Authenticated users

**Request Body (Punch In):**
```json
{
  "date": "2024-01-15",
  "punchTime": "09:00:00",
  "isPunchIn": true,
  "workType": "OFFICE"
}
```

**Request Body (Punch Out):**
```json
{
  "date": "2024-01-15",
  "punchTime": "18:00:00",
  "isPunchIn": false,
  "workType": null
}
```

**Response:** `201 Created`

### 6.2 Get Today's Attendance

**Endpoint:** `GET /attendance/today`

**Access:** Authenticated users

**Response:** `200 OK`

### 6.3 Get My Attendance

**Endpoint:** `GET /attendance/my-attendance`

**Access:** Authenticated users

**Response:** `200 OK`

### 6.4 Get My Attendance by Date Range

**Endpoint:** `GET /attendance/my-attendance/date-range?startDate=2024-01-01&endDate=2024-01-31`

**Access:** Authenticated users

**Response:** `200 OK`

### 6.5 Request Attendance Correction

**Endpoint:** `POST /attendance/{id}/request-correction`

**Access:** Authenticated users

**Request Body:**
```json
{
  "punchInTime": "09:15:00",
  "punchOutTime": "18:30:00",
  "workType": "OFFICE",
  "remarks": "Forgot to punch in on time"
}
```

**Response:** `200 OK`

### 6.6 Approve Correction

**Endpoint:** `POST /attendance/{id}/approve-correction`

**Access:** SUPER_ADMIN, HR_ADMIN, MANAGER

**Response:** `200 OK`

### 6.7 Reject Correction

**Endpoint:** `POST /attendance/{id}/reject-correction`

**Access:** SUPER_ADMIN, HR_ADMIN, MANAGER

**Request Body:**
```json
{
  "reason": "Inconsistent with system logs"
}
```

**Response:** `200 OK`

### 6.8 Get Pending Corrections

**Endpoint:** `GET /attendance/pending-corrections`

**Access:** SUPER_ADMIN, HR_ADMIN, MANAGER

**Response:** `200 OK`

### 6.9 Manual Attendance Marking

**Endpoint:** `POST /attendance/mark`

**Access:** SUPER_ADMIN, HR_ADMIN

**Request Body:**
```json
{
  "employeeId": 2,
  "date": "2024-01-15",
  "status": "LEAVE",
  "remarks": "On approved leave"
}
```

**Response:** `201 Created`

---

## 7. Reports APIs

All report endpoints return file downloads (CSV or Excel).

### 7.1 Leave Balance Report

**Endpoint:** `GET /reports/leave-balance?year=2024&format=excel`

**Access:** SUPER_ADMIN, HR_ADMIN, MANAGER

**Query Parameters:**
- `year` (required): Year for the report
- `format` (optional): `csv` or `excel` (default: excel)

**Response:** File download

### 7.2 Attendance Report

**Endpoint:** `GET /reports/attendance?startDate=2024-01-01&endDate=2024-01-31&format=csv`

**Access:** SUPER_ADMIN, HR_ADMIN, MANAGER

**Query Parameters:**
- `startDate` (required): Start date (YYYY-MM-DD)
- `endDate` (required): End date (YYYY-MM-DD)
- `format` (optional): `csv` or `excel` (default: excel)

**Response:** File download

### 7.3 Leave Usage Report

**Endpoint:** `GET /reports/leave-usage?startDate=2024-01-01&endDate=2024-12-31&format=excel`

**Access:** SUPER_ADMIN, HR_ADMIN, MANAGER

**Query Parameters:**
- `startDate` (required): Start date (YYYY-MM-DD)
- `endDate` (required): End date (YYYY-MM-DD)
- `format` (optional): `csv` or `excel` (default: excel)

**Response:** File download

---

## 8. Health Check

### 8.1 Health Status

**Endpoint:** `GET /health`

**Access:** Public

**Response:** `200 OK`
```json
{
  "status": "UP",
  "service": "Leave Management System"
}
```

---

## Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "message": "Employee with this email already exists",
  "data": null
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Unauthorized",
  "data": null
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "Access denied",
  "data": null
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Employee not found",
  "data": null
}
```

### 422 Validation Error
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Invalid email format",
    "password": "Password must be at least 8 characters"
  }
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "message": "An error occurred: ...",
  "data": null
}
```

---

## Enum Values

### Role
- `SUPER_ADMIN`
- `HR_ADMIN`
- `MANAGER`
- `EMPLOYEE`

### EmploymentType
- `FULL_TIME`
- `CONTRACT`

### EmployeeStatus
- `ACTIVE`
- `INACTIVE`

### LeaveType
- `CASUAL_LEAVE`
- `SICK_LEAVE`
- `EARNED_LEAVE`
- `LOSS_OF_PAY`
- `COMP_OFF`
- `OPTIONAL_HOLIDAY`

### LeaveStatus
- `PENDING`
- `APPROVED`
- `REJECTED`
- `CANCELLED`

### HolidayType
- `NATIONAL`
- `STATE`
- `COMPANY`
- `OPTIONAL`

### AttendanceStatus
- `PRESENT`
- `ABSENT`
- `LEAVE`
- `HOLIDAY`

### WorkType
- `OFFICE`
- `WFH`

### IndianState
All Indian states and union territories (e.g., `MAHARASHTRA`, `KARNATAKA`, `DELHI`, etc.)

---

## Rate Limiting

Currently not implemented. Consider adding rate limiting in production.

## API Versioning

Currently using v1 (implicit). Future versions can be added as `/api/v2/...`

## CORS

Configured to allow requests from `http://localhost:3000` by default.
Update `CORS_ORIGINS` environment variable for production.
