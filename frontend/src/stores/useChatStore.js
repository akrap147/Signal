import { create } from 'zustand';
import { Client } from '@stomp/stompjs';

const useChatStore = create((set, get) => ({
  client: null,
  isConnected: false,
  messages: [], // 현재 보고 있는 채널의 메시지들

  // 1. WebSocket 연결
  connect: (userId) => {
    // 이미 연결되어 있으면 패스
    if (get().client && get().client.active) return;

    const client = new Client({
      brokerURL: 'ws://localhost:8081/ws-stomp', // 채팅 서버 주소 (8081 포트 확인!)
      reconnectDelay: 5000, // 연결 끊기면 5초 뒤 재시도
      onConnect: () => {
        console.log('✅ Chat Server Connected!');
        set({ isConnected: true });
      },
      onDisconnect: () => {
        console.log('🔴 Disconnected');
        set({ isConnected: false });
      },
      // 디버깅용 로그 (개발 중에만 켜둠)
      debug: (str) => console.log(str),
    });

    client.activate();
    set({ client });
  },

  // 2. 연결 해제 (로그아웃 시)
  disconnect: () => {
    const { client } = get();
    if (client) {
      client.deactivate();
      set({ client: null, isConnected: false, messages: [] });
    }
  },

  // 3. 채널 구독 (방에 들어갔을 때)
  subscribeToChannel: (channelId, type = 'channel') => {
    const { client } = get();
    if (!client || !client.active) return;

    // 기존 메시지 초기화 (새 방에 들어갔으니)
    set({ messages: [] });

    // 구독 요청: /sub/channel/{id}
    const topic = type === 'dm' ? `/sub/dm/${channelId}` : `/sub/channel/${channelId}`;
    
    console.log(`👀 Subscribing to ${topic}`);

    client.subscribe(topic, (message) => {
      const receivedMsg = JSON.parse(message.body);
      
      // 상태 업데이트: 기존 메시지 리스트 뒤에 새 메시지 추가
      set((state) => ({
        messages: [...state.messages, receivedMsg],
      }));
    });
  },

  // 4. 메시지 전송
  sendMessage: (channelId, userId, content, type = 'CHANNEL') => {
    const { client } = get();
    if (!client || !client.active) return;

    const payload = {
        type: type, // 'CHANNEL' or 'DM' (Enum 대문자 맞춤)
        roomId: channelId,
        senderId: userId,
        content: content
    };

    // 전송 요청: /pub/chat/message (DTO 구조에 맞춤)
    client.publish({
      destination: '/pub/chat/message',
      body: JSON.stringify(payload),
    });
  },
}));

export default useChatStore;
