import { config } from './config.js';
import { getOrCreateRouter } from './room.js';

const transports    = new Map(); // transportId → transport
const producers     = new Map(); // producerId  → producer
const consumers     = new Map(); // consumerId  → consumer
const roomProducers = new Map(); // roomId      → Set<producerId>

export function getProducerIds(roomId) {
  return [...(roomProducers.get(roomId) || [])];
}

export async function createTransport(roomId) {
  const router = await getOrCreateRouter(roomId);
  const transport = await router.createWebRtcTransport(config.mediasoup.webRtcTransportOptions);

  transport.appData.roomId = roomId;
  transport.on('dtlsstatechange', (state) => { if (state === 'closed') transport.close(); });
  transports.set(transport.id, transport);

  return {
    id: transport.id,
    iceParameters: transport.iceParameters,
    iceCandidates: transport.iceCandidates,
    dtlsParameters: transport.dtlsParameters,
  };
}

export async function connectTransport(transportId, dtlsParameters) {
  const transport = transports.get(transportId);
  if (!transport) throw new Error(`Transport not found: ${transportId}`);
  await transport.connect({ dtlsParameters });
}

export async function produce(transportId, kind, rtpParameters) {
  const transport = transports.get(transportId);
  if (!transport) throw new Error(`Transport not found: ${transportId}`);

  const producer = await transport.produce({ kind, rtpParameters });
  const roomId = transport.appData.roomId;

  producer.appData.roomId = roomId;
  producers.set(producer.id, producer);

  if (!roomProducers.has(roomId)) roomProducers.set(roomId, new Set());
  roomProducers.get(roomId).add(producer.id);

  producer.on('transportclose', () => {
    roomProducers.get(roomId)?.delete(producer.id);
    producers.delete(producer.id);
  });

  return { id: producer.id };
}

export async function consume(transportId, producerId, rtpCapabilities, roomId) {
  const router = await getOrCreateRouter(roomId);
  if (!router.canConsume({ producerId, rtpCapabilities })) throw new Error('Cannot consume');

  const transport = transports.get(transportId);
  if (!transport) throw new Error(`Transport not found: ${transportId}`);

  const consumer = await transport.consume({ producerId, rtpCapabilities, paused: true });
  consumers.set(consumer.id, consumer);

  consumer.on('transportclose', () => consumers.delete(consumer.id));
  consumer.on('producerclose', () => { consumers.delete(consumer.id); consumer.close(); });

  return {
    id: consumer.id,
    producerId: consumer.producerId,
    kind: consumer.kind,
    rtpParameters: consumer.rtpParameters,
  };
}

export async function resume(consumerId) {
  const consumer = consumers.get(consumerId);
  if (!consumer) throw new Error(`Consumer not found: ${consumerId}`);
  await consumer.resume();
}

export async function closeTransport(transportId) {
  const transport = transports.get(transportId);
  if (!transport) return;
  transport.close();
  transports.delete(transportId);
}
