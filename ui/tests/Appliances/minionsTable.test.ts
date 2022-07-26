import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { createClient, setActiveClient } from 'villus'
import MinionsTable from '@/components/Appliances/MinionsTable.vue'
import { setActivePinia } from 'pinia'

describe('MinionsTable.vue', () => {
  beforeEach(() => {
    const minionItems = computed(() => [{
      id: '1',
      date: 'date1',
      label: 'minion1',
      status: 'UP',
      location: 'default'
    }]) 

    setActivePinia(createTestingPinia({
      initialState: { 
        minionsQueries  : { 
          listMinions: minionItems
        }
      }
    }))

    setActiveClient(createClient({
      url: 'http://test/graphql'
    }))
  })

  describe('Required columns', () => {
    const requiredColumns = [
      ['Date', 'col-date'],
      ['Minion', 'col-minion'],
      ['Latency', 'col-latency'],
      ['Uptime', 'col-uptime'],
      ['Status', 'col-status']
    ]

    it.each(requiredColumns)('should have "%s" column', (_, dataTest) => {
      const wrapper = mount(MinionsTable, { 
        global: { plugins: [createTestingPinia()] }
      })

      const elem = wrapper.find(`[data-test="${dataTest}"]`)
      expect(elem.exists()).toBe(true)
    })
  })
    
  it('should have an empty table when there\'s no minion', () =>{
    const wrapper = mount(MinionsTable, { 
      global: { plugins: [createTestingPinia()] }
    })
      
    const minionItem = wrapper.find('[data-test="minion-item"]')
    expect(minionItem.exists()).toBe(false)
  })
    
  it('should display a list when there\'s minion', () => {
    const wrapper = mount(MinionsTable)
    const minionItem = wrapper.find('[data-test="minion-item"]')
    expect(minionItem.exists()).toBe(true)
  })
})