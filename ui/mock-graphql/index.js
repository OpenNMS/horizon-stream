import { createServer } from '@graphql-yoga/node'
import schema from './schema.js'

const start = async () => {
  const server = createServer(schema)
  await server.start()
}

start()