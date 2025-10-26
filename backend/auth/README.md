# Authentication Service - Digital Banking Platform

## Table of Contents
1. [Overview](#overview)
2. [System Architecture](#system-architecture)
3. [Technology Stack](#technology-stack)
4. [Core Components](#core-components)
5. [API Documentation](#api-documentation)
6. [Security Implementation](#security-implementation)
7. [Database Design](#database-design)
8. [JWT Token Management](#jwt-token-management)
9. [Testing Strategy](#testing-strategy)
10. [Setup & Installation](#setup--installation)
11. [Configuration](#configuration)
12. [Running the Application](#running-the-application)
13. [Future Enhancements](#future-enhancements)

---

## Overview

The Authentication Service is a microservice component of the Digital Banking Platform responsible for user authentication, authorization, and session management. It provides secure JWT-based authentication for all services in the banking ecosystem.

### Key Features
- **User Registration** - Secure user account creation with password hashing
- **User Authentication** - Login with username/password credentials
- **JWT Token Generation** - Stateless session management using JSON Web Tokens
- **Token Validation** - Verify and decode JWT tokens for protected resources
- **User Management** - CRUD operations for user accounts
- **Security** - BCrypt password hashing, CSRF protection, and secure headers

### Service Responsibilities
- Managing user credentials and identity
- Issuing and validating authentication tokens
- Providing authentication status to other microservices
- Enforcing security policies and access control

---

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Client Applications                       │
│         (Web App, Mobile App, Third-party Services)         │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTP/HTTPS
                     │ JSON Payloads
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   Spring Security Filter Chain               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │   CSRF   │→ │   Auth   │→ │  Session │→ │  Headers │   │
│  │ Disabled │  │  Filter  │  │ Stateless│  │  Filter  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                     AuthController Layer                     │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  /auth/register  │  /auth/login  │  /auth/validate   │  │
│  │  /auth/verify    │  /auth/me     │  /auth/user/{id}  │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                     UserService Layer                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  • User Registration Logic                           │  │
│  │  • Password Hashing (BCrypt)                         │  │
│  │  • Authentication Logic                              │  │
│  │  • JWT Token Operations                              │  │
│  │  • User CRUD Operations                              │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   Repository Layer (JPA)                     │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  UserRepository (Spring Data JPA)                    │  │
│  │  • findByUsername()                                  │  │
│  │  • save()                                            │  │
│  │  • delete()                                          │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   PostgreSQL Database                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  users Table                                         │  │
│  │  ├─ id (Primary Key)                                 │  │
│  │  ├─ username (Unique)                                │  │
│  │  ├─ password_hash                                    │  │
│  │  └─ email                                            │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘

                     ┌──────────────┐
                     │   JwtUtil    │
                     │  (Singleton) │
                     │              │
                     │ • Generate   │
                     │ • Validate   │
                     │ • Extract    │
                     └──────────────┘
```

### Request Flow

#### 1. User Registration Flow
```
Client → POST /auth/register
    ↓
SecurityFilterChain (permits /auth/**)
    ↓
AuthController.register()
    ↓
UserService.register()
    ├─ Check username exists
    ├─ Hash password (BCrypt)
    └─ Save to UserRepository
        ↓
    PostgreSQL (INSERT)
        ↓
    Response: {"message": "User registered successfully"}
```

#### 2. User Login Flow
```
Client → POST /auth/login
    ↓
SecurityFilterChain (permits /auth/**)
    ↓
AuthController.login()
    ↓
UserService.login()
    ├─ UserService.authenticate()
    │   ├─ Fetch user from database
    │   └─ Compare passwords (BCrypt)
    └─ JwtUtil.generateToken()
        ├─ Create Claims (username, issued time, expiry)
        ├─ Sign with secret key (HS256)
        └─ Return JWT string
            ↓
    Response: {"token": "eyJhbGci..."}
```

#### 3. Token Validation Flow
```
Client → GET /auth/verify
         Header: Authorization: Bearer <token>
    ↓
SecurityFilterChain
    ↓
AuthController.verify()
    ├─ Extract token from header
    └─ UserService.validate()
        └─ JwtUtil.validateToken()
            ├─ Parse JWT
            ├─ Verify signature
            ├─ Check expiration
            └─ Extract username
                ↓
    Response: {"valid": true, "username": "user123"}
```

---

## Technology Stack

### Core Framework
- **Spring Boot 3.5.7** - Application framework
- **Kotlin 1.9.25** - Primary programming language
- **Java 21** - JVM runtime

### Security
- **Spring Security 6.5.6** - Authentication and authorization
- **BCrypt** - Password hashing algorithm
- **JJWT 0.11.5** - JWT creation and validation

### Database
- **PostgreSQL** - Primary relational database
- **Spring Data JPA** - Data access layer
- **Hibernate 6.6.33** - ORM framework
- **HikariCP** - Database connection pooling

### Testing
- **JUnit 5** - Testing framework
- **Spring Boot Test** - Integration testing
- **MockMvc** - REST API testing
- **Kotlin Test** - Kotlin-specific assertions

### Build & Development
- **Gradle 8.14.3** - Build automation
- **Spring DevTools** - Development time features
- **Kotlin All-Open Plugin** - For JPA entities
- **Kotlin No-Arg Plugin** - Default constructors for JPA

---

## Core Components

### 1. Domain Model

#### User Entity
```kotlin
@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val username: String,
    
    @Column(nullable = false)
    val passwordHash: String,
    
    @Column
    val email: String? = null
)
```

**Design Decisions:**
- `id`: Auto-generated primary key for database efficiency
- `username`: Unique constraint for authentication
- `passwordHash`: Never store plain passwords
- `email`: Optional for future features (password reset, notifications)

### 2. Repository Layer

```kotlin
@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): Optional<User>
}
```

**Features:**
- Extends `JpaRepository` for CRUD operations
- Custom query method `findByUsername()` for authentication
- Automatic query generation by Spring Data JPA

### 3. Service Layer

#### UserService
The business logic layer handling all user operations.

**Key Methods:**

```kotlin
// User Registration
fun register(username: String, password: String, email: String?): User
    - Validates username uniqueness
    - Hashes password using BCrypt
    - Persists user to database

// Authentication
fun authenticate(username: String, password: String): Boolean
    - Fetches user by username
    - Compares password hashes

// Login
fun login(username: String, password: String): String?
    - Authenticates user
    - Generates JWT token on success

// Token Validation
fun validate(token: String): String?
    - Validates JWT signature
    - Returns username if valid

// Token Verification
fun verifyToken(token: String): Boolean
    - Checks token validity and expiration

// User Management
fun getUserByUsername(username: String): User?
fun deleteUser(username: String): Boolean
fun deleteAllUsers()
```

**Security Features:**
- BCrypt password hashing (work factor: 10)
- No plain password storage or transmission
- Null-safe operations with Optional handling

### 4. Controller Layer

#### AuthController
REST API endpoints for authentication operations.

```kotlin
@RestController
@RequestMapping("/auth")
class AuthController(private val authService: UserService)
```

**Endpoints:**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/register` | Register new user | No |
| POST | `/auth/login` | Login and get JWT | No |
| POST | `/auth/validate` | Validate JWT token | No |
| GET | `/auth/verify` | Verify token via header | No |
| GET | `/auth/me` | Get current user info | Yes (Bearer token) |
| DELETE | `/auth/user/{username}` | Delete user account | No |

**Request/Response DTOs:**
- `RegisterRequest(username, password, email)`
- `LoginRequest(username, password)`
- `TokenResponse(token)`
- `ValidateRequest(token)`
- `ValidateResponse(valid, username)`

### 5. JWT Utility

#### JwtUtil (Singleton Object)
Handles all JWT operations with secure key management.

```kotlin
object JwtUtil {
    private val secretKey: SecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)
    private const val expirationMs = 3600000L // 1 hour
    
    fun generateToken(username: String): String
    fun validateToken(token: String): String?
    fun extractUsername(token: String): String?
    fun isTokenExpired(token: String): Boolean
}
```

**Security Features:**
- Cryptographically secure key generation
- HS256 (HMAC-SHA256) signing algorithm
- 1-hour token expiration
- Automatic signature verification

### 6. Security Configuration

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }  // Disabled for JWT-based auth
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/auth/**").permitAll()
                    .anyRequest().authenticated()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
        
        return http.build()
    }
}
```

**Configuration Rationale:**
- **CSRF Disabled**: JWT tokens provide CSRF protection
- **Stateless Sessions**: No server-side session storage
- **Permit /auth/** : Authentication endpoints are public
- **HTTP Basic Disabled**: Using JWT instead
- **Form Login Disabled**: REST API, not form-based

---

## API Documentation

### 1. Register User

**Endpoint:** `POST /auth/register`

**Request:**
```json
{
  "username": "john_doe",
  "password": "SecurePass123!",
  "email": "john@example.com"
}
```

**Success Response (200 OK):**
```json
{
  "message": "User registered successfully"
}
```

**Error Response (409 Conflict):**
```json
{
  "error": "Username already exists"
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "error": "Registration failed"
}
```

---

### 2. Login

**Endpoint:** `POST /auth/login`

**Request:**
```json
{
  "username": "john_doe",
  "password": "SecurePass123!"
}
```

**Success Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTcwMDAwMDAwMCwiZXhwIjoxNzAwMDAzNjAwfQ.signature"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "error": "Invalid credentials"
}
```

---

### 3. Validate Token

**Endpoint:** `POST /auth/validate`

**Request:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Success Response (200 OK):**
```json
{
  "valid": true,
  "username": "john_doe"
}
```

**Invalid Token Response (200 OK):**
```json
{
  "valid": false,
  "username": null
}
```

---

### 4. Verify Token (via Header)

**Endpoint:** `GET /auth/verify`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Success Response (200 OK):**
```json
{
  "valid": true,
  "username": "john_doe"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "error": "Missing or invalid token"
}
```

---

### 5. Get Current User

**Endpoint:** `GET /auth/me`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Success Response (200 OK):**
```json
{
  "username": "john_doe",
  "email": "john@example.com"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "error": "Invalid token"
}
```

**Error Response (404 Not Found):**
```json
{
  "error": "User not found"
}
```

---

### 6. Delete User

**Endpoint:** `DELETE /auth/user/{username}`

**Success Response (200 OK):**
```json
{
  "message": "User deleted successfully"
}
```

**Error Response (404 Not Found):**
```json
{
  "error": "User not found"
}
```

---

## Security Implementation

### 1. Password Security

#### BCrypt Hashing
```kotlin
private val passwordEncoder = BCryptPasswordEncoder()

// During registration
val hashed = passwordEncoder.encode(password)

// During login
passwordEncoder.matches(password, user.passwordHash)
```

**Properties:**
- **Algorithm**: BCrypt with salt
- **Work Factor**: 10 (default, adjustable)
- **Rainbow Table Protection**: Automatic salting
- **Brute Force Protection**: Intentionally slow hashing

### 2. JWT Token Structure

**Header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload:**
```json
{
  "sub": "username",
  "iat": 1700000000,
  "exp": 1700003600
}
```

**Signature:**
```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secretKey
)
```

### 3. Security Headers

Automatically applied by Spring Security:
- `X-Content-Type-Options: nosniff`
- `X-XSS-Protection: 0`
- `Cache-Control: no-cache, no-store, max-age=0, must-revalidate`
- `Pragma: no-cache`
- `Expires: 0`
- `X-Frame-Options: DENY`

### 4. Input Validation

**Implemented:**
- Username uniqueness check
- Null-safe operations
- Optional handling for database queries

**Recommended Additions:**
- Password strength validation
- Email format validation
- Username format restrictions
- Rate limiting for login attempts

---

## Database Design

### Schema

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255)
);

CREATE INDEX idx_users_username ON users(username);
```

### Entity Relationships

Currently a single-table design. Future extensions:

```
users (1) ─── (N) user_roles
users (1) ─── (N) user_sessions
users (1) ─── (N) password_history
users (1) ─── (1) user_profile
```

### Database Configuration

**Location:** `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/database
spring.datasource.username=username
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

---

## JWT Token Management

### Token Lifecycle

```
┌─────────────┐
│   Login     │
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│  Generate Token     │
│  - Subject: username│
│  - Issued: now()    │
│  - Expires: +1h     │
│  - Sign with key    │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Client Stores      │
│  (localStorage/     │
│   sessionStorage)   │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Include in Header  │
│  Authorization:     │
│  Bearer <token>     │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Server Validates   │
│  - Signature valid? │
│  - Not expired?     │
│  - User exists?     │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Grant Access       │
└─────────────────────┘
```

### Token Expiration Strategy

**Current:** 1 hour (3600000ms)

**Recommendations:**
- **Short-lived Access Tokens**: 15 minutes
- **Refresh Tokens**: 7 days (not implemented)
- **Remember Me Tokens**: 30 days (not implemented)

### Token Revocation

**Not Currently Implemented**

**Future Implementation Options:**
1. **Blacklist**: Store revoked tokens in Redis
2. **Whitelist**: Store active sessions in database
3. **Token Versioning**: Increment user token version on logout

---

## Testing Strategy

### Test Structure

```
src/test/kotlin/com/bank/auth/
└── AuthApplicationTests.kt
    ├── Context Tests (1)
    ├── Registration Tests (2)
    ├── Login Tests (3)
    ├── Token Validation Tests (4)
    ├── User Info Tests (2)
    ├── Delete User Tests (2)
    └── Integration Tests (2)
Total: 16 tests
```

### Test Categories

#### 1. Unit Tests
```kotlin
@Test
fun `should register a new user successfully`()
@Test
fun `should return conflict when registering duplicate username`()
@Test
fun `should login successfully with valid credentials`()
@Test
fun `should fail login with invalid password`()
```

#### 2. Integration Tests
```kotlin
@Test
fun `should support multiple users`()
@Test
fun `should handle complete user lifecycle`()
```

#### 3. Security Tests
```kotlin
@Test
fun `should validate a valid token`()
@Test
fun `should invalidate an invalid token`()
@Test
fun `should reject verify request without Authorization header`()
```

### Test Database

Tests use the same PostgreSQL database configured in `application.properties`.

**Cleanup Strategy:**
- `@BeforeEach`: Delete test users before each test
- `@AfterEach`: Delete test users after each test
- Ensures test isolation and repeatability

### Running Tests

```bash
# Run all tests
./gradlew test

# Run with detailed output
./gradlew test --info

# Run specific test
./gradlew test --tests "AuthApplicationTests.should login successfully*"

# Generate test report
./gradlew test
# View: build/reports/tests/test/index.html
```

### Test Coverage

**Current Coverage:**
- ✅ User registration (success & failure)
- ✅ User authentication (success & failure)
- ✅ JWT generation and validation
- ✅ Token expiration handling
- ✅ Authorization header parsing
- ✅ User deletion
- ✅ Multi-user scenarios
- ✅ Complete lifecycle testing

**Not Covered (Future):**
- Password strength validation
- Email validation
- Rate limiting
- Token refresh logic
- Account lockout after failed attempts

---

## Setup & Installation

### Prerequisites

1. **Java 21**
   ```bash
   java --version
   # openjdk 21.0.9 or higher
   ```

2. **PostgreSQL 15+**
   ```bash
   psql --version
   # PostgreSQL 15.14 or higher
   ```

3. **Gradle 8.14.3** (included via wrapper)

### Database Setup

```bash
# 1. Start PostgreSQL
sudo systemctl start postgresql

# 2. Create database
psql -U postgres
CREATE DATABASE database;
CREATE USER username WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE database TO username;
\q

# 3. Verify connection
psql -U username -d database -h localhost
```

### Application Setup

```bash
# 1. Clone repository
cd /path/to/digital_banking_platform/backend/auth-service

# 2. Make gradlew executable
chmod +x gradlew

# 3. Build project
./gradlew build

# 4. Run tests
./gradlew test
```

---

## Configuration

### Application Properties

**File:** `src/main/resources/application.properties`

```properties
# Application Name
spring.application.name=auth

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/database
spring.datasource.username=username
spring.datasource.password=password

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Environment-Specific Configuration

**Development:**
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
logging.level.org.springframework.security=DEBUG
```

**Production:**
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
logging.level.root=INFO
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_PASSWORD}
```

### JWT Configuration

Currently hardcoded in `JwtUtil.kt`:

```kotlin
private const val expirationMs = 3600000L // 1 hour
```

**Recommended:** Move to application.properties:
```properties
jwt.expiration.ms=3600000
jwt.secret=${JWT_SECRET}  # From environment variable
```

---

## Running the Application

### Development Mode

```bash
# Run with DevTools (auto-reload)
./gradlew bootRun
```

**Accessible at:** `http://localhost:8080`

### Production Build

```bash
# 1. Clean and build
./gradlew clean build

# 2. Run JAR
java -jar build/libs/auth-0.0.1-SNAPSHOT.jar

# 3. With custom port
java -jar build/libs/auth-0.0.1-SNAPSHOT.jar --server.port=9090
```

### Docker Deployment (Future)

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/auth-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
docker build -t auth-service:latest .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/database \
  auth-service:latest
```

---

## Future Enhancements

### 1. Security Improvements

- [ ] **Refresh Tokens**: Implement long-lived refresh tokens
- [ ] **Token Revocation**: Redis-based token blacklist
- [ ] **Password Policies**: Strength requirements, history tracking
- [ ] **Account Lockout**: After N failed login attempts
- [ ] **2FA/MFA**: TOTP or SMS-based second factor
- [ ] **Rate Limiting**: Prevent brute force attacks
- [ ] **OAuth2/OpenID Connect**: Social login integration

### 2. User Management

- [ ] **Email Verification**: Confirm email during registration
- [ ] **Password Reset**: Forgot password flow
- [ ] **User Roles**: Admin, User, Manager permissions
- [ ] **User Profiles**: Extended user information
- [ ] **Account Status**: Active, Suspended, Deleted states

### 3. Observability

- [ ] **Logging**: Structured logging with correlation IDs
- [ ] **Metrics**: Prometheus/Micrometer integration
- [ ] **Tracing**: Distributed tracing with Zipkin/Jaeger
- [ ] **Health Checks**: Liveness and readiness probes
- [ ] **Audit Logs**: Track all authentication events

### 4. Performance

- [ ] **Caching**: Redis for user sessions and tokens
- [ ] **Database Indexing**: Optimize query performance
- [ ] **Connection Pooling**: Fine-tune HikariCP settings
- [ ] **Async Processing**: Non-blocking I/O where applicable

### 5. API Enhancements

- [ ] **API Versioning**: `/v1/auth/...`, `/v2/auth/...`
- [ ] **GraphQL Support**: Alternative to REST
- [ ] **WebSocket**: Real-time notifications
- [ ] **API Documentation**: OpenAPI/Swagger integration
- [ ] **HATEOAS**: Hypermedia links in responses

### 6. Testing

- [ ] **Contract Testing**: Pact or Spring Cloud Contract
- [ ] **Load Testing**: Gatling or JMeter scenarios
- [ ] **Security Testing**: OWASP ZAP automated scans
- [ ] **Mutation Testing**: PIT for test quality

### 7. Infrastructure

- [ ] **Kubernetes**: Deployment manifests
- [ ] **Service Mesh**: Istio or Linkerd integration
- [ ] **CI/CD Pipeline**: GitHub Actions or Jenkins
- [ ] **Environment Management**: Dev, Staging, Production
- [ ] **Secrets Management**: Vault or AWS Secrets Manager

---

## Project Structure

```
auth-service/
├── build.gradle.kts              # Gradle build configuration
├── settings.gradle.kts           # Project settings
├── gradlew                       # Gradle wrapper (Unix)
├── gradlew.bat                   # Gradle wrapper (Windows)
├── README.md                     # This file
├── HELP.md                       # Spring Boot help
│
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/bank/auth/
│   │   │       ├── AuthApplication.kt          # Application entry point
│   │   │       ├── config/
│   │   │       │   └── SecurityConfig.kt       # Spring Security configuration
│   │   │       ├── controller/
│   │   │       │   └── AuthController.kt       # REST API endpoints
│   │   │       ├── model/
│   │   │       │   └── User.kt                 # User entity
│   │   │       ├── repository/
│   │   │       │   └── UserRepository.kt       # Data access layer
│   │   │       ├── service/
│   │   │       │   └── UserService.kt          # Business logic
│   │   │       └── util/
│   │   │           └── JwtUtil.kt              # JWT operations
│   │   │
│   │   └── resources/
│   │       ├── application.properties          # App configuration
│   │       ├── application.yml                 # Alternative config format
│   │       ├── static/                         # Static resources
│   │       └── templates/                      # Template files
│   │
│   └── test/
│       └── kotlin/
│           └── com/bank/auth/
│               └── AuthApplicationTests.kt     # Test suite
│
├── build/                        # Build output (generated)
│   ├── classes/                  # Compiled classes
│   ├── libs/                     # JAR artifacts
│   ├── reports/                  # Test reports
│   └── test-results/             # Test results
│
└── gradle/
    └── wrapper/                  # Gradle wrapper files
```

---

## Development Guidelines

### Code Style

- **Kotlin Conventions**: Follow official Kotlin style guide
- **Naming**: Descriptive, camelCase for functions/variables
- **Documentation**: KDoc for public APIs
- **Formatting**: 4-space indentation, 120 char line limit

### Git Workflow

```bash
# Feature branches
git checkout -b feature/add-password-reset

# Commit messages
git commit -m "feat: add password reset functionality"
git commit -m "fix: resolve token expiration issue"
git commit -m "docs: update API documentation"

# Types: feat, fix, docs, style, refactor, test, chore
```

### Pull Request Checklist

- [ ] Code compiles without warnings
- [ ] All tests pass
- [ ] New tests added for new features
- [ ] Documentation updated
- [ ] No sensitive data in commits
- [ ] Security best practices followed

---

## Troubleshooting

### Common Issues

**1. Database Connection Error**
```
Error: Connection to localhost:5432 refused
```
**Solution:** Ensure PostgreSQL is running and credentials are correct

**2. Port Already in Use**
```
Error: Port 8080 is already in use
```
**Solution:** Change port in application.properties or stop conflicting process

**3. JWT Signature Mismatch**
```
Error: JWT signature does not match
```
**Solution:** Secret key changed between token generation and validation

**4. Tests Failing**
```
Error: User not found
```
**Solution:** Ensure test database is accessible and cleanup hooks are working

### Debug Mode

```bash
# Run with debug logging
./gradlew bootRun --debug

# Or set in application.properties
logging.level.root=DEBUG
logging.level.com.bank.auth=TRACE
```

---

## Contact & Support

**Project:** Digital Banking Platform - Authentication Service  
**Version:** 0.0.1-SNAPSHOT  
**Last Updated:** October 27, 2025  

For issues, questions, or contributions, please refer to the project repository.

---

## License

This project is part of the Digital Banking Platform. All rights reserved.

---

**Built with ❤️ using Kotlin, Spring Boot, and PostgreSQL**
