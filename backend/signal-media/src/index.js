import dotenv from 'dotenv';
import amqp from 'amqplib';
import { config } from './config.js';
import { mediasoupManager } from './mediasoup.js';

dotenv.config();

let connection;
let channel;

const RECONNECT_INTERVAL = 5000;

async function connectRabbitMQ() {
  try {
    console.log('[RabbitMQ] Connecting...');
    connection = await amqp.connect(config.rabbitmq.url);
    channel = await connection.createChannel();

    connection.on('error', (err) => {
      console.error('[RabbitMQ] Connection error', err);
      setTimeout(connectRabbitMQ, RECONNECT_INTERVAL);
    });

    connection.on('close', () => {
      console.warn('[RabbitMQ] Connection closed. Reconnecting...');
      setTimeout(connectRabbitMQ, RECONNECT_INTERVAL);
    });
    
    await channel.assertExchange(config.rabbitmq.exchange, 'topic', { durable: true });
    const q = await channel.assertQueue(config.rabbitmq.requestQueue, { durable: true });
    await channel.bindQueue(q.queue, config.rabbitmq.exchange, 'signal.media.#');

    console.log(`[*] Waiting for messages in ${q.queue}`);

    channel.consume(q.queue, async (msg) => {
      if (msg !== null) {
        const routingKey = msg.fields.routingKey;
        const content = JSON.parse(msg.content.toString());
        const replyTo = msg.properties.replyTo;
        const correlationId = msg.properties.correlationId;

        console.log(`[x] Received: ${routingKey}`, content);
        
        let response = { success: false };

        try {
          if (routingKey === 'signal.media.createRouter') {
            const router = await mediasoupManager.getOrCreateRouter(content.roomId);
            response = { success: true, rtpCapabilities: router.rtpCapabilities };
          } 
          else if (routingKey === 'signal.media.createTransport') {
            const transportInfo = await mediasoupManager.createWebRtcTransport(content.roomId);
            response = { success: true, ...transportInfo };
          }
          else if (routingKey === 'signal.media.connectTransport') {
            const { transportId, dtlsParameters } = content;
            await mediasoupManager.connectWebRtcTransport(transportId, dtlsParameters);
            response = { success: true };
          }
          else if (routingKey === 'signal.media.produce') {
            const { transportId, kind, rtpParameters } = content;
            const producerInfo = await mediasoupManager.produce(transportId, kind, rtpParameters);
            response = { success: true, ...producerInfo };
          }
          else if (routingKey === 'signal.media.consume') {
            const { transportId, producerId, rtpCapabilities, roomId } = content;
            const consumerInfo = await mediasoupManager.consume(transportId, producerId, rtpCapabilities, roomId);
            response = { success: true, ...consumerInfo };
          }
          else if (routingKey === 'signal.media.resume') {
             const { consumerId } = content;
             await mediasoupManager.resume(consumerId);
             response = { success: true };
          }
          else if (routingKey === 'signal.media.closeTransport') {
            const { transportId } = content;
            await mediasoupManager.closeTransport(transportId);
            response = { success: true };
          }
          else if (routingKey === 'signal.media.closeProducer') {
            const { producerId } = content;
            await mediasoupManager.closeProducer(producerId);
            response = { success: true };
          }
          else if (routingKey === 'signal.media.closeConsumer') {
            const { consumerId } = content;
            await mediasoupManager.closeConsumer(consumerId);
            response = { success: true };
          }
          
          // 응답 전송 (RPC 패턴)
          if (replyTo) {
            channel.sendToQueue(replyTo, Buffer.from(JSON.stringify(response)), {
              correlationId: correlationId,
              contentType: 'application/json'
            });
          }
        } catch (err) {
          console.error('Error handling message', err);
          if (replyTo) {
            channel.sendToQueue(replyTo, Buffer.from(JSON.stringify({ success: false, error: err.message })), {
              correlationId: correlationId,
              contentType: 'application/json'
            });
          }
        }

        channel.ack(msg);
      }
    });

  } catch (error) {
    console.error('[RabbitMQ] Failed to connect', error);
    setTimeout(connectRabbitMQ, RECONNECT_INTERVAL);
  }
}

async function run() {
  console.log('--- Starting Media Server ---');
  // 1. Mediasoup 초기화
  await mediasoupManager.init();

  // 2. RabbitMQ 연결 (Auto-Reconnect)
  connectRabbitMQ();
}

run();
