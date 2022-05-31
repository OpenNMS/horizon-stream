import axios from 'axios'
import useKeycloak from '@/composables/useKeycloak'

const api = axios.create({
  baseURL: process.env.API_BASE_URL || import.meta.env.VITE_BASE_URL?.toString()
})

api.interceptors.request.use(
  async (config) => {
    const { keycloak } = useKeycloak()

    const defaultHeaders = {
      Authorization: `Bearer ${keycloak.value?.token}`,
      'Content-Type': 'application/json'
    }

    config.headers = defaultHeaders

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

export { api }
