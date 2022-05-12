import axios from 'axios'
import useSpinner from '@/composables/useSpinner'
import useSnackbar from '@/composables/useSnackbar'
import keycloakConfig from '../../keycloak.config'
import { UserInfo } from '@/types'
import useToken from '@/composables/useToken'
import router from '@/router'
import { getMsgFromError } from './errorService'

const { startSpinner, stopSpinner } = useSpinner()
const { showSnackbar } = useSnackbar()
const { setToken, token } = useToken()

const auth = axios.create({
  baseURL: `${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect`
})

auth.interceptors.request.use(
  (config) => {
    const defaultHeaders = {
      Authorization: `Bearer ${token.value.access_token}`
    }
    config.headers = defaultHeaders
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

const login = async (username: string, password: string): Promise<void> => {
  const params = new URLSearchParams()
  params.append('username', username)
  params.append('password', password)
  params.append('client_id', keycloakConfig.clientId as string)
  params.append('grant_type', 'password')

  startSpinner()
  try {
    const resp = await auth.post('/token', params)
    setToken(resp.data)
    router.push('/')
  } catch (err: unknown) {
    showSnackbar({ error: true, msg: getMsgFromError(err) })
  } finally {
    stopSpinner()
  }
}

const refreshToken = async (): Promise<boolean> => {
  const params = new URLSearchParams()
  params.append('client_id', keycloakConfig.clientId as string)
  params.append('grant_type', 'refresh_token')
  params.append('refresh_token', token.value.refresh_token)

  try {
    const resp = await auth.post('/token', params)
    setToken(resp.data)
    return true
  } catch (err: unknown) {
    setToken(null)
    router.push('/login')
    return false
  }
}

const logout = async (): Promise<void> => {
  const params = new URLSearchParams()
  params.append('client_id', keycloakConfig.clientId as string)
  params.append('refresh_token', token.value.refresh_token)

  startSpinner()

  try {
    await auth.post('/logout', params)
  } finally {
    setToken(null)
    stopSpinner()
    router.push('/login')
  }
}

const getUserInfo = async (): Promise<UserInfo | null> => {
  startSpinner()

  try {
    const resp = await auth.post('/userinfo')
    return resp.data
  } catch (err: unknown) {
    return null
  } finally {
    stopSpinner()
  }
}

export { login, logout, getUserInfo, refreshToken }
