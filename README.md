<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen?style=for-the-badge&logo=spring-boot" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk" alt="Java"/>
  <img src="https://img.shields.io/badge/JWT-Authentication-blue?style=for-the-badge&logo=json-web-tokens" alt="JWT"/>
  <img src="https://img.shields.io/badge/OAuth2-Google-red?style=for-the-badge&logo=google" alt="OAuth2"/>
</p>

<h1 align="center">🔐 Authentication API</h1>

<p align="center">
  <strong>A production-ready, secure REST API for authentication using JWT and OAuth2</strong>
</p>

<p align="center">
  <a href="#-features">Features</a> •
  <a href="#-tech-stack">Tech Stack</a> •
  <a href="#-quick-start">Quick Start</a> •
  <a href="#-api-documentation">API Docs</a> •
  <a href="#-testing">Testing</a> •
  <a href="#-deployment">Deployment</a>
</p>

---

## ✨ Features

### 🔑 Authentication
- **JWT Token Authentication** - Secure stateless authentication with access tokens
- **OAuth2 with Google** - Social login integration with Google accounts
- **Email Verification** - Account activation via email confirmation
- **Role-Based Access Control** - USER and ADMIN roles with protected endpoints

### 🛡️ Security
- **BCrypt Password Hashing** - Industry-standard password encryption
- **HTTP-Only Cookies** - XSS protection for JWT tokens
- **CORS Configuration** - Cross-origin resource sharing setup
- **Input Validation** - Request validation with Jakarta Bean Validation

### 📧 Email
- **Welcome Emails** - Beautiful HTML emails for new users
- **Verification Emails** - Secure account activation links
- **SMTP Integration** - Works with any SMTP provider (Mailtrap, Gmail, SendGrid)

### 📖 Documentation
- **OpenAPI 3.0** - Full API documentation with Swagger UI
- **Interactive Testing** - Try endpoints directly from the browser
- **Request/Response Examples** - Clear examples for all endpoints

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Framework** | Spring Boot 3.5.0 |
| **Language** | Java 17 |
| **Security** | Spring Security 6, JWT, OAuth2 |
| **Database** | PostgreSQL + Spring Data JPA |
| **Documentation** | SpringDoc OpenAPI 3 (Swagger) |
| **Email** | Spring Mail + JavaMailSender |
| **Build** | Maven |
| **Testing** | JUnit 5, Mockito, MockMvc |

---

## 🚀 Quick Start

### Prerequisites

- ☕ **Java 17** or higher
- 🐘 **PostgreSQL** 14 or higher
- 📦 **Maven** 3.8 or higher
- 🔑 **Google OAuth2 Credentials** (optional, for social login)

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/yourusername/authentication-api.git
cd authentication-api/backend
```

### 2️⃣ Configure the Database

Create a PostgreSQL database:

```sql
CREATE DATABASE auth_db;
```

### 3️⃣ Configure Environment

Copy the template and fill in your values:

```bash
cp src/main/resources/application.properties.template src/main/resources/application-dev.properties
```

Edit `application-dev.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/auth_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT Secret (generate at https://generate-secret.vercel.app/32)
jwt.secret=your_jwt_secret_here

# Google OAuth2 (get from Google Cloud Console)
spring.security.oauth2.client.registration.google.client-id=your_client_id
spring.security.oauth2.client.registration.google.client-secret=your_client_secret

# Email (use Mailtrap for development)
spring.mail.host=smtp.mailtrap.io
spring.mail.username=your_mailtrap_username
spring.mail.password=your_mailtrap_password
```

### 4️⃣ Run the Application

```bash
# Using Maven
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or on Windows
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

### 5️⃣ Access the API

| Service | URL |
|---------|-----|
| 🌐 **API Base URL** | http://localhost:8080 |
| 📖 **Swagger UI** | http://localhost:8080/swagger-ui.html |
| 📋 **OpenAPI JSON** | http://localhost:8080/api-docs |
| 🔐 **Google Login** | http://localhost:8080/oauth2/authorization/google |

---

## 📖 API Documentation

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/auth/register` | Register a new user | ❌ |
| `POST` | `/api/auth/login` | Login with credentials | ❌ |
| `POST` | `/api/auth/logout` | Logout (clear cookie) | ❌ |
| `GET` | `/api/auth/me` | Get current user info | ✅ JWT |
| `GET` | `/api/auth/verify` | Verify email address | ❌ |
| `POST` | `/api/auth/resend-verification` | Resend verification email | ❌ |
| `GET` | `/api/auth/token` | Get current JWT token | ❌ |
| `GET` | `/api/auth/health` | Health check | ❌ |

### Admin Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `PUT` | `/api/admin/users/role` | Change user role | ✅ ADMIN |

### Request Examples

<details>
<summary>📝 Register a New User</summary>

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "securePassword123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Response:**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "message": "User registered successfully. Please verify your email.",
  "success": true,
  "role": "USER"
}
```
</details>

<details>
<summary>🔓 Login</summary>

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "john@example.com",
    "password": "securePassword123"
  }'
```

**Response:**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "message": "Login successful",
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "USER"
}
```
</details>

<details>
<summary>👑 Change User Role (Admin Only)</summary>

```bash
curl -X PUT http://localhost:8080/api/admin/users/role \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '{
    "email": "john@example.com",
    "role": "ADMIN"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Role updated successfully",
  "user": {
    "id": 1,
    "email": "john@example.com",
    "username": "johndoe",
    "role": "ADMIN"
  }
}
```
</details>

