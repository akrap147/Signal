import dotenv from 'dotenv';
import express from 'express';
import { config } from './config.js';
import { initWorker } from './worker.js';
import { getOrCreateRouter } from './room.js';
import { getProducerIds, createTransport, connectTransport, produce, consume, resume, closeTransport } from './session.js';

dotenv.config();

const app = express();
app.use(express.json());

app.post('/router', async (req, res) => {
  const { roomId } = req.body;
  const router = await getOrCreateRouter(roomId);
  res.json({ rtpCapabilities: router.rtpCapabilities, existingProducerIds: getProducerIds(roomId) });
});

app.post('/transport', async (req, res) => {
  res.json(await createTransport(req.body.roomId));
});

app.post('/connect', async (req, res) => {
  const { transportId, dtlsParameters } = req.body;
  await connectTransport(transportId, dtlsParameters);
  res.json({ success: true });
});

app.post('/produce', async (req, res) => {
  const { transportId, kind, rtpParameters } = req.body;
  res.json(await produce(transportId, kind, rtpParameters));
});

app.post('/consume', async (req, res) => {
  const { transportId, producerId, rtpCapabilities, roomId } = req.body;
  res.json(await consume(transportId, producerId, rtpCapabilities, roomId));
});

app.post('/resume', async (req, res) => {
  await resume(req.body.consumerId);
  res.json({ success: true });
});

app.post('/close', async (req, res) => {
  await closeTransport(req.body.transportId);
  res.json({ success: true });
});

app.use((err, req, res, next) => {
  console.error('[Error]', err.message);
  res.status(500).json({ success: false, error: err.message });
});

async function run() {
  await initWorker();
  app.listen(config.server.port, () => {
    console.log(`[signal-media] listening on port ${config.server.port}`);
  });
}

run();
