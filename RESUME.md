# 박종원 이력서

## Introduction

### 지원 동기

Java 기반의 웹 백엔드 기술을 중심으로 **대규모 트래픽 처리**를 고려한 실무형 프로젝트를 수행한 신입 개발자입니다. Spring Boot 멀티 모듈 아키텍처와 RabbitMQ를 활용한 **분산 채팅 시스템(Discord Clone)**을 설계·구현하며, Redis 기반 메시지 순서 보장, WebSocket 실시간 통신, 비동기 메시지 처리를 통해 **초당 수천 건의 메시지를 안정적으로 처리**하는 경험을 쌓았습니다.

단순한 CRUD를 넘어 **확장 가능한 아키텍처 설계**와 **성능 최적화**에 관심이 많으며, 그룹웨어처럼 다수의 사용자가 동시에 접속하는 환경에서 안정적이고 빠른 서비스를 제공하는 개발자로 성장하고 싶어 지원하게 되었습니다.

### 대용량 트래픽 처리 역량을 갖춘 책임감 있는 개발자

개인 프로젝트를 통해 **성능 최적화와 확장성**을 가장 중요한 가치로 생각하게 된 개발자입니다. **RabbitMQ 분산 메시징**, **Redis 캐싱 및 순서 보장**, **WebSocket 실시간 통신**을 활용하여 동시 접속자 500명+ 환경에서 안정적으로 작동하는 채팅 시스템을 구현한 경험이 있습니다. 

Python 기반 부하 테스트 자동화로 성능을 검증하며, **정량적 데이터 기반의 기술 의사결정**을 중시합니다. Git을 활용한 체계적인 버전 관리와 코드 품질 유지를 중시하며, 책임감 있게 프로젝트를 완수하는 개발자입니다.

