import { Event } from './events'

const mockEvent: Event = {
  "uei": "uei.opennms.org/alarms/trigger",
  "time": "2022-01-12T17:12:22.000Z",
  "source": "asn-cli-script",
  "descr": "A problem has been triggered...",
  "creation-time": "2022-01-12T17:12:22.000Z",
  "logmsg": {
    "notify": true,
    "dest": "A problem has been triggered on //..."
  }
}

export const getMockEvent = (): Event => mockEvent
