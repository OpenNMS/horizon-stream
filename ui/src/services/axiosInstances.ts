import axios from 'axios'

const api = axios.create({
  baseURL: '/url',
  withCredentials: true
})

export { api }
