import { WorkerHandler } from './handlers/WorkerHandler.js';
import { RoomHandler } from './handlers/RoomHandler.js';
import { TransportHandler } from './handlers/TransportHandler.js';

class MediasoupManager {
  constructor() {
    this.workerHandler = new WorkerHandler();
    this.roomHandler = new RoomHandler(this.workerHandler);
    this.transportHandler = new TransportHandler(this.roomHandler);
  }

  async init() {
    await this.workerHandler.init();
  }

  async getOrCreateRouter(roomId) {
    return this.roomHandler.getOrCreateRouter(roomId);
  }

  getProducerIds(roomId) {
    return this.transportHandler.getProducerIds(roomId);
  }

  async createWebRtcTransport(roomId) {
    return this.transportHandler.createWebRtcTransport(roomId);
  }

  async connectWebRtcTransport(transportId, dtlsParameters) {
    return this.transportHandler.connectWebRtcTransport(transportId, dtlsParameters);
  }

  async produce(transportId, kind, rtpParameters) {
    return this.transportHandler.produce(transportId, kind, rtpParameters);
  }

  async consume(transportId, producerId, rtpCapabilities, roomId) {
    return this.transportHandler.consume(transportId, producerId, rtpCapabilities, roomId);
  }

  async resume(consumerId) {
    return this.transportHandler.resume(consumerId);
  }

  async closeTransport(transportId) {
    return this.transportHandler.closeTransport(transportId);
  }

  async closeProducer(producerId) {
    return this.transportHandler.closeProducer(producerId);
  }

  async closeConsumer(consumerId) {
    return this.transportHandler.closeConsumer(consumerId);
  }
}

export const mediasoupManager = new MediasoupManager();
