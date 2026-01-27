import dotenv from 'dotenv';
import amqp from 'amqplib';
import { config } from './config.js';
import { mediasoupManager } from './mediasoup.js';

dotenv.config();

let connection;
let channel;

async function run() {
  console.log('--- Starting Media Server ---');

  // 1. Mediasoup 초기화
  await mediasoupManager.init();

  // 2. RabbitMQ 연결
  try {
    connection = await amqp.connect(config.rabbitmq.url);
    channel = await connection.createChannel();
    
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
          
          // 응답 전송 (RPC 패턴)
          if (replyTo) {
            channel.sendToQueue(replyTo, Buffer.from(JSON.stringify(response)), {
              correlationId: correlationId
            });
          }
        } catch (err) {
          console.error('Error handling message', err);
          if (replyTo) {
            channel.sendToQueue(replyTo, Buffer.from(JSON.stringify({ success: false, error: err.message })), {
              correlationId: correlationId
            });
          }
        }

        channel.ack(msg);
      }
    });

  } catch (error) {
    console.error('Failed to connect to RabbitMQ', error);
  }
}

run();
