# Rooms Manager - Communication SaaS Platform MVP

A scalable, enterprise-grade real-time communication SaaS platform similar to Agora.

## Features (MVP Phase 1)

- ✅ Application Registration & Management
- ✅ User Authentication (JWT-based)
- ✅ API Key Management
- ✅ Role-Based Access Control (RBAC)
- ✅ MongoDB Integration
- ✅ Docker Compose Setup

## Tech Stack

- **Backend**: Spring Boot 3.1.5
- **Database**: MongoDB
- **Cache**: Redis
- **Authentication**: JWT (JSON Web Tokens)
- **DevOps**: Docker & Docker Compose

## Project Structure

```
rooms_manager_prompet/
├── auth-service/
│   ├── src/main/java/com/roomsmanager/auth/
│   │   ├── controller/        # REST Controllers
│   │   ├── service/           # Business Logic
│   │   ├── entity/            # MongoDB Documents
│   │   ├── repository/        # Data Access Layer
│   │   ├── dto/               # Data Transfer Objects
│   │   └── config/            # Configuration
│   ├── pom.xml                # Maven Dependencies
│   └── Dockerfile
├── docker-compose.yml         # Multi-service orchestration
├── SYSTEM_PROMPT.md          # Full architectural blueprint
└── README.md
```

## Getting Started

### Prerequisites

- Docker & Docker Compose
- Java 17+ (for local development)
- Maven 3.9+

### Quick Start with Docker

```bash
# Clone the repository
git clone https://github.com/figomigo123/rooms_manager_prompet.git
cd rooms_manager_prompet

# Start all services
docker-compose up -d

# Check service status
docker-compose logs -f auth-service
```

Services will be available at:
- Auth Service: `http://localhost:8001`
- MongoDB: `localhost:27017`
- Redis: `localhost:6379`

### Local Development

```bash
# Build auth-service
cd auth-service
mvn clean install

# Run auth-service
mvn spring-boot:run
```

## API Endpoints

### Authentication

#### Register Application
```bash
curl -X POST http://localhost:8001/auth/v1/apps/register \
  -H "Content-Type: application/json" \
  -d '{
    "appName": "My App",
    "owner": "admin@example.com",
    "plan": "FREE"
  }'
```

**Response:**
```json
{
  "appId": "app_abc123def456",
  "appSecret": "secret-key-uuid",
  "appToken": "eyJhbGc...",
  "appName": "My App",
  "plan": "FREE",
  "createdAt": "2024-01-15T10:30:00",
  "expiresIn": 900000
}
```

#### Login (Get App Token)
```bash
curl -X POST http://localhost:8001/auth/v1/login \
  -H "Content-Type: application/json" \
  -d '{
    "appId": "app_abc123def456",
    "appSecret": "secret-key-uuid"
  }'
```

**Response:**
```json
{
  "appToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "appId": "app_abc123def456",
  "expiresIn": 900000,
  "tokenType": "Bearer"
}
```

#### Verify Token
```bash
curl -X POST http://localhost:8001/auth/v1/verify-token \
  -H "Authorization: Bearer {token}"
```

#### Get App Details
```bash
curl -X GET http://localhost:8001/auth/v1/apps/{appId} \
  -H "Authorization: Bearer {token}"
```

#### Health Check
```bash
curl http://localhost:8001/auth/v1/health
```

## Database Schema

### Collections

#### `applications`
```javascript
{
  _id: ObjectId,
  appId: string (unique),
  appSecret: string (hashed),
  appName: string,
  owner: string,
  plan: string,
  apiKey: string (hashed),
  webhookUrl: string,
  limits: {
    maxRooms: number,
    maxUsersPerRoom: number,
    maxMinutesMonth: number
  },
  createdAt: date,
  updatedAt: date,
  active: boolean
}
```

#### `app_users`
```javascript
{
  _id: ObjectId,
  userId: string,
  appId: string,
  email: string (unique per app),
  passwordHash: string,
  name: string,
  role: string,
  permissions: [string],
  metadata: object,
  status: string,
  createdAt: date,
  updatedAt: date,
  lastLoginAt: date
}
```

#### `api_keys`
```javascript
{
  _id: ObjectId,
  appId: string,
  keyHash: string (unique),
  name: string,
  permissions: [string],
  rateLimit: number,
  lastUsedAt: date,
  createdAt: date,
  expiresAt: date,
  active: boolean
}
```

## Configuration

### Environment Variables

```yaml
# JWT
JWT_SECRET: your-secret-key (min 32 chars)
JWT_EXPIRATION: 900000 (15 minutes in ms)
JWT_REFRESH_EXPIRATION: 86400000 (24 hours in ms)

# Database
SPRING_DATA_MONGODB_URI: mongodb://user:password@host:27017/db

# Redis
SPRING_REDIS_HOST: localhost
SPRING_REDIS_PORT: 6379

# CORS
APP_CORS_ALLOWED_ORIGINS: http://localhost:3000,http://localhost:8080
```

## Development Roadmap

### Phase 1: MVP ✅ (Current)
- [x] Auth Service
- [x] Application Management
- [x] User Authentication
- [x] JWT Token Generation
- [x] Database Setup

### Phase 2: Core Features (Next)
- [ ] Room Service
- [ ] MediaSoup Integration
- [ ] Basic Audio/Video
- [ ] Chat System
- [ ] Usage Tracking

### Phase 3: Advanced Features
- [ ] 10-Speaker Audio System
- [ ] Screen Sharing
- [ ] Whiteboard
- [ ] Admin Dashboard (React)
- [ ] Billing System

### Phase 4: Scale & Polish
- [ ] Load Testing
- [ ] Performance Optimization
- [ ] Monitoring & Analytics
- [ ] SDKs (Flutter, Web, Mobile)

## Testing

### Run Tests
```bash
cd auth-service
mvn test
```

### Load Testing
```bash
# Using JMeter or similar tools
# See documentation for details
```

## Troubleshooting

### MongoDB Connection Failed
```bash
# Check MongoDB service
docker-compose logs mongodb

# Restart MongoDB
docker-compose restart mongodb
```

### Port Already in Use
```bash
# Change ports in docker-compose.yml
# Or kill existing processes:
lsof -i :8001  # Find process on port 8001
kill -9 <PID>   # Kill the process
```

## Security

- ✅ Passwords hashed with BCrypt (12 rounds)
- ✅ JWT tokens with HS512 signature
- ✅ CORS enabled for specific origins
- ✅ HTTPS/TLS recommended for production
- ✅ Environment variables for secrets

## Performance

- ✅ MongoDB indexes on frequently queried fields
- ✅ Redis caching for session state
- ✅ JWT stateless authentication
- ✅ Async operations for I/O

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

MIT License - see LICENSE file for details

## Contact

- GitHub: [@figomigo123](https://github.com/figomigo123)
- Email: contact@example.com

## Support

For detailed architecture documentation, see [SYSTEM_PROMPT.md](./SYSTEM_PROMPT.md)
