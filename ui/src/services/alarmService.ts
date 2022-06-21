import { AlarmResponseList } from '@/types/alarms'
import { api } from './axiosInstances'
import useSpinner from '@/composables/useSpinner'
import useSnackbar from '@/composables/useSnackbar'
import { getMsgFromError } from './errorService'

const { startSpinner, stopSpinner } = useSpinner()
const { showSnackbar } = useSnackbar()

const endpoint = '/alarms'

const getAlarms = async (): Promise<AlarmResponseList> => {
  startSpinner()

  const emptyResponse = {
    alarm: [],
    count: 0,
    offset: 0,
    totalCount: 0
  }

  try {
    const resp = await api.get(`${endpoint}/list`)

    if (!resp.data) return emptyResponse

    return resp.data
  } catch (err: unknown) {
    if ((err as any).response?.status === 403) {
      showSnackbar({ error: true, msg: getMsgFromError(err) })
    }
    return emptyResponse
  } finally {
    stopSpinner()
  }
}

const deleteAlarmById = async (id: number): Promise<boolean> => {
  startSpinner()

  try {
    await api.post(`${endpoint}/${id}/clear`, { user: 'admin' })
    return true
  } catch (err: unknown) {
    showSnackbar({ error: true, msg: getMsgFromError(err) })
    return false
  } finally {
    stopSpinner()
  }
}

export { getAlarms, deleteAlarmById }
