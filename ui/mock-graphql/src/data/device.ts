// @ts-nocheck
import casual from 'casual'
import { rndNumber, rndStatus, rndLatency, rndUptime } from '../helpers/random'

casual.define('device', function () {
  return {
    id: casual.uuid,
    name: `device-${casual.word}`,
    icmp_latency: rndLatency(),
    snmp_uptime: rndUptime(),
    status: rndStatus()
  }
})

casual.define('listDevices', function () {
  return {
    items: [casual.device, casual.device, casual.device, casual.device, casual.device, casual.device, casual.device, casual.device, casual.device, casual.device, casual.device, casual.device, casual.device, casual.device, casual.device],
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