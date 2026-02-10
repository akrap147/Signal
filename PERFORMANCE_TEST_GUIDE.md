# 채팅 시스템 성능 테스트 가이드

## 🎯 목적
Redis seqId 방식과 DB AUTO_INCREMENT 방식의 성능을 비교하여 현재 구조의 우수성을 증명

## 📊 측정 지표

### 1. 처리량 (Throughput)
- **정의**: 초당 처리 가능한 메시지 수
- **목표**: Redis 방식이 더 높은 처리량 달성
- **측정**: `messages_received / total_duration`

### 2. 지연시간 (Latency)
- **정의**: 메시지 전송부터 수신까지 걸린 시간
- **목표**: Redis 방식이 더 낮은 지연시간
- **측정**: P50, P95, P99 백분위수

### 3. 순서 정확도 (Order Accuracy)
- **정의**: 메시지가 올바른 순서로 수신된 비율
- **목표**: Redis 방식이 100% 정확도
- **측정**: `(1 - order_errors / total_messages) * 100`

### 4. 시스템 리소스
- **CPU 사용률**
- **메모리 사용량**
- **네트워크 I/O**

---

## 🧪 테스트 시나리오

### 시나리오 1: 기본 부하 테스트
```bash
# 설정
- 동시 사용자: 50명
- 사용자당 메시지: 100개
- 총 메시지: 5,000개
```

### 시나리오 2: 고부하 테스트
```bash
# 설정
- 동시 사용자: 200명
- 사용자당 메시지: 500개
- 총 메시지: 100,000개
```

### 시나리오 3: 극한 부하 테스트
```bash
# 설정
- 동시 사용자: 500명
- 사용자당 메시지: 1,000개
- 총 메시지: 500,000개
```

---

## 🚀 실행 방법

### 1. 환경 준비

#### Redis 시작
```bash
# Docker로 Redis 실행
docker run -d --name redis-test -p 6379:6379 redis:7-alpine

# 또는 로컬 Redis
redis-server
```

#### RabbitMQ 시작
```bash
# Docker로 RabbitMQ 실행 (STOMP 플러그인 포함)
docker run -d --name rabbitmq-test \
  -p 5672:5672 \
  -p 15672:15672 \
  -p 61613:61613 \
  rabbitmq:3-management

# STOMP 플러그인 활성화
docker exec rabbitmq-test rabbitmq-plugins enable rabbitmq_stomp
```

#### 채팅 서버 시작
```bash
cd backend/signal-chat
./gradlew bootRun
```

---

### 2. 테스트 실행

#### 테스트 1: Redis seqId 방식 (현재 구조)
```bash
python performance_test.py
```

**결과 파일**: `performance_result_YYYYMMDD_HHMMSS.json`

#### 테스트 2: DB AUTO_INCREMENT 방식 (비교용)
```bash
# performance_test.py 수정 필요
# WS_URL 또는 destination을 /pub/chat/message-no-redis로 변경

python performance_test_no_redis.py
```

---

### 3. 결과 분석

#### 예상 결과 (Redis seqId 방식)
```json
{
  "results": {
    "throughput": 5000.0,        // msg/sec
    "avg_latency_ms": 2.5,       // ms
    "p95_latency_ms": 5.0,       // ms
    "order_accuracy": 100.0,     // %
    "order_errors": 0
  }
}
```

#### 예상 결과 (DB AUTO_INCREMENT 방식)
```json
{
  "results": {
    "throughput": 3000.0,        // msg/sec (더 낮음)
    "avg_latency_ms": 4.0,       // ms (더 높음)
    "p95_latency_ms": 10.0,      // ms (더 높음)
    "order_accuracy": 85.0,      // % (순서 오류 발생!)
    "order_errors": 750
  }
}
```

---

## 📊 시각화 (선택사항)

### 결과 비교 그래프 생성
```python
import json
import matplotlib.pyplot as plt

# 결과 파일 로드
with open('performance_result_redis.json') as f:
    redis_result = json.load(f)
    
with open('performance_result_no_redis.json') as f:
    no_redis_result = json.load(f)

# 비교 그래프
metrics = ['throughput', 'avg_latency_ms', 'order_accuracy']
redis_values = [redis_result['results'][m] for m in metrics]
no_redis_values = [no_redis_result['results'][m] for m in metrics]

# 그래프 생성
fig, axes = plt.subplots(1, 3, figsize=(15, 5))
for i, metric in enumerate(metrics):
    axes[i].bar(['Redis', 'No Redis'], [redis_values[i], no_redis_values[i]])
    axes[i].set_title(metric)
    axes[i].set_ylabel('Value')

plt.tight_layout()
plt.savefig('performance_comparison.png')
print("📊 그래프 저장: performance_comparison.png")
```

---

## 🎯 성능 증명 체크리스트

### ✅ Redis seqId 방식의 우수성 증명

- [ ] **처리량**: Redis 방식이 30% 이상 높음
- [ ] **지연시간**: Redis 방식이 P95 기준 50% 이상 낮음
- [ ] **순서 정확도**: Redis 방식이 100% (No Redis는 85% 이하)
- [ ] **확장성**: 동시 사용자 증가 시에도 성능 유지

### 📝 보고서 작성 항목

1. **테스트 환경**
   - 서버 스펙 (CPU, RAM)
   - Redis/RabbitMQ 버전
   - 네트워크 환경

2. **테스트 결과**
   - 각 시나리오별 측정값
   - 비교 그래프
   - 순서 오류 사례

3. **결론**
   - Redis seqId 방식의 장점
   - 성능 개선 수치
   - 실제 서비스 적용 시 기대 효과

---

## 🔧 트러블슈팅

### 문제 1: WebSocket 연결 실패
```bash
# 채팅 서버 로그 확인
tail -f backend/signal-chat/logs/application.log

# 포트 확인
lsof -i :8081
```

### 문제 2: Redis 연결 실패
```bash
# Redis 상태 확인
redis-cli ping
# 응답: PONG

# Redis 키 확인
redis-cli keys "room:*"
```

### 문제 3: RabbitMQ 메시지 쌓임
```bash
# RabbitMQ 관리 콘솔 접속
open http://localhost:15672
# ID/PW: guest/guest

# Queue 상태 확인
# db.queue의 메시지 수 확인
```

---

## 📚 추가 테스트 아이디어

### 1. 분산 서버 테스트
- 채팅 서버 2대 이상 실행
- 로드 밸런서 추가
- 서버 간 메시지 순서 보장 확인

### 2. 장애 복구 테스트
- Redis 서버 중단 시 동작 확인
- RabbitMQ 재시작 시 메시지 손실 확인

### 3. 장기 안정성 테스트
- 24시간 연속 부하 테스트
- 메모리 누수 확인
- 성능 저하 여부 확인

---

## 🎓 학습 포인트

1. **Redis Atomic Increment의 중요성**
   - 분산 환경에서 순서 보장
   - 매우 빠른 처리 속도 (< 1ms)

2. **비동기 처리의 장점**
   - 실시간 전송과 DB 저장 분리
   - RabbitMQ를 통한 부하 분산

3. **성능 측정의 중요성**
   - 정량적 데이터로 설계 결정 정당화
   - 병목 지점 파악 및 최적화

---

## 📞 문의

테스트 관련 문의사항이 있으시면 이슈를 등록해주세요.
