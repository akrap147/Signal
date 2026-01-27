import asyncio
import websockets
import json
import time

# 설정
WS_URL = "ws://localhost:8081/ws-stomp"
CHANNEL_ID = 1
ZOMBIE_COUNT = 5  # Client bottleneck check
MESSAGE_COUNT = 10000 # 1만 발!
ATTACK_INTERVAL = 0.001 # 0.001초 (극도로 빠른 속도)

async def connect_and_subscribe(user_id):
    """좀비 클라이언트: 연결하고 구독만 함"""
    try:
        async with websockets.connect(WS_URL) as websocket:
            # 1. CONNECT Frame
            connect_frame = "CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\0"
            await websocket.send(connect_frame)
            await websocket.recv() # CONNECTED 응답 대기

            # 2. SUBSCRIBE Frame
            subscribe_frame = f"SUBSCRIBE\nid:sub-{user_id}\ndestination:/topic/channel.{CHANNEL_ID}\n\n\0"
            await websocket.send(subscribe_frame)

            # 무한 대기 (메시지 수신)
            while True:
                try:
                    msg = await websocket.recv()
                    # 1호 좀비(감시병)만 로그 출력
                    if user_id == 0:
                        # STOMP Frame 파싱 (Header와 Body 분리)
                        parts = msg.split("\n\n")
                        if len(parts) > 1:
                            body = parts[-1].replace('\0', '') # Null byte 제거
                            if body.strip():
                                try:
                                    data = json.loads(body)
                                    if "Traffic Attack Message" in str(body):
                                        latency = time.time() - data.get('ts', 0)
                                        content = data['content']
                                        seq_id = data.get('seqId', -1)
                                        idx = content.split()[-1]
                                        print(f"👀 Recv: {idx} (Seq: {seq_id}, Delay: {latency:.4f}s)")
                                except json.JSONDecodeError:
                                    pass # JSON 아니면 무시 (CONNECTED 등)
                except websockets.ConnectionClosed:
                    if user_id == 0: print("🔥 Monitor Zombie Disconnected!")
                    break
    except Exception as e:
        print(f"Zombie {user_id} died: {e}") # 로그 과다 방지 주석 해제
        pass

async def attacker():
    """공격자: 미친 듯이 메시지를 보냄"""
    try:
        async with websockets.connect(WS_URL) as websocket:
            # CONNECT
            connect_frame = "CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\0"
            await websocket.send(connect_frame)
            await websocket.recv()

            print("😈 Attacker Connected! Start bombing...")
            
            for i in range(MESSAGE_COUNT):
                message_payload = {
                    "type": "CHANNEL",
                    "roomId": CHANNEL_ID,
                    "senderId": 9999,
                    "content": f"Traffic Attack Message {i}",
                    "ts": time.time() # 보낸 시간 기록
                }
                # 3. SEND Frame (Traffic Attack) -> Controller 경유
                send_frame = f"SEND\ndestination:/pub/chat/message\ncontent-type:application/json\n\n{json.dumps(message_payload)}\0"
                await websocket.send(send_frame)
                
                if i % 10 == 0:
                    print(f"💣 Sent: {i}")

                await asyncio.sleep(ATTACK_INTERVAL)
                
            print("💀 Attack Finished.")
            
    except Exception as e:
        print(f"Attacker failed: {e}")

async def main():
    print(f"🧟 Spawning {ZOMBIE_COUNT} zombies...")
    
    # 좀비들 생성 (비동기로 동시에 접속 시도)
    zombies = [connect_and_subscribe(i) for i in range(ZOMBIE_COUNT)]
    
    # 좀비들을 백그라운드 태스크로 실행
    tasks = [asyncio.create_task(z) for z in zombies]
    
    # 좀비들이 접속할 시간 3초 대기
    await asyncio.sleep(3)
    print("🧟 Zombies are ready. Launching attacker...")
    
    # 공격 시작
    await attacker()

    print("⏳ Waiting 30s for remaining messages...")
    await asyncio.sleep(30)
    print("🏁 Test Complete.")

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("Test stopped by user")
