"""
채팅 시스템 성능 벤치마크 테스트
- Redis seqId vs DB AUTO_INCREMENT 비교
- 처리량, 지연시간, 순서 정확도 측정
"""

import asyncio
import websockets
import json
import time
import statistics
from datetime import datetime
from collections import defaultdict

# 테스트 설정
WS_URL = "ws://localhost:8081/ws-stomp"
CHANNEL_ID = 1
CONCURRENT_USERS = 50  # 동시 접속자 수
MESSAGES_PER_USER = 100  # 사용자당 메시지 수
TOTAL_MESSAGES = CONCURRENT_USERS * MESSAGES_PER_USER

# 결과 수집
results = {
    'sent_times': {},  # {message_id: sent_timestamp}
    'received_times': {},  # {message_id: received_timestamp}
    'received_seqIds': [],  # 수신된 seqId 순서
    'latencies': [],  # 지연시간 리스트
    'order_errors': 0,  # 순서 오류 카운트
}

async def subscriber(user_id):
    """메시지 수신 전담 클라이언트"""
    try:
        async with websockets.connect(WS_URL) as websocket:
            # CONNECT
            connect_frame = "CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\0"
            await websocket.send(connect_frame)
            await websocket.recv()

            # SUBSCRIBE
            subscribe_frame = f"SUBSCRIBE\nid:sub-{user_id}\ndestination:/topic/channel.{CHANNEL_ID}\n\n\0"
            await websocket.send(subscribe_frame)

            print(f"📡 Subscriber {user_id} connected")

            # 메시지 수신
            while True:
                try:
                    msg = await websocket.recv()
                    parts = msg.split("\n\n")
                    if len(parts) > 1:
                        body = parts[-1].replace('\0', '')
                        if body.strip():
                            try:
                                data = json.loads(body)
                                if "PERF_TEST" in data.get('content', ''):
                                    # 메시지 ID 추출
                                    msg_id = data['content'].split('_')[-1]
                                    recv_time = time.time()
                                    seq_id = data.get('seqId', -1)
                                    
                                    # 결과 기록
                                    results['received_times'][msg_id] = recv_time
                                    results['received_seqIds'].append(seq_id)
                                    
                                    # 지연시간 계산
                                    if msg_id in results['sent_times']:
                                        latency = recv_time - results['sent_times'][msg_id]
                                        results['latencies'].append(latency)
                                        
                            except json.JSONDecodeError:
                                pass
                except websockets.ConnectionClosed:
                    break
                except Exception as e:
                    print(f"Subscriber {user_id} error: {e}")
                    break
    except Exception as e:
        print(f"Subscriber {user_id} failed to connect: {e}")

async def sender(user_id, message_count):
    """메시지 전송 클라이언트"""
    try:
        async with websockets.connect(WS_URL) as websocket:
            # CONNECT
            connect_frame = "CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\0"
            await websocket.send(connect_frame)
            await websocket.recv()

            print(f"📤 Sender {user_id} connected")

            # 메시지 전송
            for i in range(message_count):
                msg_id = f"{user_id}_{i}"
                message_payload = {
                    "type": "CHANNEL",
                    "roomId": CHANNEL_ID,
                    "senderId": user_id,
                    "content": f"PERF_TEST_{msg_id}",
                }
                
                # 전송 시간 기록
                send_time = time.time()
                results['sent_times'][msg_id] = send_time
                
                # SEND Frame
                send_frame = f"SEND\ndestination:/pub/chat/message\ncontent-type:application/json\n\n{json.dumps(message_payload)}\0"
                await websocket.send(send_frame)
                
                # 약간의 간격 (너무 빠르면 네트워크 버퍼 오버플로우)
                await asyncio.sleep(0.001)
            
            print(f"✅ Sender {user_id} finished sending {message_count} messages")
            
    except Exception as e:
        print(f"Sender {user_id} failed: {e}")

