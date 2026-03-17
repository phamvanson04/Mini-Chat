# Mini-Chat

A realtime chat backend application built with Spring Boot, supporting:
- JWT Authentication (register, login, logout)
- Public and room-based chat via WebSocket/STOMP
- Online/offline presence tracking with heartbeat mechanism
- Multi-source data storage: MySQL (auth), MongoDB (chat rooms/messages), Redis (presence cache)
- Fast deployment with Docker Compose

## 1) Technology Stack

### Backend & Framework
- ☕ **Java 21** — Latest JDK with virtual threads, records, pattern matching
- 🌱 **Spring Boot 3.2.x** — Standard web framework, auto-config, embedded server
- 🔐 **Spring Security + JWT** — Authentication, authorization, token-based auth (stateless)
- 🔌 **Spring WebSocket (STOMP + SockJS)** — Realtime 2-way communication with fallback for older browsers

### Data Layer
- 🗄️ **Spring Data JPA + Hibernate** — ORM, manage MySQL entities (User, Auth)
- 🍃 **Spring Data MongoDB** — NoSQL, store documents (Chat messages + Rooms)
- 🔴 **Spring Data Redis** — In-memory cache, fast presence tracking
- 🐬 **MySQL 8.4** — Relational DB, auth + user data
- 🍀 **MongoDB 7** — Document DB, chat history + room metadata
- ⚡ **Redis 7** — Cache + pub/sub, user online status

### DevOps & Container
- 🐳 **Docker** — Containerize app + dependencies
- 🎭 **Docker Compose** — Orchestrate multiple services (app, MySQL, MongoDB, Redis)
- 📦 **Maven 3.9** — Build tool, dependency management

### Libraries & Tooling
- 🪶 **Lombok** — Reduce boilerplate (auto-generate getters, setters, constructors)
- 🔑 **JJWT 0.12.6** — JWT library for signing/verifying tokens
- 🧪 **JUnit 5 + Spring Boot Test** — Unit & integration testing

## 2) Architecture Overview

```
com.minichat/
├── auth/                    🔐 Authentication & Authorization
│   ├── controller/          REST: /api/auth/register, login, logout
│   ├── dto/                 Request/Response payloads
│   ├── model/               AppUser, Role entities
│   ├── repository/          JPA repository for users
│   ├── security/            JWT filter, service, custom UserDetailsService
│   └── service/             Auth business logic
│
├── chat/                    💬 Chat & Presence
│   ├── controller/          REST: /api/rooms, WebSocket: /app/...
│   ├── dto/                 CreateRoomRequest, JoinRoomRequest, etc
│   ├── event/               UserPresenceEvent (publish/subscribe)
│   ├── listener/            Event listeners, WebSocket listeners
│   ├── model/               ChatMessage, ChatRoom, UserPresence, BaseEntity
│   ├── repository/          MongoDB, MySQL, Redis repositories
│   └── service/             ChatService, RoomService, PresenceService
│
├── config/                  ⚙️ Configuration
│   ├── auth/                SecurityConfig (JWT filter chain)
│   └── websocket/           WebSocketConfig (STOMP endpoint, broker)
│
└── shared/                  🔧 Shared Utilities
    ├── error/               Exception handler, custom exceptions
    └── response/            BaseResponse, ResponseFactory
```

## 3) Core Features

### 🔐 JWT Authentication
- `POST /api/auth/register` — Create new account, hash password, save to MySQL
- `POST /api/auth/login` — Authenticate user, issue JWT token (24h TTL)
- `POST /api/auth/logout` — Mark user offline, clear presence cache

### 🏠 Room Management
- `POST /api/rooms` — Create chat room, persist metadata to MongoDB
- `POST /api/rooms/{roomId}/join` — User joins room, add to membership
- `GET /api/rooms` — List all rooms
- `GET /api/rooms/{roomId}` — Get room details (name, members, created_at)

### 💬 Public Chat (Broadcast)
- **WebSocket endpoint**: `/ws` (STOMP over SockJS)
- **Send message**: `POST /app/chat.sendMessage` → broadcast to `/topic/public`
- **Join announcement**: `POST /app/chat.addUser` → announce join to `/topic/public`
- All connected users receive messages instantly

### 🎯 Room Chat (Scoped)
- **Send to room**: `POST /app/room/{roomId}/send` → broadcast to `/topic/rooms/{roomId}`
- **Join room**: `POST /app/room/{roomId}/join` → announce join within room topic
- Messages persist in MongoDB
- Only users in the room receive messages (not global broadcast)

### 👥 Presence & Heartbeat
- **User online tracking**: Store presence record (username, timestamp) in Redis
- **Heartbeat**: Client sends `POST /app/presence.heartbeat` periodically (every 30s)
- **Auto offline**: If no heartbeat for >30s → mark offline, trigger event
- **Event listener**: Spring event listener publishes UserPresenceEvent, notifies web clients

## 4) Run Locally Without Docker

### ✅ Requirements
- ☕ **Java 21 JDK**
- 📦 **Maven 3.9+**
- 🐬 **MySQL 8.x** running locally
- 🍀 **MongoDB 7.x** running locally
- ⚡ **Redis 7.x** running locally

### ⚙️ Configuration
Default settings in `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mini_chat_auth
    username: root
    password: root
  data:
    mongodb:
      uri: mongodb://localhost:27017/mini_chat
    redis:
      host: localhost
      port: 6379
```

### 🚀 Run
```bash
mvn clean spring-boot:run
```
App runs by default at: **http://localhost:8080**