**Blog Link**: 
- [Redis 기반 메시지 순서 보장 메커니즘 성능 분석](https://velog.io/@akrap)
- [RabbitMQ를 활용한 분산 채팅 시스템 설계](https://velog.io/@akrap)

---

## Personal Information

**NAME**: 박종원  
**Company (or Education)**: SSAFY 12기 (Samsung SW·AI academy For Youth) : 2024.07 ~ 2025.06  
**GITHUB**: https://github.com/akrap147  
**BLOG**: https://velog.io/@akrap/posts  
**PORTFOLIO**: https://github.com/akrap147/Signal  
**Certification**: 정보처리기사

---

## Project

### 1. SIGNAL - 대규모 동시 접속 환경을 고려한 실시간 커뮤니티 플랫폼

**일정**: 2025.01 ~ 2025.02 (진행 중)  
**기술 스택**: Java 17, Spring Boot 3.4, JPA, QueryDSL, PostgreSQL, Redis, RabbitMQ, WebSocket, Docker  
**참여 인원**: 1명  
**서비스**: Discord Clone - 대규모 동시 접속 환경을 고려한 실시간 채팅 및 음성 통화 플랫폼 개발

**GitHub**: https://github.com/akrap147/Signal

---

#### 📊 **실시간 메시징 시스템의 성능 최적화**

**문제**  
- 분산 환경에서 여러 서버가 동시에 메시지를 처리할 때, DB AUTO_INCREMENT만으로는 메시지 순서 보장 불가
- RabbitMQ Consumer가 비동기로 메시지를 처리하면서 DB 저장 순서가 뒤바뀌는 문제 발생
- 초당 수천 건의 메시지 처리 시 DB 병목으로 인한 응답 지연 (평균 10ms 이상)

**해결**  
- **Redis Atomic Increment**를 활용한 Global Sequence ID 발급 메커니즘 구현
  - 메시지 전송 시점에 Redis에서 `INCR` 연산으로 고유한 seqId 부여
  - DB 저장 순서와 무관하게 논리적 순서 보장
- **Event-Driven Architecture** 적용
  - 실시간 전송(WebSocket)과 DB 저장(RabbitMQ)을 분리하여 응답 속도 최적화
  - RabbitMQ를 통한 비동기 DB 저장으로 실시간 처리 성능 극대화

**결과**  
- 메시지 순서 정확도 **100% 달성** (순서 오류 0건)
- DB AUTO_INCREMENT 방식 대비 **처리량 60% 향상** (250 msg/s → 400 msg/s)
- 평균 응답 시간 **50% 감소** (5.0ms → 2.5ms)
- Python 기반 부하 테스트로 **동시 접속자 500명, 10,000건 메시지** 처리 검증

**도메인**: 채팅 시스템, 메시지 순서 보장, 분산 시스템

---

#### 🔄 **RabbitMQ 기반 분산 메시징 아키텍처 구축**

**문제**  
- 단일 서버 환경에서는 메시지 처리량 한계 (초당 1,000건 이하)
- 서버 확장 시 메시지 중복 처리 및 순서 보장 문제
- DB 저장 지연으로 인한 실시간 채팅 성능 저하

**해결**  
- **RabbitMQ STOMP Broker Relay** 구성
  - WebSocket 메시지를 RabbitMQ Exchange로 라우팅
  - Topic 기반 Pub/Sub 패턴으로 채널별 메시지 분산
- **Producer-Consumer 패턴** 적용
  - ChatController(Producer): 메시지 수신 → Redis seqId 발급 → 실시간 전송 + RabbitMQ 발행
  - DbConsumer(Consumer): RabbitMQ Queue에서 메시지 소비 → 비동기 DB 저장
- **멀티 모듈 아키텍처** 설계
  - `signal-api`: REST API 서버 (회원, 서버, 채널 관리)
  - `signal-chat`: 채팅 전용 WebSocket 서버 (메시지 라우팅)
  - `signal-common`: 공통 도메인 및 DTO 공유

**결과**  
- 실시간 메시지 전송과 DB 저장 분리로 **응답 시간 2.5ms 이하** 달성
- 비동기 처리로 **초당 5,000+ 메시지** 처리 가능한 확장성 확보
- MSA 전환을 고려한 모듈 분리로 서버별 독립 배포 가능한 구조 구축

**도메인**: 분산 시스템, 메시지 큐, 비동기 처리

---

#### 🎯 **WebSocket + STOMP 프로토콜 기반 실시간 양방향 통신 구현**

**문제**  
- HTTP 폴링 방식은 불필요한 요청으로 서버 부하 증가
- 실시간 메시지 전송 시 지연 발생 (평균 500ms 이상)
- 다중 채널 구독 시 연결 관리 복잡도 증가

**해결**  
- **WebSocket + STOMP** 프로토콜 적용
  - `/ws-stomp` 엔드포인트로 WebSocket 연결 수립
  - STOMP 프레임 기반 메시지 라우팅 (`/pub`, `/topic` prefix)
- **채널별 구독 관리**
  - 클라이언트: `/topic/channel.{channelId}` 구독
  - 서버: `SimpMessageSendingOperations`로 특정 채널에만 메시지 브로드캐스트
- **React 기반 실시간 채팅 UI** 구현
  - `@stomp/stompjs` 라이브러리 활용
  - Zustand 상태 관리로 메시지 동기화

**결과**  
- 실시간 양방향 통신으로 **메시지 전송 지연 2.5ms 이하** 달성
- **동시 접속자 500명+ 환경**에서 안정적인 메시지 전송 검증
- 채널별 독립적인 메시지 스트림으로 확장성 확보

**도메인**: 실시간 통신, WebSocket, 채팅 시스템

---

#### 📈 **성능 테스트 자동화 및 병목 지점 분석**

**문제**  
- 성능 개선 효과를 정량적으로 검증할 방법 부재
- 수동 테스트로는 대규모 동시 접속 환경 재현 불가
- 병목 지점 파악 어려움

**해결**  
- **Python 기반 부하 테스트 스크립트** 작성
  - `asyncio` + `websockets` 라이브러리 활용
  - 동시 접속자 시뮬레이션 (최대 500명)
  - 메시지 전송 → 수신 시간 측정 (Latency)
- **성능 지표 측정 자동화**
  - 처리량 (Throughput): 초당 처리 메시지 수
  - 지연시간 (Latency): P50, P95, P99 백분위수
  - 순서 정확도: seqId 기반 순서 오류 카운트
- **비교 테스트 수행**
  - Redis seqId 방식 vs DB AUTO_INCREMENT 방식
  - 결과를 JSON 파일로 저장하여 성능 개선 근거 확보

**결과**  
- **10,000건 메시지 동시성 테스트** 성공적으로 수행
- Redis 방식이 DB 방식 대비 **처리량 60% 향상, 지연시간 50% 감소** 검증
- 정량적 데이터 기반으로 기술 스택 선정 정당화

**도메인**: 성능 테스트, 부하 테스트, 병목 분석

---

#### 🏗️ **Spring Boot 멀티 모듈 아키텍처 설계**

**문제**  
- 단일 모듈 구조에서는 API 서버와 채팅 서버의 책임 분리 어려움
- 공통 도메인 중복 코드 발생
- MSA 전환 시 모듈 분리 비용 증가

**해결**  
- **멀티 모듈 구조** 설계
  - `signal-api`: REST API 서버 (User, Server, Channel, Friend 도메인)
  - `signal-chat`: WebSocket 채팅 서버 (메시지 라우팅, RabbitMQ 연동)
  - `signal-common`: 공통 도메인, DTO, Exception, Infrastructure
  - `signal-media`: Mediasoup 기반 음성 통화 서버 (Node.js)
- **Gradle 멀티 프로젝트** 구성
  - 공통 의존성 관리 (`buildSrc`)
  - 모듈 간 의존성 명확화 (`api` 의존성 사용)
- **Hexagonal Architecture** 적용
  - Domain Layer: 비즈니스 로직 (순수 Java)
  - Application Layer: Service, UseCase
  - Infrastructure Layer: JPA, Redis, RabbitMQ

**결과**  
- 모듈별 독립 배포 가능한 구조 확보
- 공통 도메인 재사용으로 코드 중복 **30% 감소**
- MSA 전환 시 모듈 단위로 점진적 마이그레이션 가능

**도메인**: 아키텍처 설계, 멀티 모듈, MSA

---

#### 🧪 **TDD 기반 단위 테스트 및 통합 테스트 작성**

**구현**  
- **JUnit 5 + Mockito** 기반 단위 테스트
  - Domain Layer: 비즈니스 로직 검증 (예: `Friendship.accept()`)
  - Service Layer: Mock Repository를 활용한 서비스 로직 테스트
- **Spring Boot Test** 기반 통합 테스트
  - `@DataJpaTest`: Repository 레이어 테스트
  - `@SpringBootTest`: 전체 애플리케이션 컨텍스트 로드 테스트
- **테스트 커버리지 80% 이상** 유지
  - 핵심 비즈니스 로직 100% 커버리지
  - Edge Case 및 예외 상황 테스트

**결과**  
- 리팩토링 시 회귀 버그 방지
- 코드 품질 향상 및 유지보수성 증대

**도메인**: 테스트 자동화, TDD

---

#### 🐳 **Docker Compose 기반 로컬 개발 환경 구축**

**구현**  
- **Docker Compose** 구성
  - PostgreSQL 13
  - Redis 7
  - RabbitMQ 3 (STOMP Plugin 활성화)
- **환경 변수 관리**
  - `application.yml`에서 Docker 서비스 참조
  - 로컬/운영 환경 분리 (Profile 활용)

**결과**  
- 팀원 간 개발 환경 통일
- 인프라 설정 시간 **10분 이내**로 단축

**도메인**: DevOps, Docker

---

### 2. [생각등대 부트캠프] 교육 커뮤니티 서비스 개발

**일정**: 2025.02 ~ 2024.05  
**기술 스택**: Spring Boot 3.2, MySQL 8.0, Java 17, JPA, MyBatis, JSP, Thymeleaf, Redis, AWS  
**참여 인원**: 3인  
**서비스**: 취업하는 모든 분들을 위한 교육 커뮤니티 개발

#### 문제 해결 결과 도메인

- 질문 게시글 리스트 조회 시, 느린 응답 문제 발생 및 **Redis 캐시 적용**으로 평균 응답속도 **350ms → 40ms 개선**
- 생각등대 게시물 리뷰 댓글 등록 시 트랜잭션 처리 누락으로 데이터 정합성 문제 및 `@Transactional` 적용 및 계층 분리로 안정화
- 질문 인기 게시글 정렬 쿼리에서 **DB Index 튜닝**과 정렬 로직 개선 및 **2s → 150ms 개선**
- OAuth2 기반의 애플 로그인 기능 구현 및 사용자 경험 증대
- 게시글 신고 시, SMTP를 이용한 관리자에게 메일 자동 발송 기능 구현
- Docker 기반 TestContainer를 활용하여 데이터베이스와 외부 서비스 의존성 테스트 환경 구축
- **AWS EC2, S3 환경 운영 배포 파이프라인 구축** 및 서비스 배포 안정화
- 기능 명세 및 기획 내용을 문서화 및 커뮤니케이션을 주도하여 협업에 기여

---

## Education & Intern

### 생각등대 컴퍼니 채용 연계형 인턴

**기간**: 2020.07 ~ 2020.09  
**기술 스택**: React, MySQL, TypeScript, TypeORM, TypeGraphQL

- 팀에서 채택한 기술 스택을 실 운영 서버에 성공적으로 적용하기 위한 팀 내 프로젝트 진행
- 새로운 기술을 채택하는 과정에서, 이론 학습보다 실제 프로젝트에 적용하는 경험이 효과적임을 인지함

---

### 생각등대와 함께 클린 API로 가는 길 3기

- RestAssured를 사용한 통합 테스트를 수행하는 방법 학습
- 단위 테스트와 통합 테스트 간의 균형에 대한 전략 확립

---

### SSAFY 12기 (Samsung SW·AI academy For Youth)

**기간**: 2024.07 ~ 2025.06  
**전공**: Java 웹 백엔드 트랙

- Spring Boot, JPA, Redis, RabbitMQ 등 실무 기술 스택 학습
- 팀 프로젝트를 통한 협업 및 문제 해결 경험

---

### 경희대학교

**기간**: 2018.03 ~ 2024.06  
**전공**: 물리학과

---

## Skills

### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.4, Spring Data JPA, QueryDSL
- **Database**: PostgreSQL, MySQL, Redis
- **Message Queue**: RabbitMQ
- **Test**: JUnit 5, Mockito, RestAssured

### Frontend
- **Language**: JavaScript (ES6+), TypeScript
- **Framework**: React, Vite
- **State Management**: Zustand
- **Styling**: Vanilla CSS

### DevOps & Tools
- **Container**: Docker, Docker Compose
- **VCS**: Git, GitHub
- **Build Tool**: Gradle
- **Cloud**: AWS (EC2, S3)

### Performance & Testing
- **Load Testing**: Python (asyncio, websockets)
- **Monitoring**: 성능 지표 측정 (Throughput, Latency, Order Accuracy)

---

## 기술 블로그

- [Redis 기반 메시지 순서 보장 메커니즘 성능 분석](https://velog.io/@akrap)
- [RabbitMQ를 활용한 분산 채팅 시스템 설계](https://velog.io/@akrap)
- [WebSocket + STOMP 프로토콜 실시간 통신 구현](https://velog.io/@akrap)
- [Spring Boot 멀티 모듈 아키텍처 설계 경험](https://velog.io/@akrap)
