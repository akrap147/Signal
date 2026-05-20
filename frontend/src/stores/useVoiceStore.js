import { create } from 'zustand';
import { Device } from 'mediasoup-client';
import useAuthStore from './useAuthStore';

const SIGNALING_URL = import.meta.env.VITE_SIGNALING_URL;

export const ms = {
  ws: null,
  device: null,
  sendTransport: null,
  recvTransport: null,
  producer: null,
  videoProducer: null,
  videoStream: null,
  pendingProducers: [],
  isCreatingRecvTransport: false,
  pendingProducerCallback: null,
  pendingVideoProducerCallback: null,
  inputStream: null,
  outputTracks: [],
  producerToUser: {},  // { [producerId]: userId }
};

const useVoiceStore = create((set, get) => ({
  activeVoiceChannelId: null,
  isMuted: false,
  isConnected: false,
  isVideoEnabled: false,
  participants: {},   // { [userId]: { username } }
  remoteVideos: {},   // { [userId]: MediaStreamTrack | null }

  joinVoiceChannel: async (channelId) => {
    if (get().activeVoiceChannelId) {
      get().leaveVoiceChannel();
    }

    sessionStorage.setItem('voiceChannelId', String(channelId));
    set({ activeVoiceChannelId: channelId, isConnected: false });

    const user = useAuthStore.getState().user;
    const userId = String(user?.id ?? '');
    const username = user?.username ?? '';

    const ws = new WebSocket(SIGNALING_URL);
    ms.ws = ws;

    ws.onopen = () => {
      ws.send(JSON.stringify({
        type: 'join',
        roomId: String(channelId),
        data: { userId, username },
      }));
    };

    ws.onmessage = async (event) => {
      const msg = JSON.parse(event.data);
      await handleMessage(msg, channelId, set, get);
    };

    ws.onclose = () => {
      set({ isConnected: false });
    };
  },

  leaveVoiceChannel: () => {
    sessionStorage.removeItem('voiceChannelId');
    if (ms.producer)      { ms.producer.close();      ms.producer = null; }
    if (ms.videoProducer) { ms.videoProducer.close();  ms.videoProducer = null; }
    if (ms.videoStream)   { ms.videoStream.getTracks().forEach(t => t.stop()); ms.videoStream = null; }
    if (ms.sendTransport) { ms.sendTransport.close();  ms.sendTransport = null; }
    if (ms.recvTransport) { ms.recvTransport.close();  ms.recvTransport = null; }
    if (ms.ws)            { ms.ws.close();             ms.ws = null; }
    ms.device = null;
    ms.pendingProducers = [];
    ms.isCreatingRecvTransport = false;
    ms.pendingProducerCallback = null;
    ms.pendingVideoProducerCallback = null;
    ms.inputStream = null;
    ms.outputTracks = [];
    ms.producerToUser = {};

    document.querySelectorAll('audio[id^="voice-audio-"]').forEach(el => el.remove());

    set({
      activeVoiceChannelId: null,
      isMuted: false,
      isConnected: false,
      isVideoEnabled: false,
      participants: {},
      remoteVideos: {},
    });
  },

  toggleMute: () => {
    if (!ms.producer) return;
    const next = !get().isMuted;
    ms.producer.track.enabled = !next;
    set({ isMuted: next });
  },

  toggleVideo: async () => {
    if (!ms.sendTransport || !ms.device) return;

    if (get().isVideoEnabled) {
      if (ms.videoProducer) {
        const producerId = ms.videoProducer.id;
        ms.videoProducer.close();
        ms.videoProducer = null;
        const roomId = String(get().activeVoiceChannelId);
        ms.ws?.send(JSON.stringify({ type: 'closeProducer', roomId, data: { producerId } }));
      }
      if (ms.videoStream) { ms.videoStream.getTracks().forEach(t => t.stop()); ms.videoStream = null; }
      set({ isVideoEnabled: false });
    } else {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ video: true });
        ms.videoStream = stream;
        ms.videoProducer = await ms.sendTransport.produce({ track: stream.getVideoTracks()[0] });
        set({ isVideoEnabled: true });
      } catch (e) {
        console.error('[Voice] Camera access failed:', e);
      }
    }
  },
}));

