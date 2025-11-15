# User Service - Complete API Documentation

## Base URL
```
http://localhost:8082
```

## Authentication
Most endpoints require authentication via JWT token in the Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

---

## 📋 Table of Contents
1. [Authentication Endpoints](#authentication-endpoints)
2. [User Profile Endpoints](#user-profile-endpoints)
3. [Address Management Endpoints](#address-management-endpoints)
4. [Admin Endpoints](#admin-endpoints)
5. [Email Verification Endpoints](#email-verification-endpoints)
6. [Password Reset Endpoints](#password-reset-endpoints)

---

## Authentication Endpoints

### 1. Register User
Create a new user account.

**Endpoint:** `POST /api/auth/register`  
**Authentication:** Not required

**Request Body:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+250788123456"
}
```

**Success Response (201):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900000,
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+250788123456",
    "role": "USER",
    "status": "ACTIVE",
    "emailVerified": false,
    "createdAt": "2025-11-12T10:30:00",
    "addresses": []
  }
}
```

**Error Response (409):**
```json
{
  "status": 409,
  "message": "Email already registered",
  "timestamp": "2025-11-12T10:30:00"
}
```

---

### 2. Login
Authenticate and get access token.

**Endpoint:** `POST /api/auth/login`  
**Authentication:** Not required

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

**Success Response (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900000,
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "role": "USER",
    "status": "ACTIVE"
  }
}
```

**Error Response (401):**
```json
{
  "status": 401,
  "message": "Invalid credentials",
  "timestamp": "2025-11-12T10:30:00"
}
```

---

### 3. Refresh Token
Get a new access token using refresh token.

**Endpoint:** `POST /api/auth/refresh`  
**Authentication:** Not required

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Success Response (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900000,
  "user": { ... }
}
```

---

### 4. Logout
Invalidate refresh token.

**Endpoint:** `POST /api/auth/logout`  
**Authentication:** Required

**Success Response (200):**
```json
{
  "message": "Logged out successfully"
}
```

---

### 5. Validate Token
Check if current token is valid.

**Endpoint:** `GET /api/auth/validate`  
**Authentication:** Required

**Success Response (200):**
```json
{
  "valid": true
}
```

---

## User Profile Endpoints

### 1. Get Current User Profile
Get authenticated user's profile.

**Endpoint:** `GET /api/users/me`  
**Authentication:** Required

**Success Response (200):**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+250788123456",
  "avatarUrl": "https://example.com/avatar.jpg",
  "role": "USER",
  "status": "ACTIVE",
  "emailVerified": true,
  "createdAt": "2025-11-01T10:00:00",
  "lastLoginAt": "2025-11-12T10:30:00",
  "addresses": [...]
}
```

---

### 2. Update Profile
Update user profile information.

**Endpoint:** `PUT /api/users/me`  
**Authentication:** Required

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "phone": "+250788999999",
  "avatarUrl": "https://example.com/new-avatar.jpg"
}
```

**Success Response (200):**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Smith",
  "phone": "+250788999999",
  "avatarUrl": "https://example.com/new-avatar.jpg",
  ...
}
```

---

### 3. Change Password
Change user password.

**Endpoint:** `PUT /api/users/me/password`  
**Authentication:** Required

**Request Body:**
```json
{
  "currentPassword": "OldPass123!",
  "newPassword": "NewSecurePass456!"
}
```

**Success Response (200):**
```json
{
  "message": "Password changed successfully"
}
```

**Error Response (401):**
```json
{
  "status": 401,
  "message": "Current password is incorrect",
  "timestamp": "2025-11-12T10:30:00"
}
```

---

### 4. Delete Account
Delete user account permanently.

**Endpoint:** `DELETE /api/users/me`  
**Authentication:** Required

**Success Response (200):**
```json
{
  "message": "Account deleted successfully"
}
```

---

### 5. Get User by ID
Get any user's public profile (for other services).

**Endpoint:** `GET /api/users/{userId}`  
**Authentication:** Required

**Success Response (200):**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  ...
}
```

---

## Address Management Endpoints

### 1. Get All Addresses
Get all addresses for authenticated user.

**Endpoint:** `GET /api/users/me/addresses`  
**Authentication:** Required

**Success Response (200):**
```json
[
  {
    "id": 1,
    "label": "Home",
    "streetAddress": "KG 123 St",
    "city": "Kigali",
    "state": "Kigali City",
    "postalCode": "00000",
    "country": "Rwanda",
    "isDefault": true
  },
  {
    "id": 2,
    "label": "Work",
    "streetAddress": "KN 456 Ave",
    "city": "Kigali",
    "state": "Kigali City",
    "postalCode": "00000",
    "country": "Rwanda",
    "isDefault": false
  }
]
```

---

### 2. Get Address by ID
Get specific address details.

**Endpoint:** `GET /api/users/me/addresses/{addressId}`  
**Authentication:** Required

**Success Response (200):**
```json
{
  "id": 1,
  "label": "Home",
  "streetAddress": "KG 123 St",
  "city": "Kigali",
  "state": "Kigali City",
  "postalCode": "00000",
  "country": "Rwanda",
  "isDefault": true
}
```

---

### 3. Get Default Address
Get user's default shipping address.

**Endpoint:** `GET /api/users/me/addresses/default`  
**Authentication:** Required

**Success Response (200):**
```json
{
  "id": 1,
  "label": "Home",
  "streetAddress": "KG 123 St",
  "city": "Kigali",
  "state": "Kigali City",
  "postalCode": "00000",
  "country": "Rwanda",
  "isDefault": true
}
```

---

### 4. Create Address
Add a new address.

**Endpoint:** `POST /api/users/me/addresses`  
**Authentication:** Required

**Request Body:**
```json
{
  "label": "Home",
  "streetAddress": "KG 123 St",
  "city": "Kigali",
  "state": "Kigali City",
  "postalCode": "00000",
  "country": "Rwanda",
  "isDefault": true
}
```

**Success Response (201):**
```json
{
  "id": 1,
  "label": "Home",
  "streetAddress": "KG 123 St",
  "city": "Kigali",
  "state": "Kigali City",
  "postalCode": "00000",
  "country": "Rwanda",
  "isDefault": true
}
```

---

### 5. Update Address
Update existing address.

**Endpoint:** `PUT /api/users/me/addresses/{addressId}`  
**Authentication:** Required

**Request Body:**
```json
{
  "label": "Home Updated",
  "streetAddress": "KG 789 St",
  "city": "Kigali",
  "state": "Kigali City",
  "postalCode": "00000",
  "country": "Rwanda",
  "isDefault": true
}
```

**Success Response (200):**
```json
{
  "id": 1,
  "label": "Home Updated",
  "streetAddress": "KG 789 St",
  ...
}
```

---

### 6. Set Default Address
Set an address as default.

**Endpoint:** `PUT /api/users/me/addresses/{addressId}/default`  
**Authentication:** Required

**Success Response (200):**
```json
{
  "id": 1,
  "label": "Home",
  "isDefault": true,
  ...
}
```

---

### 7. Delete Address
Delete an address.

**Endpoint:** `DELETE /api/users/me/addresses/{addressId}`  
**Authentication:** Required

**Success Response (200):**
```json
{
  "message": "Address deleted successfully"
}
```

---

## Admin Endpoints
All admin endpoints require ADMIN role.

### 1. Get All Users (Paginated)
Get all users with pagination.

**Endpoint:** `GET /api/admin/users?page=0&size=20`  
**Authentication:** Required (ADMIN)

**Success Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "username": "johndoe",
      "email": "john@example.com",
      "role": "USER",
      "status": "ACTIVE"
    },
    ...
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 100,
  "totalPages": 5
}
```

---

### 2. Get User by ID (Admin)
Get full user details including sensitive info.

**Endpoint:** `GET /api/admin/users/{userId}`  
**Authentication:** Required (ADMIN)

**Success Response (200):**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+250788123456",
  "role": "USER",
  "status": "ACTIVE",
  "emailVerified": true,
  "failedLoginAttempts": 0,
  "createdAt": "2025-11-01T10:00:00",
  "lastLoginAt": "2025-11-12T10:30:00",
  "addresses": [...]
}
```

