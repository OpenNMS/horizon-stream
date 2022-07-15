// @ts-nocheck
import casual from 'casual'

const rndNumber = () => Math.floor(Math.random() * 100)

casual.define('device', function () {
  return {
    id: casual.uuid,
    name: casual.word,
    status: casual.safe_color_name,
    icmp_latency: rndNumber(),
    snmp_uptime: casual.unix_time
  }
})

casual.define('listDevices', function () {
  return {
    items: [casual.device, casual.device, casual.device],
    count: rndNumber(),
    totalCount: rndNumber(),
    offset: rndNumber()
  }
})

const device = casual.device
const listDevices = casual.listDevices

export {
  device,
  listDevices
}