import { create } from 'zustand';
import { Device } from 'mediasoup-client';
import useChatStore from './useChatStore';
import useAuthStore from './useAuthStore';

// 컴포넌트 외부 mediasoup 객체 (Zustand 상태 아님)
export const ms = {
  device: null,
  sendTransport: null,
  recvTransport: null,
  producer: null,
  inputStream: null,
  outputTracks: [],
  pendingProducers: [],
  isCreatingRecvTransport: false,
  pendingProducerCallback: null,
  producerToUser: {},
  subscriptions: [],
};

// STOMP로 백엔드에 액션 전송
function sendAction(client, action, roomId, userId, payload = {}) {
  client.publish({
    destination: '/pub/voice',
    body: JSON.stringify({ action, roomId, userId, payload }),
  });
}

// ──────────────── 시그널링 핸들러 (개인 토픽) ────────────────

async function handlePersonalMessage(action, data, channelId, userId) {
  const client = useChatStore.getState().client;

  switch (action) {
    case 'joinRoom': {
      // data = { rtpCapabilities } 또는 rtpCapabilities 직접
      const rtpCapabilities = data.rtpCapabilities ?? data;
      const device = new Device();
      await device.load({ routerRtpCapabilities: rtpCapabilities });
      ms.device = device;
      useVoiceStore.setState({ isConnected: true });
      sendAction(client, 'createTransport', channelId, userId);
      break;
    }

    case 'createTransport': {
      if (!ms.sendTransport) {
        await initSendTransport(data, channelId, userId);
      } else if (ms.isCreatingRecvTransport) {
        await initRecvTransport(data, channelId, userId);
      }
      break;
    }

    case 'produced': {
      ms.pendingProducerCallback?.({ id: data.id });
      ms.pendingProducerCallback = null;
      break;
    }

    case 'consumed': {
      await finalizeConsume(data, channelId, userId);
      break;
    }

    case 'error':
      console.error('[Voice] 서버 에러');
      break;

    default:
      break;
  }
}

// ──────────────── 시그널링 핸들러 (방 전체 토픽) ────────────────

function handleRoomMessage(event, channelId, userId) {
  if (event.action === 'newProducer') {
    const { producerId, userId: remoteUserId } = event;
    if (String(remoteUserId) === String(userId)) return; // 자신 제외
    ms.producerToUser[producerId] = remoteUserId;
    useVoiceStore.setState((state) => ({
      participants: { ...state.participants, [remoteUserId]: true },
    }));
    consumeProducer(producerId, channelId, userId);
  }

  if (event.action === 'userLeft') {
    const { userId: leftId } = event;
    useVoiceStore.setState((state) => {
      const { [leftId]: _, ...rest } = state.participants;
      return { participants: rest };
    });
  }
}

// ──────────────── 전송 Transport 초기화 ────────────────

async function initSendTransport(params, channelId, userId) {
  const client = useChatStore.getState().client;
  const transport = ms.device.createSendTransport(params);
  ms.sendTransport = transport;

  transport.on('connect', ({ dtlsParameters }, callback) => {
    sendAction(client, 'connect', channelId, userId, {
      roomId: channelId,
      transportId: params.id,
      dtlsParameters,
    });
    callback();
  });

  transport.on('produce', ({ kind, rtpParameters }, callback) => {
    ms.pendingProducerCallback = callback;
    sendAction(client, 'produce', channelId, userId, {
      roomId: channelId,
      transportId: params.id,
      kind,
      rtpParameters,
    });
  });

  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    ms.inputStream = stream;
    ms.producer = await transport.produce({ track: stream.getAudioTracks()[0] });
  } catch (e) {
    console.error('[Voice] 마이크 접근 실패:', e);
  }
}

// ──────────────── 수신 Transport 초기화 ────────────────

async function initRecvTransport(params, channelId, userId) {
  const client = useChatStore.getState().client;
  const transport = ms.device.createRecvTransport(params);
  ms.recvTransport = transport;
  ms.isCreatingRecvTransport = false;

  transport.on('connect', ({ dtlsParameters }, callback) => {
    sendAction(client, 'connect', channelId, userId, {
      roomId: channelId,
      transportId: params.id,
      dtlsParameters,
    });
    callback();
  });

  // 대기 중이던 producer들 소비
  const pending = ms.pendingProducers.splice(0);
  for (const pid of pending) {
    requestConsume(pid, channelId, userId);
  }
}

