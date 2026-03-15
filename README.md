# Signal (Discord Clone Project)

Signal은 대규모 트래픽 처리를 고려하여 설계된 Discord 클론 프로젝트입니다.
MSA 전환을 염두에 둔 Spring Boot 멀티 모듈 아키텍처와 RabbitMQ를 활용한 이벤트 드리븐 채팅 시스템을 특징으로 합니다.

---

## 🚀 Key Features & Architecture

### 1. RabbitMQ 기반 분산 채팅 시스템
- **Event-Driven Architecture**: 채팅 메시지 전송과 DB 저장을 분리하여 실시간 처리 성능을 극대화했습니다.
  - **Producer**: `signal-chat` 서버가 메시지를 받아 RabbitMQ Exchange로 발행.
  - **Consumer**: `DbConsumer`가 큐에서 비동기로 메시지를 소비하여 PostgreSQL에 저장.
- **STOMP & WebSocket**: 채널 메시지 및 DM 모두 WebSocket + STOMP 기반으로 실시간 처리.

### 2. 데이터 정합성 보장 (Redis)
- **Global Sequence ID**: Redis의 `INCR` 연산을 활용하여 채팅방별 고유한 시퀀스 ID(`seqId`)를 발급.
- 분산 환경에서도 메시지 순서를 보장하며, 클라이언트가 메시지 누락 여부를 판단할 수 있도록 설계했습니다.
- 서버 초대 코드 저장 및 검증에도 Redis를 활용합니다.

### 3. 멀티 모듈 아키텍처 (Multi-Module)
| 모듈 | 역할 |
|------|------|
| `signal-api` | REST API 서버 - 인증, 서버/채널/카테고리/친구 관리 (8080) |
| `signal-chat` | 채팅 전용 WebSocket 서버 - 실시간 메시지 라우팅 (8081) |
| `signal-common` | 도메인 엔티티, DTO, Repository, 공통 유틸리티 공유 |
| `signal-media` | 미디어 파일 업로드 처리 |
| `signal-signaling` | 음성/영상 통화 시그널링 (Mediasoup SFU) |

### 4. 서버(길드) 관리
- Discord 스타일의 서버 → 카테고리 → 채널 계층 구조 구현.
- Redis 기반 초대 코드를 통한 서버 가입 시스템.

### 5. 친구 시스템
- 이메일로 친구 요청 발송 / 수락 / 거절 / 삭제.
- 친구 관계 상태 관리: `PENDING` → `ACCEPTED` / `BLOCKED`.

---

## 🛠 Tech Stack

### Backend
| 항목 | 기술 |
|------|------|
| Framework | Spring Boot 3.5 |
| Language | Java 17 |
| Database | PostgreSQL |
| Message Broker | RabbitMQ (STOMP Plugin) |
| Cache | Redis |
| ORM | Spring Data JPA, QueryDSL |
| Security | Spring Security + JWT |
| Build | Gradle (Multi-module) |
| Test | JUnit 5, Testcontainers |

### Frontend
| 항목 | 기술 |
|------|------|
| Framework | React 19, Vite |
| State Management | Zustand |
| Server State | TanStack Query (React Query) |
| HTTP Client | Axios |
| Styling | Tailwind CSS |
| Form | React Hook Form |

### DevOps & Tools
- **Docker & Docker Compose**: PostgreSQL, Redis, RabbitMQ 로컬 인프라 통합 관리.
- **Stress Test**: Python (`asyncio`, `websockets`) 기반 10,000건 동시성 테스트 스크립트 보유.

---

## ⚙️ Getting Started

### 1. 인프라 실행 (Docker Compose)

```bash
cd backend
docker-compose up -d
```

실행되는 서비스:
- PostgreSQL: `localhost:5432`
- RabbitMQ: `localhost:5672` (AMQP), `localhost:15672` (Management UI), `localhost:61613` (STOMP)
- Redis: `localhost:6379`

### 2. 백엔드 실행

```bash
# signal-api (REST API, 8080)
cd backend
./gradlew :signal-api:bootRun

# signal-chat (WebSocket, 8081)
./gradlew :signal-chat:bootRun
```

### 3. 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

---

## 📁 Project Structure

```
Signal/
├── backend/
│   ├── signal-api/        # REST API 서버
│   ├── signal-chat/       # WebSocket 채팅 서버
│   ├── signal-common/     # 공통 도메인 / DTO / Repository
│   ├── signal-media/      # 미디어 파일 처리
│   ├── signal-signaling/  # 음성/영상 시그널링
│   └── docker-compose.yml
└── frontend/
    └── src/
        ├── api/           # Axios API 클라이언트
        ├── components/    # React 컴포넌트
        ├── hooks/         # 커스텀 훅
        ├── pages/         # 페이지 컴포넌트
        └── stores/        # Zustand 스토어
```
