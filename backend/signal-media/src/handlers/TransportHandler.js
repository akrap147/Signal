import { config } from '../config.js';

export class TransportHandler {
  constructor(roomHandler) {
    this.roomHandler = roomHandler;
    this.transports = new Map(); // transportId -> transport
    this.producers = new Map();  // producerId -> producer
    this.consumers = new Map();  // consumerId -> consumer
  }

  async createWebRtcTransport(roomId) {
    const router = await this.roomHandler.getOrCreateRouter(roomId);
    const transport = await router.createWebRtcTransport(config.mediasoup.webRtcTransportOptions);

    transport.on('dtlsstatechange', (dtlsState) => {
      if (dtlsState === 'closed') {
        transport.close();
      }
    });

    // Store transport with roomId reference if needed later
    transport.appData.roomId = roomId;

    this.transports.set(transport.id, transport);
    
    return {
      id: transport.id,
      iceParameters: transport.iceParameters,
      iceCandidates: transport.iceCandidates,
      dtlsParameters: transport.dtlsParameters,
    };
  }

  async connectWebRtcTransport(transportId, dtlsParameters) {
    const transport = this.transports.get(transportId);
    if (!transport) throw new Error(`Transport not found: ${transportId}`);
    
    await transport.connect({ dtlsParameters });
    return { success: true };
  }

  async produce(transportId, kind, rtpParameters) {
    const transport = this.transports.get(transportId);
    if (!transport) throw new Error(`Transport not found: ${transportId}`);

    const producer = await transport.produce({ kind, rtpParameters });
    this.producers.set(producer.id, producer);

    producer.on('transportclose', () => {
      producer.close();
      this.producers.delete(producer.id);
    });

    return { id: producer.id };
  }

  async consume(transportId, producerId, rtpCapabilities, roomId) {
    // 1. Router 찾기
    const router = await this.roomHandler.getOrCreateRouter(roomId);
    if (!router) throw new Error(`Router not found for room: ${roomId}`);

    // 2. Consume 가능 여부 확인
    if (!router.canConsume({ producerId, rtpCapabilities })) {
      throw new Error('Cannot consume');
    }

    // 3. Transport 찾기
    const transport = this.transports.get(transportId);
    if (!transport) throw new Error(`Transport not found: ${transportId}`);

    // 4. Consumer 생성
    const consumer = await transport.consume({
      producerId,
      rtpCapabilities,
      paused: true, // Recommended to start paused
    });

    this.consumers.set(consumer.id, consumer);

    consumer.on('transportclose', () => {
      this.consumers.delete(consumer.id);
    });

    consumer.on('producerclose', () => {
        this.consumers.delete(consumer.id);
        consumer.close();
    });

    return {
      id: consumer.id,
      producerId: consumer.producerId,
      kind: consumer.kind,
      rtpParameters: consumer.rtpParameters,
    };
  }

  async resume(consumerId) {
    const consumer = this.consumers.get(consumerId);
    if (!consumer) throw new Error(`Consumer not found: ${consumerId}`);
    
    await consumer.resume();
    console.log(`[Mediasoup] Consumer resumed: ${consumerId}`);
    return { resumed: true };
  }

  async closeTransport(transportId) {
    const transport = this.transports.get(transportId);
    if (!transport) return;

    transport.close();
    this.transports.delete(transportId);
    console.log(`[Mediasoup] Transport closed: ${transportId}`);
  }

  async closeProducer(producerId) {
    const producer = this.producers.get(producerId);
    if (!producer) return;

    producer.close();
    this.producers.delete(producerId);
    console.log(`[Mediasoup] Producer closed: ${producerId}`);
  }

  async closeConsumer(consumerId) {
    const consumer = this.consumers.get(consumerId);
    if (!consumer) return;

    consumer.close();
    this.consumers.delete(consumerId);
    console.log(`[Mediasoup] Consumer closed: ${consumerId}`);
  }
}
