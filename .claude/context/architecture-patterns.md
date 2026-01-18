# Architecture Patterns (Microservices / Game Server)

## 1. Layered Architecture
- **Controller/Handler**: Handles network requests (Proto -> DTO), basic validation.
- **Service**: Implements core business logic. Stateful or Stateless.
- **Repository/DAO**: Data access layer. No business logic here.
- **Model/Domain**: Internal entities (POJOs). Not Proto objects.

## 2. Communication
- **Internal**: RPC (gRPC / Dubbo) or In-process calls.
- **External**: TCP/WebSocket (Netty) for game clients.
- **Message Queue**: For asynchronous events (logging, metrics, non-critical updates).

## 3. Game State Management
- **Stateless Services**: Preferred for logic like Login, Ranking (backed by Redis).
- **Stateful Services**: For Gameplay (Room/Match). Use Actor model or synchronized locking.
- **Concurrency**:
  - Avoid `synchronized` on service methods if possible.
  - Use Optimistic Locking (CAS) for DB updates.
  - Use Redis distributed locks for critical global resources.

## 4. Design Principles (DDD)
- **Bounded Context**: Keep `Login` logic separate from `Matchmaking`.
- **Anti-Corruption Layer**: Translate external protos to internal domain objects immediately.
