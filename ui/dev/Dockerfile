FROM node:17-alpine

WORKDIR /app

COPY package* ./
RUN yarn install
COPY . .

EXPOSE 3000

CMD ["echo", "started"]
