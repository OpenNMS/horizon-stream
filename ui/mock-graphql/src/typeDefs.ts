const typeDefs = `
  type Device {
    id: String!
    id_minion: String!
    name: String!
    status: String!
    icmp_latency: String!
    snmp_uptime: String!
  }
  input DeviceInput {
    name: String
    icmp_latency: String
    snmp_uptime: String
    management_ip: String
    community_string: String
    port: Int
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
    lastUpdated: String!
    icmp_latency: String!
    snmp_uptime: String!
  }
  type ListMinions {
    minions: [Minion!]!
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
  type Mutation {
    saveDevice(device: DeviceInput!): String
  }
`

export default typeDefs