// ──────────────── Producer 소비 ────────────────

function consumeProducer(producerId, channelId, userId) {
  const client = useChatStore.getState().client;
  if (ms.recvTransport) {
    requestConsume(producerId, channelId, userId);
  } else {
    ms.pendingProducers.push(producerId);
    if (!ms.isCreatingRecvTransport) {
      ms.isCreatingRecvTransport = true;
      sendAction(client, 'createTransport', channelId, userId);
    }
  }
}

function requestConsume(producerId, channelId, userId) {
  const client = useChatStore.getState().client;
  sendAction(client, 'consume', channelId, userId, {
    roomId: channelId,
    transportId: ms.recvTransport.id,
    producerId,
    rtpCapabilities: ms.device.rtpCapabilities,
  });
}

async function finalizeConsume(data, channelId, userId) {
  const client = useChatStore.getState().client;
  const { id, producerId, kind, rtpParameters } = data;
  const consumer = await ms.recvTransport.consume({ id, producerId, kind, rtpParameters });

  sendAction(client, 'resume', channelId, userId, {
    roomId: channelId,
    consumerId: id,
  });

  ms.outputTracks.push(consumer.track);
  const audio = document.createElement('audio');
  audio.id = `voice-audio-${id}`;
  audio.autoplay = true;
  audio.srcObject = new MediaStream([consumer.track]);
  audio.style.display = 'none';
  document.body.appendChild(audio);
  audio.play().catch(() => {});
}

// ──────────────── 정리 ────────────────

function cleanup() {
  ms.producer?.close();      ms.producer = null;
  ms.sendTransport?.close(); ms.sendTransport = null;
  ms.recvTransport?.close(); ms.recvTransport = null;
  ms.device = null;
  ms.inputStream?.getTracks().forEach((t) => t.stop()); ms.inputStream = null;
  ms.outputTracks = [];
  ms.pendingProducers = [];
  ms.isCreatingRecvTransport = false;
  ms.pendingProducerCallback = null;
  ms.producerToUser = {};
  ms.subscriptions.forEach((s) => s?.unsubscribe()); ms.subscriptions = [];
  document.querySelectorAll('audio[id^="voice-audio-"]').forEach((el) => el.remove());
}

// ──────────────── Zustand Store ────────────────

const useVoiceStore = create((set, get) => ({
  activeVoiceChannelId: null,
  isMuted: false,
  isConnected: false,
  participants: {}, // { [userId]: true }

  joinVoiceChannel: async (channelId) => {
    if (get().activeVoiceChannelId) get().leaveVoiceChannel();

    const client = useChatStore.getState().client;
    if (!client?.active) {
      console.error('[Voice] STOMP 미연결');
      return;
    }

    const user = useAuthStore.getState().user;
    const userId = user?.id;

    set({ activeVoiceChannelId: channelId, isConnected: false, participants: {} });
    sessionStorage.setItem('voiceChannelId', String(channelId));

    const personalSub = client.subscribe(
      `/topic/voice.${channelId}.${userId}`,
      async (msg) => {
        const { action, data } = JSON.parse(msg.body);
        await handlePersonalMessage(action, data, channelId, userId);
      }
    );

    const roomSub = client.subscribe(
      `/topic/voice.${channelId}`,
      (msg) => {
        const event = JSON.parse(msg.body);
        handleRoomMessage(event, channelId, userId);
      }
    );

    ms.subscriptions = [personalSub, roomSub];
    sendAction(client, 'joinRoom', channelId, userId);
  },

  leaveVoiceChannel: () => {
    const { activeVoiceChannelId } = get();
    const client = useChatStore.getState().client;
    const userId = useAuthStore.getState().user?.id;

    if (activeVoiceChannelId && client?.active) {
      sendAction(client, 'leave', activeVoiceChannelId, userId, {
        roomId: activeVoiceChannelId,
      });
    }

    cleanup();
    sessionStorage.removeItem('voiceChannelId');
    set({ activeVoiceChannelId: null, isMuted: false, isConnected: false, participants: {} });
  },

  toggleMute: () => {
    if (!ms.producer?.track) return;
    const next = !get().isMuted;
    ms.producer.track.enabled = !next;
    set({ isMuted: next });
  },
}));

export default useVoiceStore;
