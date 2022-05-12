export interface AlarmResponseList {
  alarm: Alarm[]
  count: number
  totalCount: number
  offset: number
}

export interface Alarm {
  id: number
  uei: string
  location: string
  nodeId: null | string
  nodeLabel: null | string
  ipAddress: null | string
  serviceType: null | string
  reductionKey: string
  type: number
  count: number
  severity: string
  firstEventTime: number
  description: string
  logMessage: string
  operatorInstructions: null | string
  troubleTicket: null | string
  troubleTicketState: null | string
  troubleTicketLink: null | string
  mouseOverText: null | string
  suppressedUntil: number
  suppressedBy: null | string
  suppressedTime: number
  ackUser: null | string
  ackTime: null | string
  clearKey: null | string
  lastEvent: LastEvent
  parameter: number[]
  lastEventTime: number
  applicationDN: null | string
  managedObjectInstance: null | string
  managedObjectType: null | string
  ossPrimaryKey: null | string
  x733AlarmType: null | string
  x733ProbableCause: 0
  qosAlarmState: null | string
  firstAutomationTime: null | string
  lastAutomationTime: null | string
  ifIndex: null | string
  reductionKeyMemo: null | string
  stickyMemo: null | string
  relatedAlarms: null | string
  affectedNodeCount: 0
}

export interface LastEvent {
  id: number
  uei: string
  label: string
  time: number
  host: null | string
  source: string
  ipAddress: null | string
  snmpHost: null | string
  serviceType: null | string
  snmp: null | string
  parameter: number[]
  createTime: number
  description: string
  logGroup: null | string
  logMessage: string
  severity: string
  pathOutage: null | string
  correlation: null | string
  suppressedCount: null | string
  operatorInstructions: null | string
  autoAction: null | string
  operatorAction: null | string
  operationActionMenuText: null | string
  notification: null | string
  troubleTicket: null | string
  troubleTicketState: null | string
  mouseOverText: null | string
  log: string
  display: string
  ackUser: null | string
  ackTime: null | string
  nodeId: null | string
  nodeLabel: null | string
  ifIndex: null | string
  location: string
}
