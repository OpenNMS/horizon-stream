import Login from '@/containers/Login.vue'
import { createTestingPinia } from '@pinia/testing'
import { test, expect } from 'vitest'
import { mount } from '@vue/test-utils'

interface LoginVm {
  usernameError: string
  passwordError: string
  username: string
  password: string
  onLoginBtnClick: () => void
}

const wrapper = mount(Login, { global: { plugins: [createTestingPinia()] }, props: {} })

test('the login page renders', () => {
  expect(wrapper).toBeTruthy()
})

test('login page input errors get set', async () => {
  ;(wrapper.vm as unknown as LoginVm).onLoginBtnClick()
  expect((wrapper.vm as unknown as LoginVm).usernameError).toBe('Username is required.')
  expect((wrapper.vm as unknown as LoginVm).passwordError).toBe('Password is required.')
})

test('username and password refs update with the correct value', async () => {
  const usernameInput = wrapper.get('[data-test="username-input"] .feather-input')
  const passwordInput = wrapper.get('[data-test="password-input"] .feather-input')
  await usernameInput.setValue('admin')
  await passwordInput.setValue('adminpass')
  expect((wrapper.vm as unknown as LoginVm).username).toBe('admin')
  expect((wrapper.vm as unknown as LoginVm).password).toBe('adminpass')
})