---

### 3. Get Users by Status
Filter users by status.

**Endpoint:** `GET /api/admin/users/status/{status}`  
**Authentication:** Required (ADMIN)

**Status values:** ACTIVE, SUSPENDED, BANNED, PENDING_VERIFICATION

**Success Response (200):**
```json
[
  {
    "id": 1,
    "username": "johndoe",
    "status": "ACTIVE",
    ...
  },
  ...
]
```

---

### 4. Update User Status
Change user account status.

**Endpoint:** `PUT /api/admin/users/{userId}/status`  
**Authentication:** Required (ADMIN)

**Request Body:**
```json
{
  "status": "SUSPENDED"
}
```

**Success Response (200):**
```json
{
  "id": 1,
  "username": "johndoe",
  "status": "SUSPENDED",
  ...
}
```

---

### 5. Update User Role
Change user role.

**Endpoint:** `PUT /api/admin/users/{userId}/role`  
**Authentication:** Required (ADMIN)

**Request Body:**
```json
{
  "role": "ADMIN"
}
```

**Success Response (200):**
```json
{
  "id": 1,
  "username": "johndoe",
  "role": "ADMIN",
  ...
}
```

---

### 6. Delete User (Admin)
Permanently delete a user account.

**Endpoint:** `DELETE /api/admin/users/{userId}`  
**Authentication:** Required (ADMIN)

