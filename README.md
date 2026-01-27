# Signal (Discord Clone Project)

Signal은 대규모 트래픽 처리를 고려하여 설계된 Discord 클론 프로젝트입니다.
MSA 전환을 염두에 둔 Spring Boot 멀티 모듈 아키텍처와 RabbitMQ를 활용한 분산 채팅 시스템을 특징으로 합니다.

## 🚀 Key Features & Architecture

### 1. RabbitMQ 기반 분산 채팅 시스템
- **Event-Driven Architecture**: 채팅 메시지 전송과 DB 저장을 분리하여 실시간 처리 성능을 극대화했습니다.
    - **Producer**: `signal-chat` 서버가 메시지를 받아 RabbitMQ Exchange로 발행.
    - **Consumer**: `DbConsumer`가 큐에서 비동기로 메시지를 소비하여 PostgreSQL에 저장.
- **STOMP & WebSocket**: 웹소켓을 통한 실시간 양방향 통신 지원.

### 2. 데이터 정합성 보장 (Redis)
- **Global Sequence ID**: Redis의 `INCR` 연산을 활용하여 채팅방별 고유한 시퀀스 ID(`seqId`)를 발급.
- 분산 환경에서도 메시지 순서를 보장하며, 클라이언트가 메시지 누락 여부를 판단할 수 있도록 설계했습니다.

### 3. 멀티 모듈 아키텍처 (Multi-Module)
- **signal-api**: REST API 서버 (회원가입, 로그인, 서버/채널/카테고리 관리 등 비즈니스 로직).
- **signal-chat**: 채팅 전용 소켓 서버 (Netty 기반, 가벼운 연결 유지 및 메시지 라우팅).
- **signal-common**: 도메인 엔티티, DTO, 공통 유틸리티 공유.

### 4. 서버(길드) 관리 기능
- Discord 스타일의 서버/카테고리/채널 계층 구조 구현.
- 초대 코드를 통한 서버 가입 시스템.

---

## 🛠 Tech Stack

### Backend
- **Framework**: Spring Boot 3.4
- **Language**: Java 17
- **Database**: PostgreSQL
- **Message Broker**: RabbitMQ (Using STOMP Plugin)
- **Cache & Properies**: Redis
- **ORM**: Spring Data JPA, QueryDSL
- **Build Tool**: Gradle (Multi-module)

### Frontend
- **Framework**: React, Vite
- **Styling**: Vanilla CSS (Premium & Dark Mode Design)

### Tools & DevOps
- **Docker & Docker Compose**: 로컬 개발 환경(DB, RabbitMQ, Redis) 통합 관리.
- **Stress Test**: Python (`asyncio`, `websockets`) 기반 10,000건 동시성 테스트 스크립트 보유.

---

## ⚙️ Getting Started

### 1. Prerequisite (Infrastructure)
Docker Compose를 사용하여 필수 인프라(PostgreSQL, Redis, RabbitMQ)를 실행합니다.
```bash
./gradlew bootRun
