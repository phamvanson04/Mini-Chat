# Mini-Chat

Mini-Chat la backend chat realtime xay dung bang Spring Boot, ho tro:
- Xac thuc JWT (register, login, logout)
- Chat public va chat theo room qua WebSocket/STOMP
- Theo doi trang thai online/offline (presence + heartbeat)
- Luu tru da nguon: MySQL (auth), MongoDB (chat room/message), Redis (presence cache)
- Chay nhanh bang Docker Compose

## 1) Cong nghe su dung
- Java 21
- Spring Boot 3.2.x
- Spring Security + JWT
- Spring WebSocket (STOMP + SockJS)
- Spring Data JPA (MySQL)
- Spring Data MongoDB
- Spring Data Redis
- Docker + Docker Compose

## 2) Kien truc tong quan
- auth:
  - Quan ly dang ky/dang nhap/dang xuat
  - Phat hanh va xac minh JWT
- chat:
  - Xu ly tin nhan public va room-based
  - Quan ly room (tao/join/list/detail)
  - Presence heartbeat, online/offline event
- config:
  - Cau hinh security filter chain
  - Cau hinh WebSocket endpoint va broker
- shared:
  - Base response va xu ly loi dung chung

## 3) Chuc nang chinh
1. JWT Authentication
- POST /api/auth/register
- POST /api/auth/login
- POST /api/auth/logout

2. Quan ly Room
- POST /api/rooms (tao room)
- POST /api/rooms/{roomId}/join
- GET /api/rooms
- GET /api/rooms/{roomId}

3. Public Chat qua WebSocket
- Client gui den: /app/chat.sendMessage
- Client gui den: /app/chat.addUser
- Server broadcast: /topic/public

4. Room Chat qua WebSocket
- Client gui den: /app/room/{roomId}/send
- Client gui den: /app/room/{roomId}/join
- Server broadcast: /topic/rooms/{roomId}

5. Presence/Heartbeat
- Client gui heartbeat den: /app/presence.heartbeat
- Presence duoc cap nhat qua Redis + event listener

## 4) Chay local khong Docker
### Yeu cau
- Java 21
- Maven 3.9+
- MySQL, MongoDB, Redis dang chay local

### Cau hinh
Mac dinh trong src/main/resources/application.yml:
- MySQL: localhost:3306 (db: mini_chat_auth)
- MongoDB: localhost:27017 (db: mini_chat)
- Redis: localhost:6379

### Run
```bash
mvn clean spring-boot:run
```
App mac dinh chay tai http://localhost:8080

## 5) Chay bang Docker Compose
```bash
docker compose up --build -d
```

Service duoc khoi tao:
- app: http://localhost:8080
- mysql host port: 3307 (container 3306)
- mongodb: 27017
- redis: 6379

Dung app:
```bash
docker compose logs -f app
```

Dung toan bo:
```bash
docker compose down
```

## 6) Test nhanh WebSocket
- Endpoint ket noi SockJS: /ws
- Neu dung Postman hoac web client STOMP:
  - subscribe /topic/public hoac /topic/rooms/{roomId}
  - send message vao /app/... tuong ung

## 7) Cau truc thu muc
```
src/main/java/com/minichat
  auth/
  chat/
  config/
  shared/
```

## 8) Ghi chu bao mat
- Khong nen de thong tin nhay cam hard-code trong application.yml.
- Nen dua cac gia tri nhu JWT secret, DB password vao bien moi truong.

## 9) Huong mo rong
- Luu full chat history va paging
- Read receipt / delivery status
- Private direct message
- Presence theo room chi tiet hon
- Rate limiting cho message endpoint
