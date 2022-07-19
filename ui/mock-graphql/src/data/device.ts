// @ts-nocheck
import casual from 'casual'
import { rndNumber, rndStatus, rndLatency, rndUptime } from '../helpers/random'
import devicesMocked from './devices-mocked'

casual.define('device', function () {
  return {
    id: casual.uuid,
    id_minion: casual.uuid,
    name: `device-${casual.word}`,
    icmp_latency: rndLatency(),
    snmp_uptime: rndUptime(),
    status: rndStatus()
  }
})

casual.define('listDevices', function () {
  return devicesMocked
  /* return {
    items: [casual.device, casual.device, casual.device, casual.device, casual.device, casual.device, casual.device, casual.device, casual.device, casual.device, casual.device, casual.device, casual.device, casual.device, casual.device],
    count: rndNumber(),
    totalCount: rndNumber(),
    offset: rndNumber()
  } */
})

const device = casual.device
const listDevices = casual.listDevices

export {
  device,
  listDevices
}