**Success Response (200):**
```json
{
  "message": "User deleted successfully"
}
```

---

## Email Verification Endpoints

### 1. Verify Email
Verify email using token from email.

**Endpoint:** `GET /api/auth/verify-email?token={token}`  
**Authentication:** Not required

**Success Response (200):**
```json
{
  "message": "Email verified successfully"
}
```

---

### 2. Resend Verification Email
Request new verification email.

**Endpoint:** `POST /api/auth/resend-verification`  
**Authentication:** Not required

**Request Body:**
```json
{
  "email": "john@example.com"
}
```

**Success Response (200):**
```json
{
  "message": "Verification email sent"
}
```

---

## Password Reset Endpoints

### 1. Request Password Reset
Request password reset link via email.

**Endpoint:** `POST /api/auth/forgot-password`  
**Authentication:** Not required

**Request Body:**
```json
{
  "email": "john@example.com"
}
```

**Success Response (200):**
```json
{
  "message": "Password reset link sent to your email"
}
```

---

### 2. Reset Password
Reset password using token from email.

**Endpoint:** `POST /api/auth/reset-password`  
**Authentication:** Not required

**Request Body:**
```json
{
  "token": "reset-token-from-email",
  "newPassword": "NewSecurePass789!"
}
```

**Success Response (200):**
```json
{
  "message": "Password reset successfully"
}
```

**Error Response (401):**
```json
{
  "status": 401,
  "message": "Invalid or expired reset token",
  "timestamp": "2025-11-12T10:30:00"
}
```

---

## Error Responses

All endpoints may return these common error responses:

### 400 - Bad Request (Validation Error)
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "email": "Email must be valid",
    "password": "Password must be at least 8 characters"
  },
  "timestamp": "2025-11-12T10:30:00"
}
```

### 401 - Unauthorized
```json
{
  "status": 401,
  "message": "Invalid credentials",
  "timestamp": "2025-11-12T10:30:00"
}
```

### 403 - Forbidden
```json
{
  "status": 403,
  "message": "Access denied",
  "timestamp": "2025-11-12T10:30:00"
}
```

### 404 - Not Found
```json
{
  "status": 404,
  "message": "User not found",
  "timestamp": "2025-11-12T10:30:00"
}
```

### 409 - Conflict
```json
{
  "status": 409,
  "message": "Email already registered",
  "timestamp": "2025-11-12T10:30:00"
}
```

### 500 - Internal Server Error
```json
{
  "status": 500,
  "message": "An unexpected error occurred",
  "timestamp": "2025-11-12T10:30:00"
}
```

---

## Rate Limiting

### Login Endpoint
- **Max Attempts:** 5 failed login attempts
- **Lockout Duration:** 15 minutes
- **Behavior:** Account temporarily locked after exceeding limit

---

## Testing with cURL

### Register
```bash
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Login
```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'
```

### Get Profile
```bash
curl -X GET http://localhost:8082/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Create Address
```bash
curl -X POST http://localhost:8082/api/users/me/addresses \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "label": "Home",
    "streetAddress": "KG 123 St",
    "city": "Kigali",
    "country": "Rwanda",
    "isDefault": true
  }'
```

---

## Postman Collection

Import this JSON into Postman to test all endpoints:

1. Create new collection: "User Service API"
2. Add environment variables:
    - `base_url`: http://localhost:8082
    - `access_token`: (will be set automatically after login)
3. Import all endpoints from this documentation
4. Use Scripts tab to auto-save tokens

**Login Script (Tests tab):**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("access_token", jsonData.accessToken);
    pm.environment.set("refresh_token", jsonData.refreshToken);
}
```

---

## Additional Notes

- All timestamps are in ISO 8601 format
- Passwords must be at least 8 characters
- JWT tokens expire after 15 minutes
- Refresh tokens expire after 7 days
- Email verification tokens expire after 24 hours
- Password reset tokens expire after 1 hour