## 5) Run with Docker Compose

### 🐳 Start Services
```bash
docker compose up --build -d
```

### 📌 Service Endpoints
| Service     | Port        | Note                    |
|-------------|------------|------------------------|
| **App**     | 8080:8080  | Spring Boot app        |
| **MySQL**   | 3307:3306  | Host:Container port    |
| **MongoDB**  | 27017:27017| Document DB            |
| **Redis**   | 6379:6379  | Cache + Presence       |

### 📊 View Logs
```bash
# Follow app logs
docker compose logs -f app

# View all logs
docker compose logs -f
```

### 🛑 Stop Services
```bash
# Stop all services
docker compose down

# Stop & remove volumes
docker compose down -v
```

## 6) Test Realtime Chat & WebSocket

### 🔗 WebSocket Connection
- **Endpoint**: `ws://localhost:8080/ws` (STOMP over SockJS)
- **Tools**: Postman, Socket.io Client, web browser + stomp.js library

### 📮 Common Message Flows

#### 1️⃣ Public Chat
```
CLIENT SUBSCRIBE: /topic/public

CLIENT SEND:
  destination: /app/chat.sendMessage
  body: {
    "sender": "alice",
    "content": "Hello everyone!"
  }

SERVER BROADCAST: /topic/public
  {
    "sender": "alice",
    "content": "Hello everyone!",
    "timestamp": "2025-03-17T10:30:00Z"
  }
```

#### 2️⃣ Room Chat
```
CLIENT SUBSCRIBE: /topic/rooms/room-123

CLIENT SEND:
  destination: /app/room/room-123/send
  body: {
    "sender": "bob",
    "content": "Room message"
  }

SERVER BROADCAST: /topic/rooms/room-123
  {
    "sender": "bob",
    "roomId": "room-123",
    "content": "Room message",
    "timestamp": "2025-03-17T10:31:00Z"
  }
```

#### 3️⃣ Presence Heartbeat
```
CLIENT SEND (every 30s):
  destination: /app/presence.heartbeat
  body: { "username": "alice" }

SERVER:
  - Update Redis presence: alice → timestamp
  - Trigger UserPresenceEvent
  - Auto clear offline after 30s timeout
```

## 7) Directory Structure

```
📁 mini-chat
├── 📄 pom.xml                    Maven build configuration
├── 📄 Dockerfile                 Multi-stage build (Maven → JRE slim)
├── 📄 docker-compose.yml         Orchestrate 4 services
├── 📄 README.md                  This file
├── 📄 .gitignore                 Ignore build, IDE, env files
│
└── 📁 src/main/java/com/minichat
    ├── 📄 MiniChatApplication.java    Spring Boot entry point
    │
    ├── 📁 auth/                  🔐 Authentication
    │   ├── controller/
    │   ├── dto/
    │   ├── model/
    │   ├── repository/
    │   ├── security/
    │   └── service/
    │
    ├── 📁 chat/                  💬 Chat & Presence
    │   ├── controller/
    │   ├── dto/
    │   ├── event/
    │   ├── listener/
    │   ├── model/
    │   ├── repository/
    │   └── service/
    │
    ├── 📁 config/                ⚙️ Configuration
    │   ├── auth/
    │   └── websocket/
    │
    └── 📁 shared/                🔧 Shared Utils
        ├── error/
        └── response/
```

## 8) 🔒 Security Notes

⚠️ **WARNING**:
- ❌ Never hardcode JWT secret, DB password in `application.yml`
- ✅ Use **environment variables** for production:
  ```bash
  export APP_JWT_SECRET=your-long-secret-key
  export SPRING_DATASOURCE_PASSWORD=your-db-password
  export SPRING_DATA_REDIS_PASSWORD=your-redis-password
  ```
- ❌ Never commit `.env` file (add to `.gitignore`)
- ✅ Use Docker secrets or Kubernetes ConfigMap for production deployments

### JWT Token Configuration
```yaml
app:
  jwt:
    secret: 12345678901234567890123456789012  # Min 32 chars
    expiration-ms: 86400000  # 24 hours
```

## 9) 🚀 Future Enhancements (Roadmap)

### 💾 Data & History
- [ ] Persistent chat history + pagination (MongoDB query with limits)
- [ ] Search messages by keyword/date range
- [ ] Archive old messages

### 📬 Advanced Chat Features
- [ ] **Direct messaging** (1-on-1 private chat)
- [ ] **Group chat** (multiple members per room)
- [ ] **Message edit/delete** (soft delete + version tracking)
- [ ] **Read receipts** (message seen status per user)
- [ ] **Typing indicators** (broadcast when user is typing)

### 👤 Presence & User Status
- [ ] User **custom status** (away, do not disturb, online)
- [ ] **Per-room presence** (detailed tracking of who's in which room)
- [ ] Last seen timestamp (when user last logged in)

### 🔔 Notifications & Alerts
- [ ] Push notifications (Firebase Cloud Messaging)
- [ ] Email alerts for missed messages
- [ ] In-app notification center

### 🎯 Moderation & Control
- [ ] **Rate limiting** (max messages per user per minute)
- [ ] **Mute/block users**
- [ ] **Room admin controls** (ban, kick, permission management)
- [ ] **Message audit log** (track edit/delete history)

### 📊 Analytics & Monitoring
- [ ] User activity statistics
- [ ] Message volume metrics
- [ ] Performance monitoring (WebSocket connection count)
- [ ] Error tracking integration (Sentry/DataDog)
