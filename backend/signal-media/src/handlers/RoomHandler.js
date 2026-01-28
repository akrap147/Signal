import { config } from '../config.js';

export class RoomHandler {
  constructor(workerHandler) {
    this.workerHandler = workerHandler;
    this.routers = new Map(); // roomId -> router
  }

  async getOrCreateRouter(roomId) {
    if (this.routers.has(roomId)) {
      return this.routers.get(roomId);
    }

    const worker = this.workerHandler.getWorker();
    const router = await worker.createRouter(config.mediasoup.routerOptions);
    
    this.routers.set(roomId, router);
    console.log(`[Mediasoup] Router created for room: ${roomId}`);
    
    return router;
  }

  getRouter(roomId) {
    return this.routers.get(roomId);
  }
}
