import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL?.toString() || '/opennms/api',
  withCredentials: true
})

export { api }
