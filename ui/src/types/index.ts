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
