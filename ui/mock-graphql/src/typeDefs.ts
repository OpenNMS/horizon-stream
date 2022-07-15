const typeDefs = `
  type Device {
    id: String!
    name: String!
    status: String!
    icmp_latency: String!
    snmp_uptime: String!
  }
  type ListDevices {
    items: [Device!]!
    count: String!
    totalCount: String!
    offset: String!
  },
  type Minion {
    id: String!
    label: String!
    status: String!
    location: String!
    date: String!
    latency: String!
    uptime: String!
  }
  type ListMinions {
    items: [Minion!]!
    count: String!
    totalCount: String!
    offset: String!
  },
  type Query {
    device: Device!
    listDevices: ListDevices!
    minion: Minion!
    listMinions: ListMinions!
  },
  type Mutation {
    saveRoutingKey(key: String!): String
  }
`

export default typeDefs
