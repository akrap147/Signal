import * as mediasoup from 'mediasoup';
import { config } from './config.js';
import { v4 as uuidv4 } from 'uuid';

class MediasoupManager {
  constructor() {
    this.workers = [];
    this.routers = new Map(); // roomId -> router
    this.transports = new Map(); // transportId -> transport
    this.producers = new Map(); // producerId -> producer
    this.consumers = new Map(); // consumerId -> consumer
  }

  async init() {
    // 기본적으로 하나만 생성 (스케일링 필요 시 루프)
    const worker = await mediasoup.createWorker(config.mediasoup.workerSettings);
    worker.on('died', () => {
      console.error('mediasoup worker died');
      process.exit(1);
    });
    this.workers.push(worker);
    console.log(`[Mediasoup] Worker created (pid: ${worker.pid})`);
  }

  getWorker() {
    // 간단하게 첫 번째 worker 반환 (라운드 로빈 등 확장 가능)
    return this.workers[0];
  }

  async getOrCreateRouter(roomId) {
    if (this.routers.has(roomId)) {
      return this.routers.get(roomId);
    }

    const worker = this.getWorker();
    const router = await worker.createRouter(config.mediasoup.routerOptions);
    this.routers.set(roomId, router);
    console.log(`[Mediasoup] Router created for room: ${roomId}`);
    return router;
  }

  async createWebRtcTransport(roomId) {
    const router = await this.getOrCreateRouter(roomId);
    const transport = await router.createWebRtcTransport(config.mediasoup.webRtcTransportOptions);

    transport.on('dtlsstatechange', (dtlsState) => {
      if (dtlsState === 'closed') {
        transport.close();
      }
    });

    this.transports.set(transport.id, transport);
    
    return {
      id: transport.id,
      iceParameters: transport.iceParameters,
      iceCandidates: transport.iceCandidates,
      dtlsParameters: transport.dtlsParameters,
    };
  }
}

export const mediasoupManager = new MediasoupManager();
