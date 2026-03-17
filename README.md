# Mini-Chat

Mini-Chat la backend chat realtime xay dung bang Spring Boot, ho tro:
- Xac thuc JWT (register, login, logout)
- Chat public va chat theo room qua WebSocket/STOMP
- Theo doi trang thai online/offline (presence + heartbeat)
- Luu tru da nguon: MySQL (auth), MongoDB (chat room/message), Redis (presence cache)
- Chay nhanh bang Docker Compose

## 1) Công Nghệ Sử Dụng

### Backend & Framework
- ☕ **Java 21** — JDK mới nhất với feature virtual thread, record, pattern matching
- 🌱 **Spring Boot 3.2.x** — Framework web chuẩn, auto-config, embedded server
- 🔐 **Spring Security + JWT** — Xác thực, phân quyền, token-based auth không session
- 🔌 **Spring WebSocket (STOMP + SockJS)** — Realtime 2-way communication, fallback cho old browser

### Data Layer
- 🗄️ **Spring Data JPA + Hibernate** — ORM, manage MySQL entities (User, Auth)
- 🍃 **Spring Data MongoDB** — NoSQL stored documents (Chat message + Room)
- 🔴 **Spring Data Redis** — In-memory cache, fast presence tracking
- 🐬 **MySQL 8.4** — Relational DB, auth + user data
- 🍀 **MongoDB 7** — Document DB, chat history + room metadata
- ⚡ **Redis 7** — Cache + pub/sub, user online status

### DevOps & Container
- 🐳 **Docker** — Containerize app + dependencies
- 🎭 **Docker Compose** — Orche multiple services (app, MySQL, MongoDB, Redis) cục bộ
- 📦 **Maven 3.9** — Build tool, dependency management, multi-module support

### Libraries & Tooling
- 🪶 **Lombok** — Reduce boilerplate (getter/setter/constructor auto-gen)
- 🔑 **JJWT 0.12.6** — JWT library, sign/verify token
- 🧪 **JUnit 5 + Spring Boot Test** — Unit & integration test

## 2) Kiến Trúc Tổng Quan

```
com.minichat/
├── auth/                    🔐 Authentication & Authorization
│   ├── controller/          REST: /api/auth/register, login, logout
│   ├── dto/                 Request/Response payload
│   ├── model/               AppUser, Role entity
│   ├── repository/          JPA repository for users
│   ├── security/            JWT filter, service, custom UserDetailsService
│   └── service/             Auth business logic
│
├── chat/                    💬 Chat & Presence
│   ├── controller/          REST: /api/rooms, WebSocket: /app/...
│   ├── dto/                 CreateRoomRequest, JoinRoomRequest, etc
│   ├── event/               UserPresenceEvent (publish/subscribe)
│   ├── listener/            Event listener, WebSocket listener
│   ├── model/               ChatMessage, ChatRoom, UserPresence, BaseEntity
│   ├── repository/          MongoDB, MySQL, Redis repo
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

## 3) Chức Năng Chính

### 🔐 JWT Authentication
- `POST /api/auth/register` — Tạo tài khoản mới, hash password, lưu vào MySQL
- `POST /api/auth/login` — Xác thực user, phát JWT token (24h TTL)
- `POST /api/auth/logout` — Đánh dấu user offline, clear presence cache

### 🏠 Quản Lý Room
- `POST /api/rooms` — Tạo chat room, lưu metadata vào MongoDB
- `POST /api/rooms/{roomId}/join` — User join room, thêm vào membership
- `GET /api/rooms` — Danh sách tất cả room
- `GET /api/rooms/{roomId}` — Chi tiết room (tên, members, created_at)

### 💬 Public Chat (Broadcast)
- **WebSocket endpoint**: `/ws` (STOMP over SockJS)
- **Send message**: `POST /app/chat.sendMessage` → broadcast `/topic/public`
- **Join announcement**: `POST /app/chat.addUser` → broadcast `/topic/public`
- User thấy tin/thông báo tức thì

### 🎯 Room Chat (Scoped)
- **Send to room**: `POST /app/room/{roomId}/send` → broadcast `/topic/rooms/{roomId}`
- **Join room**: `POST /app/room/{roomId}/join` → announce join trong room topic
- Message persistent lưu MongoDB
- Chỉ user trong room thấy, không broadcast global

### 👥 Presence & Heartbeat
- **User online tracking**: Lưu presence record (username, timestamp) trên Redis
- **Heartbeat**: Client gửi `POST /app/presence.heartbeat` định kỳ (mỗi 30s)
- **Auto offline**: Nếu không heartbeat quá 30s → mark offline, trigger event
- **Event listener**: Spring event listener publish UserPresenceEvent, notify web client

## 4) Chạy Local Không Docker

### ✅ Yêu Cầu
- ☕ **Java 21 JDK**
- 📦 **Maven 3.9+**
- 🐬 **MySQL 8.x** chạy local
- 🍀 **MongoDB 7.x** chạy local
- ⚡ **Redis 7.x** chạy local

### ⚙️ Cấu Hình
Mặc định trong `src/main/resources/application.yml`:
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
App mặc định chạy tại: **http://localhost:8080**

## 5) Chạy Bằng Docker Compose

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

## 7) Cấu Trúc Thư Mục

```
📁 mini-chat
├── 📄 pom.xml                    Maven build config
├── 📄 Dockerfile                 Multi-stage build (Maven → JRE slim)
├── 📄 docker-compose.yml         Orche 4 services
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

## 8) 🔒 Ghi Chú Bảo Mật

⚠️ **Cảnh báo**:
- ❌ Không hard-code JWT secret, DB password trong `application.yml`
- ✅ Sử dụng **environment variables** cho production:
  ```bash
  export APP_JWT_SECRET=your-long-secret-key
  export SPRING_DATASOURCE_PASSWORD=your-db-password
  export SPRING_DATA_REDIS_PASSWORD=your-redis-password
  ```
- ❌ Không commit `.env` file (thêm vào `.gitignore`)
- ✅ Dùng Docker secrets or Kubernetes ConfigMap cho deploy production

### JWT Token Config
```yaml
app:
  jwt:
    secret: 12345678901234567890123456789012  # Min 32 chars
    expiration-ms: 86400000  # 24 hours
```

## 9) 🚀 Hướng Mở Rộng (Future Features)

### 💾 Data & History
- [ ] Persistent chat history + pagination (MongoDB query with limits)
- [ ] Search messages by keyword/date range
- [ ] Archive old messages

### 📬 Advanced Chat
- [ ] **Direct message** (1-on-1 private chat)
- [ ] **Group chat** (multiple members per room)
- [ ] **Message edit/delete** (soft delete + version tracking)
- [ ] **Read receipt** (message seen status per user)
- [ ] **typing indicator** (broadcast user is typing)

### 👤 Presence & Status
- [ ] User **custom status** (away, do not disturb, online)
- [ ] **Per-room presence** (detailed who's in which room)
- [ ] Last seen timestamp (when user last logged in)

### 🔔 Notifications
- [ ] Push notification (Firebase Cloud Messaging)
- [ ] Email alert for missed messages
- [ ] In-app notification center

### 🎯 Moderation & Control
- [ ] **Rate limiting** (max messages per user per minute)
- [ ] **Mute/block users**
- [ ] **Room admin** controls (ban, kick, permissions)
- [ ] **Message audit log** (track edit/delete history)

### 📊 Analytics & Monitoring
- [ ] User activity stats
- [ ] Message volume metrics
- [ ] Performance monitoring (WebSocket connection count)
- [ ] Error tracking (Sentry/DataDog)
