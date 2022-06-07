import axios from 'axios'
import useSpinner from '@/composables/useSpinner'
import keycloakConfig from '../../keycloak.config'
import useKeycloak from '@/composables/useKeycloak'

const { startSpinner, stopSpinner } = useSpinner()
const { keycloak } = useKeycloak()

const auth = axios.create({
  baseURL: `${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect`
})

auth.interceptors.request.use(
  (config) => {
    const defaultHeaders = {
      Authorization: `Bearer ${keycloak.value?.tokenParsed}`
    }
    config.headers = defaultHeaders
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

const logout = async (): Promise<void> => {
  const params = new URLSearchParams()
  params.append('client_id', keycloakConfig.clientId as string)
  params.append('refresh_token', keycloak.value?.refreshTokenParsed as unknown as string)

  startSpinner()

  try {
    await auth.post('/logout', params)
  } finally {
    stopSpinner()
  }
}

export { logout }
