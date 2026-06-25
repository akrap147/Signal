import * as mediasoup from 'mediasoup';
import { config } from './config.js';

let worker;

export async function initWorker() {
  worker = await mediasoup.createWorker(config.mediasoup.workerSettings);
  worker.on('died', () => {
    console.error('[Mediasoup] Worker died');
    process.exit(1);
  });
  console.log(`[Mediasoup] Worker created (pid: ${worker.pid})`);
}

export function getWorker() {
  return worker;
}
