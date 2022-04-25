import { api } from './axiosInstances'

const endpoint = '/login'

const login = async (username: string, password: string): Promise<any> => {
  try {
    const resp = await api.post(endpoint, {
      username,
      password
    })
    return resp.data
  } catch (err) {
    return []
  }
}

export { login }
