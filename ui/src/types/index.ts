import { Commit, Dispatch } from 'vuex'

export interface VuexContext {
  commit: Commit
  dispatch: Dispatch
}

export interface SnackbarProps {
  msg: string
  center?: boolean
  error?: boolean
}

export interface TokenResponse {
  access_token: string
  expires_in: number
  refresh_expires_in: number
  refresh_token: string
  scope: string
  session_state: string
  token_type: string
}

export interface UserInfo {
  email_verified: boolean
  preferred_username: string
  sub: string
}

export interface ResponseError {
  response: {
    status: number
    data: {
      error_description: string
    }
  }
}
