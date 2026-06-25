import useChatStore from './useChatStore';

export function subscribeToCanvas(roomId, onEvent) {
  const client = useChatStore.getState().client;
  if (!client || !client.active) return null;
  return client.subscribe(`/topic/canvas.${roomId}`, (message) => {
    onEvent(JSON.parse(message.body));
  });
}

export function publishDraw(payload) {
  const client = useChatStore.getState().client;
  if (!client || !client.active) return;
  client.publish({
    destination: '/pub/canvas/draw',
    body: JSON.stringify(payload),
  });
}

export async function fetchHistory(roomId) {
  try {
    const res = await fetch(`/api/canvas/${roomId}/history`);
    if (!res.ok) return [];
    const data = await res.json();
    return data.map((s) => (typeof s === 'string' ? JSON.parse(s) : s));
  } catch {
    return [];
  }
}
