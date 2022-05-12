import { Event } from '@/types/events'
import { defineStore } from 'pinia'
import API from '@/services'

export const useEventStore = defineStore('eventStore', {
  state: () => ({}),
  actions: {
    async sendEvent(event: Event) {
      await API.sendEvent(event)
    }
  }
})