async function handleMessage(msg, channelId, set, get) {
  const roomId = String(channelId);

  if (msg.type === 'userJoined') {
    // sessionId is always present; userId/username are display extras
    const key = msg.sessionId ?? msg.userId;
    if (!key) return;
    const label = msg.username || msg.userId || '상대방';
    set(state => ({
      participants: { ...state.participants, [key]: { username: label } },
    }));
    return;
  }

  if (msg.type === 'userLeft') {
    const key = msg.sessionId ?? msg.userId;
    if (!key) return;
    set(state => {
      const { [key]: _p, ...restParticipants } = state.participants;
      const { [key]: _v, ...restVideos } = state.remoteVideos;
      return { participants: restParticipants, remoteVideos: restVideos };
    });
    return;
  }

  if (msg.type === 'newProducer') {
    // Use sessionId as the stable participant key; userId/username are for display
    const key = msg.sessionId ?? msg.userId ?? msg.producerId;
    const label = msg.username || msg.userId || '상대방';
    // Ensure participant tile exists (upsert, don't overwrite existing)
    set(state => ({
      participants: state.participants[key]
        ? state.participants
        : { ...state.participants, [key]: { username: label } },
    }));
    ms.producerToUser[msg.producerId] = key;
    consumeProducer(msg.producerId, roomId);
    return;
  }

  if (msg.type === 'producerClosed') {
    const key = ms.producerToUser[msg.producerId];
    delete ms.producerToUser[msg.producerId];
    if (key) {
      // Keep participant tile, just clear the video track
      set(state => ({ remoteVideos: { ...state.remoteVideos, [key]: null } }));
    }
    return;
  }

  if (msg.rtpCapabilities) {
    const device = new Device();
    await device.load({ routerRtpCapabilities: msg.rtpCapabilities });
    ms.device = device;
    set({ isConnected: true });

    ms.ws.send(JSON.stringify({
      type: 'createTransport',
      roomId,
      data: { roomId, direction: 'send' },
    }));
    return;
  }

  if (msg.id && msg.iceParameters) {
    if (!ms.sendTransport && !ms.isCreatingRecvTransport) {
      await initSendTransport(msg, roomId, set);
    } else if (ms.isCreatingRecvTransport) {
      await initRecvTransport(msg, roomId);
    }
    return;
  }

  // produce response
  if (msg.id && !msg.iceParameters && !msg.rtpCapabilities && !msg.producerId) {
    if (ms.pendingProducerCallback) {
      ms.pendingProducerCallback({ id: msg.id });
      ms.pendingProducerCallback = null;
    } else if (ms.pendingVideoProducerCallback) {
      ms.pendingVideoProducerCallback({ id: msg.id });
      ms.pendingVideoProducerCallback = null;
    }
    return;
  }

  if (msg.id && msg.producerId && msg.kind && msg.rtpParameters) {
    await finalizeConsume(msg, roomId, set);
  }
}

async function initSendTransport(serverData, roomId, set) {
  const sendTransport = ms.device.createSendTransport(serverData);
  ms.sendTransport = sendTransport;

  sendTransport.on('connect', ({ dtlsParameters }, callback) => {
    ms.ws.send(JSON.stringify({
      type: 'connectTransport',
      roomId,
      data: { roomId, transportId: serverData.id, dtlsParameters },
    }));
    callback();
  });

  sendTransport.on('produce', ({ kind, rtpParameters }, callback) => {
    ms.ws.send(JSON.stringify({
      type: 'produce',
      roomId,
      data: { roomId, transportId: serverData.id, kind, rtpParameters },
    }));
    if (kind === 'audio') {
      ms.pendingProducerCallback = callback;
    } else {
      ms.pendingVideoProducerCallback = callback;
    }
  });

  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    ms.inputStream = stream;
    ms.producer = await sendTransport.produce({ track: stream.getAudioTracks()[0] });
  } catch (e) {
    console.error('[Voice] Mic access failed:', e);
  }
}

function consumeProducer(remoteProducerId, roomId) {
  if (ms.recvTransport) {
    requestConsume(remoteProducerId, roomId);
  } else {
    ms.pendingProducers.push(remoteProducerId);
    if (!ms.isCreatingRecvTransport) {
      ms.isCreatingRecvTransport = true;
      ms.ws.send(JSON.stringify({
        type: 'createTransport',
        roomId,
        data: { roomId, direction: 'recv' },
      }));
    }
  }
}

async function initRecvTransport(serverData, roomId) {
  const recvTransport = ms.device.createRecvTransport(serverData);
  ms.recvTransport = recvTransport;
  ms.isCreatingRecvTransport = false;

  recvTransport.on('connect', ({ dtlsParameters }, callback) => {
    ms.ws.send(JSON.stringify({
      type: 'connectTransport',
      roomId,
      data: { roomId, transportId: serverData.id, dtlsParameters },
    }));
    callback();
  });

  const pending = ms.pendingProducers.splice(0);
  for (const pid of pending) {
    requestConsume(pid, roomId);
  }
}

function requestConsume(remoteProducerId, roomId) {
  ms.ws.send(JSON.stringify({
    type: 'consume',
    roomId,
    data: {
      roomId,
      transportId: ms.recvTransport.id,
      producerId: remoteProducerId,
      rtpCapabilities: ms.device.rtpCapabilities,
    },
  }));
}

async function finalizeConsume(data, roomId, set) {
  const { id, producerId, kind, rtpParameters } = data;
  const consumer = await ms.recvTransport.consume({ id, producerId, kind, rtpParameters });

  ms.ws.send(JSON.stringify({
    type: 'resume',
    roomId,
    data: { roomId, consumerId: id },
  }));

  if (kind === 'video') {
    const key = ms.producerToUser[producerId] ?? producerId;
    set(state => ({ remoteVideos: { ...state.remoteVideos, [key]: consumer.track } }));

    consumer.on('transportclose', () => {
      set(state => { const { [key]: _, ...rest } = state.remoteVideos; return { remoteVideos: rest }; });
    });
    consumer.on('producerclose', () => {
      consumer.close();
      set(state => ({ remoteVideos: { ...state.remoteVideos, [key]: null } }));
    });
  } else {
    ms.outputTracks.push(consumer.track);

    const audio = document.createElement('audio');
    audio.id = `voice-audio-${id}`;
    audio.autoplay = true;
    audio.srcObject = new MediaStream([consumer.track]);
    audio.style.display = 'none';
    document.body.appendChild(audio);
    audio.play().catch(() => {});
  }
}

export default useVoiceStore;
