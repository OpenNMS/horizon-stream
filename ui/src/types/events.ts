export interface Event {
  uei: string
  time: string
  source: string
  descr: string
  logmsg: {
    notify: boolean
    dest: string
  }
  "creation-time": string
}
