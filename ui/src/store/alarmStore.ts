import { defineStore } from 'pinia'
import { Alarm } from '@/types/alarms'
import API from '@/services'

interface State {
  alarms: Alarm[]
}

export const useAlarmStore = defineStore('alarmStore', {
  state: () =>
    ({
      alarms: []
    } as State),
  actions: {
    async getAlarms() {
      this.alarms = await API.getAlarms()
    },
    async sendAlarm(alarm: Alarm) {
      await API.sendAlarm(alarm)
    },
    async clearAlarm(alarm: Alarm) {
      await API.clearAlarm(alarm)
    }
  }
})
