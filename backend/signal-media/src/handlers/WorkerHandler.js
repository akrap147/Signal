import * as mediasoup from 'mediasoup';
import { config } from '../config.js';

export class WorkerHandler {
  constructor() {
    this.workers = [];
  }

  async init() {
    const worker = await mediasoup.createWorker(config.mediasoup.workerSettings);
    
    worker.on('died', () => {
      console.error('mediasoup worker died');
      process.exit(1);
    });

    this.workers.push(worker);
    console.log(`[Mediasoup] Worker created (pid: ${worker.pid})`);
  }

  getWorker() {
    // Round-robin or simple selection (currently returning the first one)
    return this.workers[0];
  }
}
