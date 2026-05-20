import os from 'os';

export const config = {
  // RabbitMQ
  rabbitmq: {
    url: process.env.RABBITMQ_URL || 'amqp://guest:guest@localhost:5672',
    exchange: 'signal.signaling.exchange',
    requestQueue: 'signal.media.request.queue',
  },
  // Mediasoup
  mediasoup: {
    workerSettings: {
      logLevel: 'warn',
      logTags: [
        'info',
        'ice',
        'dtls',
        'rtp',
        'srtp',
        'rtcp',
      ],
      rtcMinPort: 10000,
      rtcMaxPort: 10100,
    },
    // Router settings
    routerOptions: {
      mediaCodecs: [
        {
          kind: 'audio',
          mimeType: 'audio/opus',
          clockRate: 48000,
          channels: 2,
        },
        {
          kind: 'video',
          mimeType: 'video/VP8',
          clockRate: 90000,
        },
      ],
    },
    // WebRtcTransport settings
    webRtcTransportOptions: {
      listenIps: [
        {
          ip: process.env.MEDIASOUP_LISTEN_IP || '127.0.0.1',
          announcedIp: process.env.MEDIASOUP_ANNOUNCED_IP,
        },
      ],
      initialAvailableOutgoingBitrate: 1000000,
      minimumAvailableOutgoingBitrate: 600000,
      maxSctpMessageSize: 262144,
      enableUdp: true,
      enableTcp: true,
      preferUdp: true,
    },
  },
};
