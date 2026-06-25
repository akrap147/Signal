import { config } from './config.js';
import { getWorker } from './worker.js';

const routers = new Map(); // roomId → router

export async function getOrCreateRouter(roomId) {
  if (!routers.has(roomId)) {
    const router = await getWorker().createRouter(config.mediasoup.routerOptions);
    routers.set(roomId, router);
    console.log(`[Mediasoup] Router created for room: ${roomId}`);
  }
  return routers.get(roomId);
}
