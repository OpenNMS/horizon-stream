import { defineStore } from 'pinia'
import API from '@/services'
import { Alarm } from '@/types/alarms'

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
      const resp = await API.getAlarms()
      this.alarms = resp.alarm
    },
    async deleteAlarmById(id: number) {
      await API.deleteAlarmById(id)
    }
  }
})
