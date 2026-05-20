FROM node:20-alpine

# mediasoup native bindings 컴파일에 필요한 도구
RUN apk add --no-cache python3 make g++ && ln -sf python3 /usr/bin/python

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY src/ ./src/

CMD ["node", "src/index.js"]
