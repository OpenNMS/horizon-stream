// @ts-nocheck
import casual from 'casual'
import { rndNumber, rndStatus, rndLatency, rndUptime } from '../helpers/random'

casual.define('minion', function () {
  return {
    id: casual.uuid,
    label: `minion-${casual.word}`,
    status: rndStatus(),
    location: casual.city,
    lastUpdated: casual.date()
  }
})

casual.define('listMinions', function () {
  return {
    minions: [casual.minion, casual.minion, casual.minion, casual.minion, casual.minion]
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