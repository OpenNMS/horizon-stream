import { api } from './axiosInstances'
import useSpinner from '@/composables/useSpinner'

const endpoint = '/login'
const { startSpinner, stopSpinner } = useSpinner()

const login = async (username: string, password: string): Promise<any> => {
  startSpinner()
  try {
    const resp = await api.post(endpoint, {
      username,
      password
    })
    return resp.data
  } catch (err) {
    return []
  } finally {
    stopSpinner()
  }
}

export { login }
