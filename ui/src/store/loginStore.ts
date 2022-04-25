import { defineStore } from 'pinia'
import API from '@/services'

interface State {
  auth: any
}

export const useLoginStore = defineStore('loginStore', {
  state: () =>
    ({
      auth: {}
    } as State),
  actions: {
    async login(username: string, password: string) {
      this.auth = await API.login(username, password)
    }
  }
})
