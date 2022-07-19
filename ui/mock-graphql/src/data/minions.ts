// @ts-nocheck
import casual from 'casual'
import { rndNumber, rndStatus, rndLatency, rndUptime } from '../helpers/random'
import minionsMocked from './minions-mocked'

casual.define('minion', function () {
  return {
    id: casual.uuid,
    label: `minion-${casual.word}`,
    status: rndStatus(),
    location: casual.city,
    lastUpdated: casual.date(),
    icmp_latency: rndLatency(),
    snmp_uptime: rndUptime()
  }
})

casual.define('listMinions', function () {
  console.log('minionsMocked',minionsMocked)
  return {
    // minions: [casual.minion, casual.minion, casual.minion, casual.minion, casual.minion]
    minions: minionsMocked
    // count: rndNumber(),
    // totalCount: rndNumber(),
    // offset: rndNumber()
  }
})

const minion = casual.minion
const listMinions = casual.listMinions

export {
  minion,
  listMinions
}