async def run_performance_test():
    """성능 테스트 실행"""
    print("=" * 60)
    print("🚀 채팅 시스템 성능 테스트 시작")
    print(f"📊 동시 사용자: {CONCURRENT_USERS}")
    print(f"📊 총 메시지: {TOTAL_MESSAGES}")
    print("=" * 60)
    
    # 1. Subscriber 시작 (메시지 수신 대기)
    print("\n📡 Subscribers 시작...")
    subscriber_tasks = [asyncio.create_task(subscriber(i)) for i in range(5)]
    await asyncio.sleep(2)  # 구독자 준비 대기
    
    # 2. Sender 시작 (메시지 전송)
    print(f"\n📤 {CONCURRENT_USERS}명의 Senders 시작...")
    start_time = time.time()
    
    sender_tasks = [
        asyncio.create_task(sender(user_id, MESSAGES_PER_USER)) 
        for user_id in range(CONCURRENT_USERS)
    ]
    
    # 모든 전송 완료 대기
    await asyncio.gather(*sender_tasks)
    
    # 3. 메시지 수신 대기 (충분한 시간)
    print("\n⏳ 메시지 수신 대기 중...")
    await asyncio.sleep(10)
    
    end_time = time.time()
    total_duration = end_time - start_time
    
    # 4. 결과 분석
    print("\n" + "=" * 60)
    print("📊 테스트 결과 분석")
    print("=" * 60)
    
    # 처리량
    messages_sent = len(results['sent_times'])
    messages_received = len(results['received_times'])
    throughput = messages_received / total_duration
    
    print(f"\n📈 처리량 (Throughput)")
    print(f"  - 전송된 메시지: {messages_sent}")
    print(f"  - 수신된 메시지: {messages_received}")
    print(f"  - 수신률: {messages_received/messages_sent*100:.2f}%")
    print(f"  - 처리 시간: {total_duration:.2f}초")
    print(f"  - 처리량: {throughput:.2f} msg/sec")
    
    # 지연시간
    if results['latencies']:
        avg_latency = statistics.mean(results['latencies']) * 1000  # ms
        p50_latency = statistics.median(results['latencies']) * 1000
        p95_latency = statistics.quantiles(results['latencies'], n=20)[18] * 1000 if len(results['latencies']) > 20 else 0
        p99_latency = statistics.quantiles(results['latencies'], n=100)[98] * 1000 if len(results['latencies']) > 100 else 0
        
        print(f"\n⏱️  지연시간 (Latency)")
        print(f"  - 평균: {avg_latency:.2f}ms")
        print(f"  - P50: {p50_latency:.2f}ms")
        print(f"  - P95: {p95_latency:.2f}ms")
        print(f"  - P99: {p99_latency:.2f}ms")
    
    # 순서 정확도
    seq_ids = results['received_seqIds']
    if seq_ids:
        order_errors = sum(1 for i in range(1, len(seq_ids)) if seq_ids[i] < seq_ids[i-1])
        order_accuracy = (1 - order_errors / len(seq_ids)) * 100
        
        print(f"\n🔢 순서 정확도 (Order Accuracy)")
        print(f"  - 수신된 메시지: {len(seq_ids)}")
        print(f"  - 순서 오류: {order_errors}")
        print(f"  - 정확도: {order_accuracy:.2f}%")
        print(f"  - seqId 범위: {min(seq_ids)} ~ {max(seq_ids)}")
    
    # 결과 저장
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    result_file = f"performance_result_{timestamp}.json"
    
    with open(result_file, 'w') as f:
        json.dump({
            'test_config': {
                'concurrent_users': CONCURRENT_USERS,
                'messages_per_user': MESSAGES_PER_USER,
                'total_messages': TOTAL_MESSAGES,
            },
            'results': {
                'messages_sent': messages_sent,
                'messages_received': messages_received,
                'throughput': throughput,
                'avg_latency_ms': avg_latency if results['latencies'] else 0,
                'p95_latency_ms': p95_latency if results['latencies'] else 0,
                'order_accuracy': order_accuracy if seq_ids else 0,
                'order_errors': order_errors if seq_ids else 0,
            }
        }, f, indent=2)
    
    print(f"\n💾 결과 저장: {result_file}")
    print("=" * 60)
    
    # Cleanup
    for task in subscriber_tasks:
        task.cancel()

if __name__ == "__main__":
    try:
        asyncio.run(run_performance_test())
    except KeyboardInterrupt:
        print("\n⚠️  테스트 중단됨")