<details>
<summary>👤 Get Current User</summary>

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "success": true,
  "role": "USER"
}
```
</details>

<details>
<summary>🚪 Logout</summary>

```bash
curl -X POST http://localhost:8080/api/auth/logout
```

**Response:**
```json
{
  "message": "Logout successful",
  "success": true
}
```
</details>

<details>
<summary>📧 Resend Verification Email</summary>

```bash
curl -X POST "http://localhost:8080/api/auth/resend-verification?email=john@example.com"
```

**Response:**
```json
{
  "message": "Verification email sent successfully",
  "success": true
}
```
</details>

---

## 🧪 Testing

### Run All Tests

```bash
./mvnw test
```

### Run Specific Test Classes

```bash
# Unit tests
./mvnw test -Dtest=JwtServiceTest
./mvnw test -Dtest=AuthServiceTest
./mvnw test -Dtest=UserServiceTest
./mvnw test -Dtest=EmailServiceTest

# Integration tests
./mvnw test -Dtest=AuthControllerTest
./mvnw test -Dtest=AdminControllerTest
```

### Test Coverage

| Component | Tests | Coverage |
|-----------|-------|----------|
| JwtService | 35+ | JWT generation, validation, expiration |
| AuthService | 15+ | Registration, login, verification |
| UserService | 25+ | User management, role changes |
| EmailService | 18+ | Email sending, templates |
| AuthController | 20+ | HTTP endpoints, validation |
| AdminController | 16+ | Admin operations, authorization |

---

## 🚢 Deployment

### Environment Variables (Production)

```bash
# Database
DATABASE_URL=jdbc:postgresql://host:5432/db_name
DATABASE_USERNAME=username
DATABASE_PASSWORD=password

# JWT
JWT_SECRET=your_production_jwt_secret

# OAuth2
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Email
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=your_email
MAIL_PASSWORD=your_app_password

# App
APP_URL=https://your-domain.com
SWAGGER_ENABLED=false
```

### Docker Deployment

```dockerfile
FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=prod"]
```

Build and run:

```bash
./mvnw clean package -DskipTests
docker build -t authentication-api .
docker run -p 8080:8080 --env-file .env authentication-api
```

### Cloud Platforms

<details>
<summary>🚂 Railway</summary>

1. Connect your GitHub repository
2. Add environment variables in Railway dashboard
3. Deploy automatically on push

</details>

<details>
<summary>🔷 Heroku</summary>

```bash
heroku create your-app-name
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set DATABASE_URL=your_database_url
# Add other environment variables
git push heroku main
```

</details>

<details>
<summary>☁️ AWS Elastic Beanstalk</summary>

1. Package as JAR: `./mvnw clean package`
2. Upload to Elastic Beanstalk
3. Configure environment variables
4. Deploy

</details>

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/cristian/backend/
│   │   ├── config/           # Security & OpenAPI configuration
│   │   ├── controller/       # REST controllers
│   │   ├── dto/              # Data transfer objects
│   │   ├── exception/        # Custom exceptions & handlers
│   │   ├── model/            # JPA entities
│   │   ├── repository/       # Data repositories
│   │   ├── security/         # JWT & OAuth2 components
│   │   └── service/          # Business logic
│   └── resources/
│       ├── application.properties
│       ├── application-dev.properties
│       └── application-prod.properties
└── test/
    └── java/com/cristian/backend/
        ├── controller/       # Controller tests
        ├── security/         # Security tests
        └── service/          # Service tests
```

---

## 🔧 Configuration

### Google OAuth2 Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Navigate to **APIs & Services** > **Credentials**
4. Click **Create Credentials** > **OAuth 2.0 Client IDs**
5. Configure consent screen
6. Add authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`
7. Copy Client ID and Client Secret

### Using OAuth2 from Frontend

After successful OAuth2 authentication, users are redirected to your frontend application at `{frontendUrl}/auth/callback` with a JWT token set as an HTTP-only cookie.

#### Initiating OAuth2 Login

```javascript
// Redirect user to Google login
window.location.href = 'http://localhost:8080/oauth2/authorization/google';
```

#### Handling the Callback in Frontend

```javascript
// In your /auth/callback page (React example)
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

function AuthCallback() {
  const navigate = useNavigate();

  useEffect(() => {
    // The JWT token is automatically set as an HTTP-only cookie
    // Verify authentication by calling the token endpoint
    fetch('http://localhost:8080/api/auth/token', { 
      credentials: 'include' 
    })
      .then(res => res.json())
      .then(data => {
        if (data.token) {
          // User is authenticated, redirect to dashboard
          navigate('/dashboard');
        } else {
          navigate('/login?error=auth_failed');
        }
      })
      .catch(() => navigate('/login?error=auth_failed'));
  }, [navigate]);

  return <div>Authenticating...</div>;
}

export default AuthCallback;
```

#### Frontend Configuration

Configure the frontend URL in your backend properties:

```properties
# application-dev.properties
app.frontend.url=http://localhost:3000

# application-prod.properties  
app.frontend.url=${FRONTEND_URL:https://your-frontend.com}
```

### JWT Secret Generation

Generate a secure 256-bit secret:

```bash
# Using OpenSSL
openssl rand -base64 32

# Or use online generator
# https://generate-secret.vercel.app/32
```

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Cristian Cortes**

- GitHub: [@cristiancortes1](https://github.com/cristiancortes1)
- Email: bejaranno05cortes@gmail.com

---

<p align="center">
  Made with ❤️ and ☕
</p>

<p align="center">
  <a href="#-authentication-api">Back to top ⬆️</a>
</p>

