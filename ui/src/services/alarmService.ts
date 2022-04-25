import { api } from './axiosInstances'
import { Alarm } from '@/types/alarms'

const endpoint = '/alarms'

const getAlarms = async (): Promise<Alarm[]> => {
  try {
    const resp = await api.get(endpoint)
    return resp.data
  } catch (err) {
    return []
  }
}

const sendAlarm = async (alarm: Alarm): Promise<Alarm[]> => {
  try {
    const resp = await api.post(endpoint, alarm)
    return resp.data
  } catch (err) {
    return []
  }
}

const clearAlarm = async (alarm: Alarm): Promise<Alarm[]> => {
  try {
    const resp = await api.delete(`${endpoint}/${alarm.id}`)
    return resp.data
  } catch (err) {
    return []
  }
}

export { getAlarms, sendAlarm, clearAlarm }
