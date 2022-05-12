import { api } from './axiosInstances'
import useSpinner from '@/composables/useSpinner'
import useSnackbar from '@/composables/useSnackbar'
import { Event } from '@/types/events'
import { getMsgFromError } from './errorService'

const { startSpinner, stopSpinner } = useSpinner()
const { showSnackbar } = useSnackbar()

const endpoint = '/events'

const sendEvent = async (event: Event): Promise<boolean> => {
  startSpinner()

  try {
    await api.post(endpoint, event)
    return true
  } catch (err: unknown) {
    showSnackbar({ error: true, msg: getMsgFromError(err) })
    return false
  } finally {
    stopSpinner()
  }
}

export { sendEvent }
