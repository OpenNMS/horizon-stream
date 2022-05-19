import { add } from 'date-fns'
import { Event } from './events'

const mockEvent: Event = {
  uei: 'uei.opennms.org/alarms/trigger',
  time: add(new Date(), { days: 1 }).toISOString(),
  source: 'asn-cli-script',
  descr: 'A problem has been triggered...',
  'creation-time': new Date().toISOString(),
  logmsg: {
    notify: true,
    dest: 'A problem has been triggered on //...'
  }
}

export const getMockEvent = (): Event => mockEvent
