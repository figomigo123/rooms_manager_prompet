FROM node:18-alpine

WORKDIR /app

RUN apk add --no-cache python3 make g++

COPY package*.json ./

RUN npm ci --only=production

COPY src ./src

EXPOSE 8003

ENV NODE_ENV=production

CMD ["npm", "start"]
